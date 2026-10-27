package org.example.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.example.model.*;
import org.example.parser.ErrorClassifier;
import org.example.parser.OutputParser;
import org.example.ui.LanguageManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class YtDlpService {
    LocalTime time = LocalTime.now();

    public int update(Consumer<String> onOutput) {
        try {
            ProcessBuilder pb = new ProcessBuilder("yt-dlp", "-U");
            Process process = pb.start();

            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        onOutput.accept("[OUT] " + time + ": " + line);
                    }
                } catch (Exception e) {
                    onOutput.accept("[ERR] " + time + ": " + e.getMessage());
                }
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        onOutput.accept("[ERR] " + time + ": " + line);
                    }
                } catch (Exception e) {
                    onOutput.accept("[ERR] " + time + ": " + e.getMessage());
                }
            });

            stdoutThread.start();
            stderrThread.start();

            int exitCode = process.waitFor();
            stdoutThread.join();
            stderrThread.join();

            return exitCode;

        } catch (Exception e) {
            onOutput.accept("[FATAL] " + time + ": " + e.getMessage());
            return -1;
        }
    }

    private void handleParsedLine(OutputParser parser, String line, Consumer<ProgressUpdate> onProgress) {
        double percent = parser.parseLine(line);
        if (percent >= 0) {
            DownloadStats stats = parser.getStats();
            onProgress.accept(new ProgressUpdate(
                    percent,
                    stats.getCompletedItems(),
                    stats.getTotalItems()
            ));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private String resolveBundledTool(String toolName) {
        File bundleDir = resolveBundleDir();
        if (bundleDir != null) {
            File tool = new File(bundleDir, toolName);
            if (tool.exists() && tool.canExecute()) {
                return tool.getAbsolutePath();
            }
        }
        return toolName;
    }

    public PlaylistFetchResult getPlaylistVideoIds(DownloadConfig config, Consumer<String> onOutput) {
        List<String> ids = new ArrayList<>();
        StringBuilder stderrBuffer = new StringBuilder();
        String playlistTitle = null;
        String playlistThumbnail = null;

        try {
            List<String> command = new ArrayList<>();
            command.add(resolveBundledTool("yt-dlp.exe"));

            if (config.hasProxy()) {
                command.add("--proxy");
                command.add(config.proxy());
            }

            Preset preset = config.preset();
            if (preset.getCookiesFromBrowser() != null && !preset.getCookiesFromBrowser().isBlank()) {
                command.add("--cookies-from-browser");
                command.add(preset.getCookiesFromBrowser());
            }

            command.add("--flat-playlist");
            command.add("--print");
            command.add("%(id)s");
            command.add("--print");
            command.add("TITLE:%(playlist_title,title)s");
            command.add("--print");
            command.add("PLAYLIST_THUMB:%(playlist_thumbnail)s");
            command.add("--print");
            command.add("FIRST_VIDEO_THUMB:%(thumbnail)s");

            command.add(config.url());

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) continue;

                    if (trimmed.startsWith("TITLE:")) {
                        if (playlistTitle == null || "NA".equals(playlistTitle)) {
                            playlistTitle = trimmed.substring("TITLE:".length()).trim();
                        }
                    } else if (trimmed.startsWith("PLAYLIST_THUMB:")) {
                        String thumb = trimmed.substring("PLAYLIST_THUMB:".length()).trim();
                        if (!"NA".equals(thumb) && !thumb.isBlank()) {
                            playlistThumbnail = thumb;
                        }
                    } else if (trimmed.startsWith("FIRST_VIDEO_THUMB:")) {
                        String thumb = trimmed.substring("FIRST_VIDEO_THUMB:".length()).trim();
                        if (playlistThumbnail == null && !"NA".equals(thumb) && !thumb.isBlank()) {
                            playlistThumbnail = thumb;
                        }
                    } else {
                        ids.add(trimmed);
                    }
                }
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderrBuffer.append(line).append("\n");
                }
            }

            process.waitFor();

            if ((playlistThumbnail == null || "NA".equals(playlistThumbnail) || playlistThumbnail.isBlank()) && !ids.isEmpty()) {
                String firstId = ids.get(0);
                playlistThumbnail = "https://i.ytimg.com/vi/" + firstId + "/maxresdefault.jpg";
            }

            if (ids.isEmpty()) {
                String stderr = stderrBuffer.toString();
                ErrorClassifier classifier = new ErrorClassifier();
                ErrorType error = ErrorType.UNKNOWN;

                for (String line : stderr.split("\n")) {
                    if (line.contains("ERROR:") || line.contains("Sign in to confirm")) {
                        ErrorType detected = classifier.classify(line);
                        if (detected != ErrorType.UNKNOWN) {
                            error = detected;
                            break;
                        }
                    }
                }

                if (error != ErrorType.UNKNOWN) {
                    onOutput.accept("[WARN] " + time + ": " + LanguageManager.getInstance().get(error.getMessageKey()));
                }
                return PlaylistFetchResult.failure(error);
            }

            return PlaylistFetchResult.success(ids, playlistTitle, playlistThumbnail);

        } catch (Exception e) {
            onOutput.accept("[WARN] " + time + ": " + LanguageManager.getInstance().get("status.error_lists") + e.getMessage());
            return PlaylistFetchResult.failure(ErrorType.UNKNOWN);
        }
    }

    public DownloadStats downloadByIds(DownloadConfig config, List<String> videoIds, DownloadCallbacks callbacks) {
        if (videoIds.isEmpty()) {
            return new DownloadStats();
        }

        try {
            Path tempFile = Files.createTempFile("fetchit-urls-", ".txt");
            List<String> urls = videoIds.stream()
                    .map(id -> "https://www.youtube.com/watch?v=" + id)
                    .toList();
            Files.write(tempFile, urls);

            DownloadConfig listConfig = new DownloadConfig(
                    tempFile.toAbsolutePath().toString(),
                    config.outputDir(),
                    config.proxy(),
                    config.preset()
            );

            DownloadStats stats = downloadFromFile(listConfig, videoIds.size(), callbacks);
            stats.setTotalItems(videoIds.size());

            Files.deleteIfExists(tempFile);
            return stats;

        } catch (Exception e) {
            callbacks.onLog("[FATAL] " + time + ": " + LanguageManager.getInstance().get("error.failed_urls") + e.getMessage());
            return new DownloadStats();
        }
    }

    private DownloadStats downloadFromFile(DownloadConfig config, int expectedTotal, DownloadCallbacks callbacks) {
        OutputParser parser = new OutputParser(expectedTotal);
        try {
            List<String> command = new ArrayList<>();
            command.add(resolveBundledTool("yt-dlp.exe"));

            if (config.hasProxy()) {
                command.add("--proxy");
                command.add(config.proxy());
            }

            Preset preset = config.preset();

            command.add("--newline");
            command.add("--no-quiet");

            command.add("--encoding");
            command.add("utf-8");

            command.add("--print");
            command.add("before_dl:TITLE:%(title)s");

            command.add("--print");
            command.add("before_dl:THUMB:%(thumbnail)s");

            command.add("-a");
            command.add(config.url());

            if (preset.isAudio()) {
                command.add("-x");
                command.add("--audio-format");
                command.add(preset.getAudioFormat());
                command.add("--audio-quality");
                command.add(String.valueOf(preset.getAudioQuality()));
                if (preset.isKeepVideo()) {
                    command.add("-k");
                }
            } else {
                String vq = preset.getVideoQuality();
                command.add("-f");
                if (vq == null || "best".equalsIgnoreCase(vq)) {
                    command.add("bestvideo+bestaudio/best");
                } else {
                    String height = vq.replace("p", "").trim();
                    command.add("bestvideo[height<=" + height + "]+bestaudio/best[height<=" + height + "]");
                }
                if (preset.getMergeOutputFormat() != null && !preset.getMergeOutputFormat().isBlank()) {
                    command.add("--merge-output-format");
                    command.add(preset.getMergeOutputFormat());
                }
                if (preset.getRecodeVideo() != null && !preset.getRecodeVideo().isBlank()) {
                    command.add("--recode-video");
                    command.add(preset.getRecodeVideo());
                }
            }

            if (preset.isEmbedThumbnail()) command.add("--embed-thumbnail");
            if (preset.isAddMetadata()) command.add("--add-metadata");
            if (preset.isEmbedChapters()) command.add("--embed-chapters");

            if (preset.isWriteSubtitles()) {
                command.add("--write-subs");
                if (preset.getSubtitleLanguages() != null && !preset.getSubtitleLanguages().isBlank()) {
                    command.add("--sub-langs");
                    command.add(preset.getSubtitleLanguages());
                }
            }
            if (preset.isEmbedSubtitles()) {
                command.add("--embed-subs");
                if (preset.getSubtitleLanguages() != null && !preset.getSubtitleLanguages().isBlank()) {
                    command.add("--sub-langs");
                    command.add(preset.getSubtitleLanguages());
                }
            }

            if (preset.getSponsorblockRemove() != null && !preset.getSponsorblockRemove().isBlank()) {
                command.add("--sponsorblock-remove");
                command.add(preset.getSponsorblockRemove());
            } else if (preset.getSponsorblockMark() != null && !preset.getSponsorblockMark().isBlank()) {
                command.add("--sponsorblock-mark");
                command.add(preset.getSponsorblockMark());
            }

            if (preset.getConcurrentFragments() > 1) {
                command.add("--concurrent-fragments");
                command.add(String.valueOf(preset.getConcurrentFragments()));
            }
            if (preset.getLimitRate() != null && !preset.getLimitRate().isBlank()) {
                command.add("--limit-rate");
                command.add(preset.getLimitRate());
            }
            if (preset.getRetries() > 0) {
                command.add("--retries");
                command.add(String.valueOf(preset.getRetries()));
            }

            if (preset.isNoOverwrites()) command.add("--no-overwrites");

            String ffmpegLocation = resolveFfmpegLocation();
            if (ffmpegLocation != null) {
                command.add("--ffmpeg-location");
                command.add(ffmpegLocation);
            }
            command.add("--parse-metadata");
            command.add("%(id)s:%(meta_comment)s");

            File tempDir = new File(System.getProperty("java.io.tmpdir"), "FetchIt_temp");
            File outputDir = new File(config.outputDir());

            if (!tempDir.exists()) tempDir.mkdirs();
            if (!outputDir.exists()) outputDir.mkdirs();

            command.add("-P");
            command.add("temp:" + tempDir.getAbsolutePath());
            command.add("-P");
            command.add("home:" + outputDir.getAbsolutePath());

            if (preset.isSplitChapters()) {
                command.add("--split-chapters");

                command.add("-o");
                command.add("%(title)s.%(ext)s");

                command.add("-o");
                command.add("chapter:%(section_title)s.%(ext)s");
            } else {
                command.add("-o");
                command.add("%(title)s.%(ext)s");
            }

            callbacks.onLog("[CMD] " + time + ": " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            Process process = pb.start();

            callbacks.onProcessStarted(process);

            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("TITLE:")) {
                            String title = line.substring(line.indexOf("TITLE:") + 6).trim();
                            callbacks.onTitleFetched(title);
                        } else if (line.contains("THUMB:")) {
                            String thumb = line.substring(line.indexOf("THUMB:") + 6).trim();
                            if (!thumb.equals("NA") && !thumb.isEmpty()) {
                                callbacks.onThumbnailFetched(thumb);
                            }
                        } else {
                            callbacks.onLog("[OUT] " + time + ": " + line);
                            handleParsedLine(parser, line, callbacks::onProgress);
                        }
                    }

                } catch (Exception e) {
                    callbacks.onLog("[ERR] " + time + ": " + e.getMessage());
                }
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        callbacks.onLog("[ERR] " + time + ": " + line);
                        handleParsedLine(parser, line, callbacks::onProgress);
                    }
                } catch (Exception e) {
                    callbacks.onLog("[ERR] " + time + ": " + e.getMessage());
                }
            });

            stdoutThread.start();
            stderrThread.start();
            process.waitFor();
            stdoutThread.join();
            stderrThread.join();

        } catch (Exception e) {
            callbacks.onLog("[FATAL] " + time + ": " + e.getMessage());
        }

        return parser.getStats();
    }

    private File resolveBundleDir() {
        //noinspection CatchMayIgnoreException
        try {
            File codeSource = new File(
                    YtDlpService.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI()
            );
            File appDir = codeSource.getParentFile();
            File rootDir = appDir.getParentFile();
            File bundle = new File(rootDir, "bundle");
            if (bundle.exists() && bundle.isDirectory()) {
                return bundle;
            }
        } catch (Exception e) {
        }

        File devBundle = new File("bundle");
        if (devBundle.exists() && devBundle.isDirectory()) {
            return devBundle;
        }

        return null;
    }

    private String resolveFfmpegLocation() {
        File bundleDir = resolveBundleDir();
        if (bundleDir != null) {
            File ffmpeg = new File(bundleDir, "ffmpeg.exe");
            if (ffmpeg.exists()) {
                return bundleDir.getAbsolutePath();
            }
        }
        return null;
    }
}
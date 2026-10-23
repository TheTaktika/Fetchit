package org.example.service;

import java.io.File;
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
                        new InputStreamReader(process.getInputStream()))) {
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
                        new InputStreamReader(process.getErrorStream()))) {
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

    public PlaylistFetchResult getPlaylistVideoIds(String url, Consumer<String> onOutput) {
        List<String> ids = new ArrayList<>();
        StringBuilder stderrBuffer = new StringBuilder();

        try {
            List<String> command = new ArrayList<>();
            command.add(resolveBundledTool("yt-dlp.exe"));
            command.add("--flat-playlist");
            command.add("--print");
            command.add("%(id)s");
            command.add(url);

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        ids.add(trimmed);
                    }
                }
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderrBuffer.append(line).append("\n");
                }
            }

            process.waitFor();

            if (ids.isEmpty()) {
                String stderr = stderrBuffer.toString();
                ErrorClassifier classifier = new ErrorClassifier();
                ErrorType error = ErrorType.UNKNOWN;

                for (String line : stderr.split("\n")) {
                    if (line.contains("ERROR:")) {
                        ErrorType detected = classifier.classify(line);
                        if (detected != ErrorType.UNKNOWN) {
                            error = detected;
                            break;
                        }
                    }
                }

                onOutput.accept("[WARN] " + time + ": " + LanguageManager.getInstance().get(error.getMessageKey()));
                return PlaylistFetchResult.failure(error);
            }

            return PlaylistFetchResult.success(ids);

        } catch (Exception e) {
            onOutput.accept("[WARN] " + time + ": " + LanguageManager.getInstance().get("status.error_lists") + e.getMessage());
            return PlaylistFetchResult.failure(ErrorType.UNKNOWN);
        }
    }

    public DownloadStats downloadByIds(DownloadConfig config,
                                       List<String> videoIds,
                                       Consumer<String> onOutput,
                                       Consumer<ProgressUpdate> onProgress,
                                       Consumer<Process> onProcessStarted) {
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
                    config.getOutputDir(),
                    config.getProxy(),
                    config.getPreset()
            );

            DownloadStats stats = downloadFromFile(listConfig, videoIds.size(), onOutput, onProgress, onProcessStarted);
            stats.setTotalItems(videoIds.size());

            Files.deleteIfExists(tempFile);
            return stats;

        } catch (Exception e) {
            onOutput.accept("[FATAL] " + time + ": " + LanguageManager.getInstance().get("error.failed_urls") + e.getMessage());
            return new DownloadStats();
        }
    }

    private DownloadStats downloadFromFile(DownloadConfig config,
                                           int expectedTotal,
                                           Consumer<String> onOutput,
                                           Consumer<ProgressUpdate> onProgress,
                                           Consumer<Process> onProcessStarted) {
        OutputParser parser = new OutputParser(expectedTotal);
        try {
            List<String> command = new ArrayList<>();
            command.add(resolveBundledTool("yt-dlp.exe"));

            if (config.hasProxy()) {
                command.add("--proxy");
                command.add(config.getProxy());
            }

            Preset preset = config.getPreset();

            command.add("--newline");
            command.add("-a");
            command.add(config.getUrl());

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

            if (preset.isEmbedThumbnail()) {
                command.add("--embed-thumbnail");
            }
            if (preset.isAddMetadata()) {
                command.add("--add-metadata");
            }
            if (preset.isEmbedChapters()) {
                command.add("--embed-chapters");
            }

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

            if (preset.isNoOverwrites()) {
                command.add("--no-overwrites");
            }

            String ffmpegLocation = resolveFfmpegLocation();
            if (ffmpegLocation != null) {
                command.add("--ffmpeg-location");
                command.add(ffmpegLocation);
            }
            command.add("--parse-metadata");
            command.add("%(id)s:%(meta_comment)s");

            command.add("-o");
            command.add(config.getOutputDir() + "/%(title)s.%(ext)s");

            onOutput.accept("[CMD] " + time + ": " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            onProcessStarted.accept(process);

            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        onOutput.accept("[OUT] " + time + ": " + line);
                        handleParsedLine(parser, line, onProgress);
                    }
                } catch (Exception e) {
                    onOutput.accept("[ERR] " + time + ": " + e.getMessage());
                }
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        onOutput.accept("[ERR] " + time + ": " + line);
                        handleParsedLine(parser, line, onProgress);
                    }
                } catch (Exception e) {
                    onOutput.accept("[ERR] " + time + ": " + e.getMessage());
                }
            });

            stdoutThread.start();
            stderrThread.start();
            process.waitFor();
            stdoutThread.join();
            stderrThread.join();

        } catch (Exception e) {
            onOutput.accept("[FATAL] " + time + ": " + e.getMessage());
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
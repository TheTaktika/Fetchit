package org.example.service;

import org.example.model.*;
import org.example.parser.ErrorClassifier;
import org.example.parser.OutputParser;
import org.example.parser.event.ParseEvent;
import org.example.ui.LanguageManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class YtDlpService {
    private final YtDlpCommandBuilder commandBuilder;
    private final ProcessRunner processRunner;
    private final ErrorClassifier errorClassifier;

    public YtDlpService() {
        this(new YtDlpCommandBuilder(new YtDlpPathResolver()), new ProcessRunner(), new ErrorClassifier());
    }

    public YtDlpService(YtDlpCommandBuilder commandBuilder, ProcessRunner processRunner, ErrorClassifier errorClassifier) {
        this.commandBuilder = commandBuilder;
        this.processRunner = processRunner;
        this.errorClassifier = errorClassifier;
    }

    public int update(Consumer<String> onOutput) {
        List<String> command = commandBuilder.buildUpdateCommand();
        return processRunner.run(
                command,
                line -> onOutput.accept("[OUT] " + LocalTime.now() + ": " + line),
                line -> onOutput.accept("[ERR] " + LocalTime.now() + ": " + line),
                null
        );
    }

    public PlaylistFetchResult getPlaylistVideoIds(DownloadConfig config, Consumer<String> onOutput) {
        List<String> ids = new ArrayList<>();
        StringBuilder stderrBuffer = new StringBuilder();
        String[] playlistTitle = {null};
        String[] playlistThumbnail = {null};

        List<String> command = commandBuilder.buildPlaylistCommand(config);

        int exitCode = processRunner.run(
                command,
                line -> {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) return;
                    if (trimmed.startsWith("TITLE:")) {
                        if (playlistTitle[0] == null || "NA".equals(playlistTitle[0])) {
                            playlistTitle[0] = trimmed.substring("TITLE:".length()).trim();
                        }
                    } else if (trimmed.startsWith("PLAYLIST_THUMB:")) {
                        String thumb = trimmed.substring("PLAYLIST_THUMB:".length()).trim();
                        if (!"NA".equals(thumb) && !thumb.isBlank()) playlistThumbnail[0] = thumb;
                    } else if (trimmed.startsWith("FIRST_VIDEO_THUMB:")) {
                        String thumb = trimmed.substring("FIRST_VIDEO_THUMB:".length()).trim();
                        if (playlistThumbnail[0] == null && !"NA".equals(thumb) && !thumb.isBlank()) {
                            playlistThumbnail[0] = thumb;
                        }
                    } else {
                        ids.add(trimmed);
                    }
                },
                line -> stderrBuffer.append(line).append("\n"),
                null
        );

        if ((playlistThumbnail[0] == null || "NA".equals(playlistThumbnail[0]) || playlistThumbnail[0].isBlank()) && !ids.isEmpty()) {
            playlistThumbnail[0] = "https://i.ytimg.com/vi/" + ids.get(0) + "/maxresdefault.jpg";
        }

        if (ids.isEmpty() && exitCode != 0) {
            ErrorType error = ErrorType.UNKNOWN;
            for (String line : stderrBuffer.toString().split("\n")) {
                if (line.contains("ERROR:") || line.contains("Sign in to confirm")) {
                    ErrorType detected = errorClassifier.classify(line);
                    if (detected != ErrorType.UNKNOWN) {
                        error = detected;
                        break;
                    }
                }
            }
            if (error != ErrorType.UNKNOWN) {
                onOutput.accept("[WARN] " + LocalTime.now() + ": " + LanguageManager.getInstance().get(error.getMessageKey()));
            }
            return PlaylistFetchResult.failure(error);
        }

        return PlaylistFetchResult.success(ids, playlistTitle[0], playlistThumbnail[0]);
    }

    public DownloadStats downloadByIds(DownloadConfig config, List<String> videoIds, DownloadCallbacks callbacks) {
        if (videoIds.isEmpty()) return new DownloadStats();

        try {
            Path tempFile = Files.createTempFile("fetchit-urls-", ".txt");
            List<String> urls = videoIds.stream().map(id -> "https://www.youtube.com/watch?v=" + id).toList();
            Files.write(tempFile, urls);

            DownloadConfig listConfig = new DownloadConfig(
                    tempFile.toAbsolutePath().toString(), config.outputDir(), config.proxy(), config.preset()
            );

            DownloadStats stats = downloadFromFile(listConfig, videoIds.size(), callbacks);
            stats.setTotalItems(videoIds.size());
            Files.deleteIfExists(tempFile);
            return stats;
        } catch (Exception e) {
            callbacks.onLog("[FATAL] " + LocalTime.now() + ": " + LanguageManager.getInstance().get("error.failed_urls") + e.getMessage());
            return new DownloadStats();
        }
    }

    private DownloadStats downloadFromFile(DownloadConfig config, int expectedTotal, DownloadCallbacks callbacks) {
        DownloadStats stats = new DownloadStats();
        stats.setTotalItems(expectedTotal);
        OutputParser parser = new OutputParser();
        List<String> command = commandBuilder.buildDownloadCommand(config);

        callbacks.onLog("[CMD] " + LocalTime.now() + ": " + String.join(" ", command));

        processRunner.run(
                command,
                line -> handleStdoutLine(line, parser, stats, callbacks),
                line -> {
                    callbacks.onLog("[ERR] " + LocalTime.now() + ": " + line);
                    handleParseEvent(parser.parseLine(line), stats, callbacks);
                },
                callbacks::onProcessStarted
        );

        return stats;
    }

    private void handleStdoutLine(String line, OutputParser parser, DownloadStats stats, DownloadCallbacks callbacks) {
        if (line.contains("TITLE:")) {
            callbacks.onTitleFetched(line.substring(line.indexOf("TITLE:") + 6).trim());
        } else if (line.contains("THUMB:")) {
            String thumb = line.substring(line.indexOf("THUMB:") + 6).trim();
            if (!thumb.equals("NA") && !thumb.isEmpty()) callbacks.onThumbnailFetched(thumb);
        } else {
            callbacks.onLog("[OUT] " + LocalTime.now() + ": " + line);
            handleParseEvent(parser.parseLine(line), stats, callbacks);
        }
    }

    private void handleParseEvent(ParseEvent event, DownloadStats stats, DownloadCallbacks callbacks) {
        switch (event) {
            case ParseEvent.Progress p -> callbacks.onProgress(new ProgressUpdate(p.percent(), stats.getCompletedItems(), stats.getTotalItems()));
            case ParseEvent.Error e -> {
                stats.incrementErrors();
                stats.addError(e.type(), e.message());
            }
            case ParseEvent.TotalItems t -> stats.setTotalItems(t.total());
            case ParseEvent.Completed c -> {
                stats.incrementDownloaded();
                stats.incrementCompleted();
            }
            case ParseEvent.Skipped s -> {
                stats.incrementSkipped();
                stats.incrementCompleted();
            }
            case ParseEvent.Ignored ignored -> { }
        }
    }
}
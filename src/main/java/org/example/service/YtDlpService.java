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
        List<String> urls = new ArrayList<>();
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
                        urls.add(trimmed);
                    }
                },
                line -> stderrBuffer.append(line).append("\n"),
                null
        );

        if (urls.isEmpty() && exitCode != 0) {
            String stderr = stderrBuffer.toString();
            ErrorType error = ErrorType.UNKNOWN;

            for (String line : stderr.split("\n")) {
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
            } else {
                onOutput.accept("[ERR] " + LocalTime.now() + ": " + stderr);
            }
            return PlaylistFetchResult.failure(error);
        }

        return PlaylistFetchResult.success(urls, playlistTitle[0], playlistThumbnail[0]);
    }

    public DownloadStats downloadByIds(DownloadConfig config, List<String> videoUrls, DownloadCallbacks callbacks) {
        if (videoUrls.isEmpty()) return new DownloadStats();

        try {
            Path tempFile = Files.createTempFile("fetchit-urls-", ".txt");
            Files.write(tempFile, videoUrls);

            DownloadConfig listConfig = new DownloadConfig(
                    tempFile.toAbsolutePath().toString(), config.outputDir(), config.proxy(), config.preset()
            );

            DownloadStats stats = downloadFromFile(listConfig, videoUrls.size(), callbacks);
            stats.setTotalItems(videoUrls.size());
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
                line -> handleStderrLine(line, parser, stats, callbacks),
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
            case ParseEvent.Progress p -> callbacks.onProgress(new ProgressUpdate(
                    p.percent(),
                    stats.getCompletedItems(),
                    stats.getTotalItems(),
                    p.speed(),
                    p.eta()
            ));
            case ParseEvent.Error e -> {
                stats.incrementErrors();
                stats.addError(e.type(), e.message());
            }
            case ParseEvent.TotalItems t -> stats.setTotalItems(t.total());
            case ParseEvent.Completed() -> {
                stats.incrementDownloaded();
                stats.incrementCompleted();
            }
            case ParseEvent.Skipped() -> {
                stats.incrementSkipped();
                stats.incrementCompleted();
            }
            case ParseEvent.Ignored() -> { }
        }
    }

    private void handleStderrLine(String line, OutputParser parser, DownloadStats stats, DownloadCallbacks callbacks) {
        ParseEvent event = parser.parseLine(line);

        if (event instanceof ParseEvent.Error err && err.type() != ErrorType.UNKNOWN) {
            callbacks.onLog("[ERR] " + LocalTime.now() + ": " + LanguageManager.getInstance().get(err.type().getMessageKey()));
            handleParseEvent(event, stats, callbacks);
        } else {
            callbacks.onLog("[ERR] " + LocalTime.now() + ": " + line);
            handleParseEvent(event, stats, callbacks);
        }
    }
}
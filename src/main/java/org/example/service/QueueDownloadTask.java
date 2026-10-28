package org.example.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.example.model.*;
import org.example.scanner.DownloaderScanner;
import org.example.ui.LanguageManager;
import org.example.ui.StatsFormatter;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class QueueDownloadTask extends Task<Void> {
    private final DownloadItem item;
    private final YtDlpService ytDlpService;
    private final DownloaderScanner scanner;
    private final Consumer<String> logCallback;
    private final Consumer<Process> onProcessStartedCallback;

    public QueueDownloadTask(DownloadItem item, YtDlpService ytDlpService, DownloaderScanner scanner,
                             Consumer<String> logCallback, Consumer<Process> onProcessStartedCallback) {
        this.item = item;
        this.ytDlpService = ytDlpService;
        this.scanner = scanner;
        this.logCallback = logCallback;
        this.onProcessStartedCallback = onProcessStartedCallback;
    }

    @Override
    protected Void call() {
        item.setStatus(DownloadStatus.FETCHING);

        DownloadConfig config = item.getConfig();
        Set<String> downloadedIds = scanner.scanDownloadedIds(config.outputDir());

        PlaylistFetchResult fetchResult = ytDlpService.getPlaylistVideoIds(
                config,
                line -> Platform.runLater(() -> logCallback.accept(line + "\n"))
        );

        if (!fetchResult.isSuccess()) {
            item.setStatus(DownloadStatus.ERROR);
            return null;
        }

        List<String> allIds = fetchResult.ids();
        List<String> toDownload = allIds.stream()
                .filter(id -> !downloadedIds.contains(id))
                .toList();

        if (allIds.size() > 1) {
            updateItemMetadata(item, fetchResult.title(), fetchResult.thumbnail());
        }

        if (toDownload.isEmpty()) {
            handleAllAlreadyDownloaded(item, fetchResult, allIds.size());
            return null;
        }

        item.setStatus(DownloadStatus.DOWNLOADING);

        DownloadStats stats = ytDlpService.downloadByIds(
                config,
                toDownload,
                new QueueDownloadCallbacks(item, allIds, logCallback, onProcessStartedCallback)
        );

        if (item.getStatus() == DownloadStatus.CANCELLED || item.getStatus() == DownloadStatus.PAUSED) {
            return null;
        }

        if (stats.getErrorsCount() > 0) {
            item.setStatus(DownloadStatus.ERROR);
        } else {
            item.setStatus(DownloadStatus.DONE);
            item.setProgress(100.0);
        }

        Platform.runLater(() -> item.setSummary(StatsFormatter.format(stats)));
        return null;
    }

    private void handleAllAlreadyDownloaded(DownloadItem item, PlaylistFetchResult fetchResult, int totalCount) {
        item.setStatus(DownloadStatus.DONE);
        item.setProgress(100.0);

        updateItemMetadata(item, fetchResult.title(), fetchResult.thumbnail());

        DownloadStats emptyStats = new DownloadStats();
        emptyStats.setPlaylistTotal(totalCount);
        emptyStats.setAlreadyOnDisk(totalCount);
        emptyStats.setTotalItems(0);

        Platform.runLater(() -> {
            item.setSummary(StatsFormatter.format(emptyStats));
            logCallback.accept(LanguageManager.getInstance().get("status.all_downloaded") + "\n");
        });
    }

    private void updateItemMetadata(DownloadItem item, String title, String thumbUrl) {
        if (title != null && !"NA".equals(title)) {
            Platform.runLater(() -> item.setTitle(title));
        }
        if (thumbUrl != null && !"NA".equals(thumbUrl)) {
            String safeUrl = thumbUrl.replace("vi_webp", "vi").replace(".webp", ".jpg");
            Image img = new Image(safeUrl, true);
            Platform.runLater(() -> item.setThumbnail(img));
        }
    }
}
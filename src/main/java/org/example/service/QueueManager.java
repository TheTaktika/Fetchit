package org.example.service;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.example.model.*;
import org.example.scanner.DownloaderScanner;
import org.example.ui.LanguageManager;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class QueueManager {
    private final ObservableList<DownloadItem> queue;
    private final YtDlpService ytDlpService;
    private final Consumer<String> logCallback;
    private DownloadItem currentItem = null;
    private final Consumer<String> statsCallback;

    public QueueManager(ObservableList<DownloadItem> queue, YtDlpService ytDlpService,
                        Consumer<String> logCallback, Consumer<String> statsCallback) {
        this.queue = queue;
        this.ytDlpService = ytDlpService;
        this.logCallback = logCallback;
        this.statsCallback = statsCallback;
    }

    public void processQueue() {
        if (currentItem != null) return;

        DownloadItem nextItem = queue.stream()
                .filter(item -> item.getStatus() == DownloadItem.Status.WAITING)
                .findFirst()
                .orElse(null);

        if (nextItem == null) return;

        currentItem = nextItem;
        startDownloadTask(nextItem);
    }

    private void startDownloadTask(DownloadItem item) {
        item.setStatus(DownloadItem.Status.FETCHING);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                DownloadConfig config = item.getConfig();
                DownloaderScanner scanner = new DownloaderScanner();
                Set<String> downloadedIds = scanner.scanDownloadedIds(config.getOutputDir());

                PlaylistFetchResult fetchResult = ytDlpService.getPlaylistVideoIds(
                        config.getUrl(),
                        line -> Platform.runLater(() -> logCallback.accept(line + "\n"))
                );

                if (!fetchResult.isSuccess()) {
                    item.setStatus(DownloadItem.Status.ERROR);
                    Platform.runLater(() -> logCallback.accept(LanguageManager.getInstance().get(fetchResult.error().getMessageKey()) + "\n"));
                    return null;
                }

                List<String> allIds = fetchResult.ids();
                List<String> toDownload = allIds.stream()
                        .filter(id -> !downloadedIds.contains(id))
                        .toList();

                if (toDownload.isEmpty()) {
                    item.setStatus(DownloadItem.Status.DONE);
                    item.setProgress(100.0);
                    Platform.runLater(() -> logCallback.accept(LanguageManager.getInstance().get("status.all_downloaded") + "\n"));
                    return null;
                }

                item.setStatus(DownloadItem.Status.DOWNLOADING);

                DownloadStats stats = ytDlpService.downloadByIds(
                        config,
                        toDownload,
                        line -> Platform.runLater(() -> logCallback.accept(line + "\n")),
                        update -> Platform.runLater(() -> {
                            double percent = 0;
                            if (update.totalItems() > 0) {
                                double currentFileFraction = update.filePercent() / 100.0;
                                percent = ((update.completedItems() + currentFileFraction) / update.totalItems()) * 100.0;
                            }
                            if (percent >= 100.0) {
                                item.setProgress(99.0);
                            } else {
                                item.setProgress(Math.min(percent, 99.0));
                            }
                        }),
                        item::setCurrentProcess
                );

                if (item.getStatus() == DownloadItem.Status.CANCELLED || item.getStatus() == DownloadItem.Status.PAUSED) {
                    return null;
                }

                if (stats.getErrorsCount() > 0) {
                    item.setStatus(DownloadItem.Status.ERROR);
                } else {
                    item.setStatus(DownloadItem.Status.DONE);
                    item.setProgress(100.0);
                }

                String summary = stats.toSummary();
                Platform.runLater(() -> statsCallback.accept(summary));

                return null;
            }
        };

        task.setOnSucceeded(e -> finishTask(item));
        task.setOnFailed(e -> {
            if (item.getStatus() != DownloadItem.Status.CANCELLED && item.getStatus() != DownloadItem.Status.PAUSED) {
                item.setStatus(DownloadItem.Status.ERROR);
                Platform.runLater(() -> logCallback.accept(LanguageManager.getInstance().get("status.error_prefix") + task.getException().getMessage() + "\n"));
            }
            finishTask(item);
        });

        new Thread(task).start();
    }

    private void finishTask(DownloadItem item) {
        item.setCurrentProcess(null);
        if (currentItem == item) {
            currentItem = null;
            processQueue();
        }
    }

    public void pauseItem(DownloadItem item) {
        item.pause();
        if (currentItem == item) {
            currentItem = null;
            processQueue();
        }
    }

    public void resumeItem(DownloadItem item) {
        if (item.getStatus() == DownloadItem.Status.PAUSED || item.getStatus() == DownloadItem.Status.CANCELLED || item.getStatus() == DownloadItem.Status.ERROR) {
            item.setStatus(DownloadItem.Status.WAITING);
            processQueue();
        }
    }

    public Consumer<String> getStatsCallback() {
        return statsCallback;
    }
}
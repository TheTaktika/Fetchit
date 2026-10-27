package org.example.service;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.example.model.*;
import org.example.scanner.DownloaderScanner;
import org.example.ui.LanguageManager;
import org.example.ui.StatsFormatter;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class QueueManager {
    private final ObservableList<DownloadItem> queue;
    private final YtDlpService ytDlpService;
    private final Consumer<String> logCallback;
    private DownloadItem currentItem = null;
    private volatile Process currentProcess;

    public QueueManager(ObservableList<DownloadItem> queue, YtDlpService ytDlpService,
                        Consumer<String> logCallback) {
        this.queue = queue;
        this.ytDlpService = ytDlpService;
        this.logCallback = logCallback;
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
                Set<String> downloadedIds = scanner.scanDownloadedIds(config.outputDir());

                PlaylistFetchResult fetchResult = ytDlpService.getPlaylistVideoIds(
                        config,
                        line -> Platform.runLater(() -> logCallback.accept(line + "\n"))
                );

                if (!fetchResult.isSuccess()) {
                    item.setStatus(DownloadItem.Status.ERROR);
                    return null;
                }

                List<String> allIds = fetchResult.ids();
                List<String> toDownload = allIds.stream()
                        .filter(id -> !downloadedIds.contains(id))
                        .toList();

                if (allIds.size() > 1) {
                    if (fetchResult.title() != null && !"NA".equals(fetchResult.title())) {
                        Platform.runLater(() -> item.setTitle(fetchResult.title()));
                    }
                    if (fetchResult.thumbnail() != null && !"NA".equals(fetchResult.thumbnail())) {
                        String safeUrl = fetchResult.thumbnail().replace("vi_webp", "vi").replace(".webp", ".jpg");
                        javafx.scene.image.Image img = new javafx.scene.image.Image(safeUrl, true);
                        Platform.runLater(() -> item.setThumbnail(img));
                    }
                }

                if (toDownload.isEmpty()) {
                    item.setStatus(DownloadItem.Status.DONE);
                    item.setProgress(100.0);

                    if (fetchResult.title() != null && !"NA".equals(fetchResult.title())) {
                        Platform.runLater(() -> item.setTitle(fetchResult.title()));
                    }
                    if (fetchResult.thumbnail() != null && !"NA".equals(fetchResult.thumbnail())) {
                        String safeUrl = fetchResult.thumbnail().replace("vi_webp", "vi").replace(".webp", ".jpg");
                        javafx.scene.image.Image img = new javafx.scene.image.Image(safeUrl, true);
                        Platform.runLater(() -> item.setThumbnail(img));
                    }

                    DownloadStats emptyStats = new DownloadStats();
                    emptyStats.setPlaylistTotal(allIds.size());
                    emptyStats.setAlreadyOnDisk(allIds.size());
                    emptyStats.setTotalItems(0);

                    Platform.runLater(() -> {
                        item.setSummary(StatsFormatter.format(emptyStats));
                        logCallback.accept(LanguageManager.getInstance().get("status.all_downloaded") + "\n");
                    });
                    return null;
                }

                item.setStatus(DownloadItem.Status.DOWNLOADING);

                DownloadStats stats = ytDlpService.downloadByIds(
                        config,
                        toDownload,
                        new DownloadCallbacks() {
                            @Override
                            public void onLog(String message) {
                                Platform.runLater(() -> logCallback.accept(message));
                            }

                            @Override
                            public void onProgress(ProgressUpdate update) {
                                Platform.runLater(() -> {
                                    double percent = 0;
                                    if (update.totalItems() > 0) {
                                        double currentFileFraction = update.filePercent() / 100.0;
                                        percent = ((update.completedItems() + currentFileFraction) / update.totalItems()) * 100.0;
                                    }

                                    if (percent >= 100.0) {
                                        item.setProgress(99.0);
                                        if (item.getStatus() == DownloadItem.Status.DOWNLOADING) {
                                            item.setStatus(DownloadItem.Status.PROCESSING);
                                        }
                                    } else {
                                        item.setProgress(Math.min(percent, 99.0));
                                    }
                                });
                            }

                            @Override
                            public void onProcessStarted(Process process) {
                                currentProcess = process;
                            }

                            @Override
                            public void onTitleFetched(String title) {
                                if (allIds.size() > 1) return;

                                Platform.runLater(() -> {
                                    if (title == null || title.isBlank() || "NA".equals(title)) {
                                        item.setTitle(item.getConfig().url());
                                        logCallback.accept("[WARN] " + LanguageManager.getInstance().get("error.empty_title") + item.getConfig().url() + "\n");
                                    } else {
                                        item.setTitle(title);
                                    }
                                });
                            }

                            @Override
                            public void onThumbnailFetched(String thumbnailUrl) {
                                if (allIds.size() > 1) return;

                                String safeUrl = thumbnailUrl;
                                if (safeUrl.endsWith(".webp")) {
                                    safeUrl = safeUrl.replace("vi_webp", "vi").replace(".webp", ".jpg");
                                }

                                javafx.scene.image.Image image = new javafx.scene.image.Image(safeUrl, true);

                                image.errorProperty().addListener((obs, oldVal, newVal) -> {
                                    if (newVal) {
                                        String errorMsg = LanguageManager.getInstance().get("error.thumbnail_load")
                                                + item.getConfig().url();
                                        Platform.runLater(() -> logCallback.accept("[WARN] " + errorMsg + "\n"));
                                    }
                                });

                                Platform.runLater(() -> item.setThumbnail(image));
                            }
                        }
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

                Platform.runLater(() -> item.setSummary(StatsFormatter.format(stats)));

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
        currentProcess = null;
        if (currentItem == item) {
            currentItem = null;
            processQueue();
        }
    }

    public void cancelItem(DownloadItem item) {
        killCurrentProcess();
        item.cancel();
        if (currentItem == item) {
            currentItem = null;
            processQueue();
        }
    }

    public void pauseItem(DownloadItem item) {
        killCurrentProcess();
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

    private void killCurrentProcess() {
        if (currentProcess != null && currentProcess.isAlive()) {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                try {
                    new ProcessBuilder("taskkill", "/f", "/t", "/pid", String.valueOf(currentProcess.pid()))
                            .start().waitFor();
                } catch (Exception e) {
                    currentProcess.destroyForcibly();
                }
            } else {
                currentProcess.descendants().forEach(ProcessHandle::destroyForcibly);
                currentProcess.destroyForcibly();
            }
        }
    }
}
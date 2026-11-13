package org.example.service;

import org.example.model.DownloadItem;
import org.example.model.DownloadStatus;
import org.example.scanner.DownloaderScanner;
import org.example.ui.LanguageManager;

import java.util.List;
import java.util.function.Consumer;

public class QueueManager {
    private final List<DownloadItem> queue;
    private final YtDlpService ytDlpService;
    private final Consumer<String> logCallback;
    private final DownloaderScanner scanner;
    private final ProcessKiller processKiller;

    private DownloadItem currentItem = null;
    private volatile Process currentProcess;

    public QueueManager(List<DownloadItem> queue, YtDlpService ytDlpService, Consumer<String> logCallback) {
        this.queue = queue;
        this.ytDlpService = ytDlpService;
        this.logCallback = logCallback;
        this.scanner = new DownloaderScanner();
        this.processKiller = new ProcessKiller();
    }

    public void processQueue() {
        if (currentItem != null) return;

        DownloadItem nextItem = queue.stream()
                .filter(item -> item.getStatus() == DownloadStatus.WAITING)
                .findFirst()
                .orElse(null);

        if (nextItem == null) return;

        currentItem = nextItem;
        startDownloadTask(nextItem);
    }

    private void startDownloadTask(DownloadItem item) {
        QueueDownloadTask task = new QueueDownloadTask(
                item,
                ytDlpService,
                scanner,
                logCallback,
                process -> currentProcess = process
        );

        Thread.startVirtualThread(() -> {
            try {
                task.run();
                finishTask(item);
            } catch (Exception e) {
                if (item.getStatus() != DownloadStatus.CANCELLED && item.getStatus() != DownloadStatus.PAUSED) {
                    item.setStatus(DownloadStatus.ERROR);
                    item.addFailedUrl(item.getUrl());
                    logCallback.accept(LanguageManager.getInstance().get("status.error_prefix") + e.getMessage() + "\n");
                }
                finishTask(item);
            }
        });
    }

    private void finishTask(DownloadItem item) {
        currentProcess = null;
        if (currentItem == item) {
            currentItem = null;
            processQueue();
        }
    }

    public void cancelItem(DownloadItem item) {
        processKiller.kill(currentProcess);
        item.cancel();
        if (currentItem == item) {
            currentItem = null;
            processQueue();
        }
    }

    public void pauseItem(DownloadItem item) {
        processKiller.kill(currentProcess);
        item.pause();
        if (currentItem == item) {
            currentItem = null;
            processQueue();
        }
    }

    public void resumeItem(DownloadItem item) {
        if (item.getStatus() == DownloadStatus.PAUSED || item.getStatus() == DownloadStatus.CANCELLED || item.getStatus() == DownloadStatus.ERROR) {
            item.setStatus(DownloadStatus.WAITING);
            processQueue();
        }
    }
}

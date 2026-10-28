package org.example.service;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.example.model.DownloadItem;
import org.example.model.DownloadStatus;
import org.example.model.ProgressUpdate;
import org.example.ui.LanguageManager;

import java.util.List;
import java.util.function.Consumer;

public class QueueDownloadCallbacks implements DownloadCallbacks {
    private final DownloadItem item;
    private final List<String> allIds;
    private final Consumer<String> logCallback;
    private final Consumer<Process> onProcessStartedCallback;

    public QueueDownloadCallbacks(DownloadItem item, List<String> allIds, Consumer<String> logCallback, Consumer<Process> onProcessStartedCallback) {
        this.item = item;
        this.allIds = allIds;
        this.logCallback = logCallback;
        this.onProcessStartedCallback = onProcessStartedCallback;
    }

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
                if (item.getStatus() == DownloadStatus.DOWNLOADING) {
                    item.setStatus(DownloadStatus.PROCESSING);
                }
            } else {
                item.setProgress(Math.min(percent, 99.0));
            }
        });
    }

    @Override
    public void onProcessStarted(Process process) {
        onProcessStartedCallback.accept(process);
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

        Image image = new Image(safeUrl, true);

        image.errorProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                String errorMsg = LanguageManager.getInstance().get("error.thumbnail_load") + item.getConfig().url();
                Platform.runLater(() -> logCallback.accept("[WARN] " + errorMsg + "\n"));
            }
        });

        Platform.runLater(() -> item.setThumbnail(image));
    }
}
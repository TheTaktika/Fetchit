package org.example.service;

import org.example.model.DownloadItem;
import org.example.model.DownloadStatus;
import org.example.model.ProgressUpdate;
import org.example.ui.LanguageManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
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
        logCallback.accept(message);
    }

    @Override
    public void onProgress(ProgressUpdate update) {
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

        item.setSpeed(update.speed() != null ? update.speed() : "");
        item.setEta(update.eta() != null && !"Unknown".equalsIgnoreCase(update.eta()) ? update.eta() : "");
    }

    @Override
    public void onProcessStarted(Process process) {
        onProcessStartedCallback.accept(process);
    }

    @Override
    public void onTitleFetched(String title) {
        if (allIds.size() > 1) return;

        if (title == null || title.isBlank() || "NA".equals(title)) {
            item.setTitle(item.getConfig().url());
            logCallback.accept("[WARN] " + LanguageManager.getInstance().get("error.empty_title") + item.getConfig().url() + "\n");
        } else {
            item.setTitle(title);
        }
    }

    @Override
    public void onThumbnailFetched(String thumbnailUrl) {
        if (allIds.size() > 1) return;

        if (thumbnailUrl == null || thumbnailUrl.isBlank() || "NA".equals(thumbnailUrl)) {
            logCallback.accept("[WARN] " + LanguageManager.getInstance().get("error.thumbnail_load") + item.getConfig().url() + "\n");
            return;
        }

        String safeUrl = thumbnailUrl;
        if (safeUrl.endsWith(".webp")) {
            safeUrl = safeUrl.replace("vi_webp", "vi").replace(".webp", ".jpg");
        }

        final String finalSafeUrl = safeUrl;

        Thread.startVirtualThread(() -> {
            try {
                URL url = URI.create(finalSafeUrl).toURL();
                try (InputStream in = url.openStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    item.setThumbnailData(out.toByteArray());
                }
            } catch (Exception e) {
                String errorMsg = LanguageManager.getInstance().get("error.thumbnail_load")
                        + " [" + e.getMessage() + "] URL: " + finalSafeUrl;
                logCallback.accept("[WARN] " + errorMsg + "\n");
            }
        });
    }
}
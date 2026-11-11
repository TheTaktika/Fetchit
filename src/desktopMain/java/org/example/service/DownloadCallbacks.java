package org.example.service;

import org.example.model.ProgressUpdate;

public interface DownloadCallbacks {
    void onLog(String message);
    void onProgress(ProgressUpdate update);
    void onProcessStarted(Process process);
    void onTitleFetched(String title);
    void onThumbnailFetched(String thumbnailUrl);
}
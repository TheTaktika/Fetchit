package org.example.model;

import org.example.ui.LanguageManager;
import java.util.Objects;

public class DownloadItem {
    private final String url;
    private String title;
    private DownloadStatus status;
    private double progress;
    private final DownloadConfig config;
    private String summary;
    private byte[] thumbnailData;
    private String speed;
    private String eta;

    public DownloadItem(String url, DownloadConfig config) {
        this.url = Objects.requireNonNull(url, LanguageManager.getInstance().get("error.null_url"));
        this.config = config;
        this.title = url;
        this.status = DownloadStatus.WAITING;
        this.progress = 0.0;
        this.summary = "";
        this.speed = "";
        this.eta = "";
    }

    public String getUrl() { return url; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public DownloadStatus getStatus() { return status; }
    public void setStatus(DownloadStatus status) { this.status = status; }

    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public byte[] getThumbnailData() { return thumbnailData; }
    public void setThumbnailData(byte[] thumbnailData) { this.thumbnailData = thumbnailData; }

    public String getSpeed() { return speed; }
    public void setSpeed(String speed) { this.speed = speed; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }

    public DownloadConfig getConfig() { return config; }

    public void cancel() {
        setStatus(DownloadStatus.CANCELLED);
    }

    public void pause() {
        setStatus(DownloadStatus.PAUSED);
    }

    public boolean canPause() {
        return getStatus() == DownloadStatus.FETCHING ||
                getStatus() == DownloadStatus.DOWNLOADING ||
                getStatus() == DownloadStatus.PROCESSING;
    }

    public boolean canResume() {
        return getStatus() == DownloadStatus.PAUSED ||
                getStatus() == DownloadStatus.CANCELLED ||
                getStatus() == DownloadStatus.ERROR;
    }

    public boolean canCancel() {
        return canPause() || getStatus() == DownloadStatus.WAITING;
    }
}
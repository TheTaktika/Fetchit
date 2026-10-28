package org.example.model;

import javafx.beans.property.*;
import org.example.ui.LanguageManager;

import java.util.Objects;

public class DownloadItem {
    private final StringProperty url = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty("");
    private final ObjectProperty<DownloadStatus> status = new SimpleObjectProperty<>(DownloadStatus.WAITING);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final DownloadConfig config;
    private final StringProperty summary = new SimpleStringProperty("");
    private final ObjectProperty<javafx.scene.image.Image> thumbnail = new SimpleObjectProperty<>();

    public DownloadItem(String url, DownloadConfig config) {
        this.url.set(Objects.requireNonNull(url, LanguageManager.getInstance().get("error.null_url")));
        this.config = config;
        this.title.set(url);
    }

    public String getUrl() { return url.get(); }
    @SuppressWarnings("unused")
    public StringProperty urlProperty() { return url; }

    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    public DownloadStatus getStatus() { return status.get(); }
    public ObjectProperty<DownloadStatus> statusProperty() { return status; }
    public void setStatus(DownloadStatus status) { this.status.set(status); }

    public double getProgress() { return progress.get(); }
    public DoubleProperty progressProperty() { return progress; }
    public void setProgress(double progress) { this.progress.set(progress); }

    public StringProperty summaryProperty() { return summary; }
    public String getSummary() { return summary.get(); }
    public void setSummary(String summary) { this.summary.set(summary); }

    public javafx.scene.image.Image getThumbnail() { return thumbnail.get(); }
    public ObjectProperty<javafx.scene.image.Image> thumbnailProperty() { return thumbnail; }
    public void setThumbnail(javafx.scene.image.Image thumbnail) { this.thumbnail.set(thumbnail); }

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
package org.example.model;

import javafx.beans.property.*;

public class DownloadItem {

    public enum Status {
        WAITING, FETCHING, DOWNLOADING, PROCESSING, DONE, ERROR, CANCELLED, PAUSED
    }

    private final StringProperty url = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty("");
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.WAITING);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final DownloadConfig config;
    private final StringProperty summary = new SimpleStringProperty("");
    private final ObjectProperty<javafx.scene.image.Image> thumbnail = new SimpleObjectProperty<>();

    public DownloadItem(String url, DownloadConfig config) {
        this.url.set(url);
        this.config = config;
        this.title.set(url);
    }

    @SuppressWarnings("unused")
    public String getUrl() { return url.get(); }
    @SuppressWarnings("unused")
    public StringProperty urlProperty() { return url; }

    @SuppressWarnings("unused")
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    public Status getStatus() { return status.get(); }
    public ObjectProperty<Status> statusProperty() { return status; }
    public void setStatus(Status status) { this.status.set(status); }

    @SuppressWarnings("unused")
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
        setStatus(Status.CANCELLED);
    }

    public void pause() {
        setStatus(Status.PAUSED);
    }
}
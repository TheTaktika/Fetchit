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

    private Process currentProcess;

    public DownloadItem(String url, DownloadConfig config) {
        this.url.set(url);
        this.config = config;
        this.title.set(url);
    }

    @SuppressWarnings("unused")
    public Process getCurrentProcess() { return currentProcess; }
    public void setCurrentProcess(Process process) { this.currentProcess = process; }

    @SuppressWarnings("unused")
    public String getUrl() { return url.get(); }
    @SuppressWarnings("unused")
    public StringProperty urlProperty() { return url; }

    @SuppressWarnings("unused")
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    @SuppressWarnings("unused")
    public void setTitle(String title) { this.title.set(title); }

    public Status getStatus() { return status.get(); }
    public ObjectProperty<Status> statusProperty() { return status; }
    public void setStatus(Status status) { this.status.set(status); }

    @SuppressWarnings("unused")
    public double getProgress() { return progress.get(); }
    public DoubleProperty progressProperty() { return progress; }
    public void setProgress(double progress) { this.progress.set(progress); }

    public DownloadConfig getConfig() { return config; }

    public void cancel() {
        setStatus(Status.CANCELLED);
        killProcess();
    }

    public void pause() {
        setStatus(Status.PAUSED);
        killProcess();
    }

    private void killProcess() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.descendants().forEach(ProcessHandle::destroyForcibly);
            currentProcess.destroyForcibly();
        }
    }
}
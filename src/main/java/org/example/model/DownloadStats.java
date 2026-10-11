package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class DownloadStats {

    private int playlistTotal = 0;
    private int alreadyOnDisk = 0;
    private int downloadedCount = 0;
    private int skippedCount = 0;
    private int errorsCount = 0;
    private final List<String> errorMessages = new ArrayList<>();

    private int totalItems = 0;
    private int completedItems = 0;

    public void setPlaylistTotal(int n) {
        this.playlistTotal = n;
    }

    public void setAlreadyOnDisk(int n) {
        this.alreadyOnDisk = n;
    }

    public int getPlaylistTotal() {
        return playlistTotal;
    }

    public int getAlreadyOnDisk() {
        return alreadyOnDisk;
    }

    public void incrementDownloaded() {
        downloadedCount++;
    }

    public void incrementSkipped() {
        skippedCount++;
    }

    public void incrementErrors() {
        errorsCount++;
    }

    public void addErrorMessage(String message) {
        errorMessages.add(message);
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public void incrementCompleted() {
        completedItems++;
    }

    public int getDownloadedCount() {
        return downloadedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getErrorsCount() {
        return errorsCount;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getCompletedItems() {
        return completedItems;
    }

    public List<String> getErrorMessages() {
        return new ArrayList<>(errorMessages);
    }

    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        if (getPlaylistTotal() > 0) {
            sb.append("Всего в плейлисте: ").append(getPlaylistTotal()).append("\n");
        }
        if (getAlreadyOnDisk() > 0) {
            sb.append("Уже было на диске: ").append(getAlreadyOnDisk()).append("\n");
        }
        sb.append("К загрузке: ").append(downloadedCount + skippedCount + errorsCount).append("\n");
        sb.append("Скачано: ").append(downloadedCount).append("\n");
        if (skippedCount > 0) {
            sb.append("Пропущено: ").append(skippedCount).append("\n");
        }
        sb.append("Ошибок: ").append(errorsCount).append("\n");

        if (!getErrorMessages().isEmpty()) {
            sb.append("\nОшибки:\n");
            for (String msg : getErrorMessages()) {
                sb.append("  • ").append(msg).append("\n");
            }
        }
        return sb.toString();
    }
}
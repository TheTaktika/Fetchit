package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class DownloadStats {

    private int downloadedCount = 0;
    private int skippedCount = 0;
    private int errorsCount = 0;
    private final List<String> errorMessages = new ArrayList<>();

    private int totalItems = 0;
    private int completedItems = 0;

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
        if (totalItems > 0) {
            sb.append("Всего в плейлисте: ").append(totalItems).append("\n");
        }
        sb.append("Скачано: ").append(downloadedCount).append("\n");
        sb.append("Пропущено (уже есть): ").append(skippedCount).append("\n");
        sb.append("Ошибок: ").append(errorsCount).append("\n");

        if (!errorMessages.isEmpty()) {
            sb.append("\nОшибки:\n");
            for (String msg : errorMessages) {
                sb.append("  • ").append(msg).append("\n");
            }
        }
        return sb.toString();
    }
}
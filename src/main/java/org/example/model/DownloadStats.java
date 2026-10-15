package org.example.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DownloadStats {

    private int playlistTotal = 0;
    private int alreadyOnDisk = 0;
    private int downloadedCount = 0;
    private int skippedCount = 0;
    private int errorsCount = 0;
    private final Map<ErrorType, List<String>> classifiedErrors = new LinkedHashMap<>();

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

    public void addError(ErrorType type, String rawMessage) {
        classifiedErrors
                .computeIfAbsent(type, k -> new ArrayList<>())
                .add(rawMessage);
    }

    public Map<ErrorType, List<String>> getClassifiedErrors() {
        return new LinkedHashMap<>(classifiedErrors);
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

    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        if (getPlaylistTotal() > 0) {
            sb.append("Всего в плейлисте: ").append(playlistTotal).append("\n");
        }
        if (getAlreadyOnDisk() > 0) {
            sb.append("Уже было на диске: ").append(alreadyOnDisk).append("\n");
        }
        sb.append("К загрузке: ").append(downloadedCount + skippedCount + errorsCount).append("\n");
        sb.append("Скачано: ").append(downloadedCount).append("\n");
        if (skippedCount > 0) {
            sb.append("Пропущено: ").append(skippedCount).append("\n");
        }
        sb.append("Ошибок: ").append(errorsCount).append("\n");

        if (!classifiedErrors.isEmpty()) {
            sb.append("\nОшибки:\n");
            for (Map.Entry<ErrorType, List<String>> entry : classifiedErrors.entrySet()) {
                ErrorType type = entry.getKey();
                int count = entry.getValue().size();
                sb.append("  • ").append(type.getUserMessage());
                if (count > 1) {
                    sb.append(" (x").append(count).append(")");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
package org.example.model;

import org.example.ui.LanguageManager;

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
            sb.append(LanguageManager.getInstance().get("status.playlist_total")).append(" ").append(getPlaylistTotal()).append("\n");
        }
        if (getAlreadyOnDisk() > 0) {
            sb.append(LanguageManager.getInstance().get("status.already_on_disk")).append(" ").append(alreadyOnDisk).append("\n");
        }
        sb.append(LanguageManager.getInstance().get("status.to_download")).append(" ").append(downloadedCount + skippedCount + errorsCount).append("\n");
        sb.append(LanguageManager.getInstance().get("status.downloaded")).append(" ").append(downloadedCount).append("\n");
        if (skippedCount > 0) {
            sb.append(LanguageManager.getInstance().get("status.skipped")).append(" ").append(skippedCount).append("\n");
        }
        sb.append(LanguageManager.getInstance().get("status.errors")).append(" ").append(errorsCount).append("\n");

        if (!classifiedErrors.isEmpty()) {
            sb.append("\n").append(LanguageManager.getInstance().get("status.error_details")).append("\n");
            for (Map.Entry<ErrorType, List<String>> entry : classifiedErrors.entrySet()) {
                ErrorType type = entry.getKey();
                int count = entry.getValue().size();
                String errorText = LanguageManager.getInstance().get(type.getMessageKey());

                sb.append("  • ").append(errorText);
                if (count > 1) {
                    sb.append(" (x").append(count).append(")");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
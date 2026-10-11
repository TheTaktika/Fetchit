package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class DownloadStats {

    private int downloadedCount = 0;
    private int skippedCount = 0;
    private int errorsCount = 0;
    private final List<String> errorMessages = new ArrayList<>();

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

    public int getDownloadedCount() {
        return downloadedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getErrorsCount() {
        return errorsCount;
    }

    public List<String> getErrorMessages() {
        return new ArrayList<>(errorMessages); // возвращаем копию
    }

    public String toSummary() {
        StringBuilder sb = new StringBuilder();
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
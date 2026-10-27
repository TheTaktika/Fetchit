package org.example.ui;

import org.example.model.DownloadStats;
import org.example.model.ErrorType;

import java.util.List;
import java.util.Map;

public class StatsFormatter {

    private StatsFormatter() {}

    public static String format(DownloadStats stats) {
        LanguageManager lang = LanguageManager.getInstance();
        StringBuilder sb = new StringBuilder();

        if (stats.getPlaylistTotal() > 0) {
            sb.append(lang.get("status.playlist_total")).append(" ").append(stats.getPlaylistTotal()).append("\n");
        }
        if (stats.getAlreadyOnDisk() > 0) {
            sb.append(lang.get("status.already_on_disk")).append(" ").append(stats.getAlreadyOnDisk()).append("\n");
        }

        int totalToDownload = stats.getDownloadedCount() + stats.getSkippedCount() + stats.getErrorsCount();
        sb.append(lang.get("status.to_download")).append(" ").append(totalToDownload).append("\n");
        sb.append(lang.get("status.downloaded")).append(" ").append(stats.getDownloadedCount()).append("\n");

        if (stats.getSkippedCount() > 0) {
            sb.append(lang.get("status.skipped")).append(" ").append(stats.getSkippedCount()).append("\n");
        }
        sb.append(lang.get("status.errors")).append(" ").append(stats.getErrorsCount()).append("\n");

        Map<ErrorType, List<String>> errors = stats.getClassifiedErrors();
        if (!errors.isEmpty()) {
            sb.append("\n").append(lang.get("status.error_details")).append("\n");
            for (Map.Entry<ErrorType, List<String>> entry : errors.entrySet()) {
                int count = entry.getValue().size();
                sb.append("  • ").append(lang.get(entry.getKey().getMessageKey()));
                if (count > 1) {
                    sb.append(" (x").append(count).append(")");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
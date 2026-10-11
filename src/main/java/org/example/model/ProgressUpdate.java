package org.example.model;

public record ProgressUpdate(
        double filePercent,
        int completedItems,
        int totalItems
) {
}
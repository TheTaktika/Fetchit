package org.example.parser;

import org.example.model.DownloadStats;
import org.example.model.ErrorType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputParser {

    private static final Pattern PROGRESS_PATTERN =
            Pattern.compile("\\[download]\\s+(\\d+\\.?\\d*)%");

    private static final Pattern ITEM_OF_PATTERN =
            Pattern.compile("Downloading item (\\d+) of (\\d+)");

    private static final Pattern ALREADY_DOWNLOADED_PATTERN =
            Pattern.compile("(.+) has already been downloaded");

    private static final Pattern ERROR_PATTERN =
            Pattern.compile("ERROR: (.+)");

    private final DownloadStats stats;
    private final ErrorClassifier errorClassifier;

    public OutputParser() {
        this(0);
    }

    public OutputParser(int initialTotalItems) {
        this.stats = new DownloadStats();
        this.stats.setTotalItems(initialTotalItems);
        this.errorClassifier = new ErrorClassifier();
    }

    public synchronized double parseLine(String line) {
        Matcher errorMatcher = ERROR_PATTERN.matcher(line);
        if (errorMatcher.find()) {
            String message = errorMatcher.group(1).trim();
            ErrorType type = errorClassifier.classify(message);
            stats.incrementErrors();
            stats.addError(type, message);
            return -1;
        }

        Matcher itemMatcher = ITEM_OF_PATTERN.matcher(line);
        if (itemMatcher.find()) {
            int total = Integer.parseInt(itemMatcher.group(2));
            if (stats.getTotalItems() == 0) {
                stats.setTotalItems(total);
            }
            return -1;
        }

        if (ALREADY_DOWNLOADED_PATTERN.matcher(line).find()) {
            stats.incrementSkipped();
            stats.incrementCompleted();
            return -1;
        }

        if (line.contains("[ExtractAudio] Destination:")) {
            stats.incrementDownloaded();
            stats.incrementCompleted();
            return -1;
        }

        Matcher progressMatcher = PROGRESS_PATTERN.matcher(line);
        if (progressMatcher.find()) {
            try {
                return Double.parseDouble(progressMatcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return -1;
    }

    public DownloadStats getStats() {
        return stats;
    }
}
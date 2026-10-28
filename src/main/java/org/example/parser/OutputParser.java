package org.example.parser;

import org.example.model.ErrorType;
import org.example.parser.event.ParseEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputParser {

    private static final Pattern PROGRESS_PATTERN = Pattern.compile("\\[download]\\s+(\\d+\\.?\\d*)%");
    private static final Pattern ITEM_OF_PATTERN = Pattern.compile("Downloading item (\\d+) of (\\d+)");
    private static final Pattern ALREADY_DOWNLOADED_PATTERN = Pattern.compile("(.+) has already been downloaded");
    private static final Pattern ERROR_PATTERN = Pattern.compile("ERROR: (.+)");

    private final ErrorClassifier errorClassifier;

    public OutputParser() {
        this.errorClassifier = new ErrorClassifier();
    }

    public ParseEvent parseLine(String line) {
        Matcher errorMatcher = ERROR_PATTERN.matcher(line);
        if (errorMatcher.find()) {
            String message = errorMatcher.group(1).trim();
            ErrorType type = errorClassifier.classify(message);
            return new ParseEvent.Error(type, message);
        }

        Matcher itemMatcher = ITEM_OF_PATTERN.matcher(line);
        if (itemMatcher.find()) {
            int total = Integer.parseInt(itemMatcher.group(2));
            return new ParseEvent.TotalItems(total);
        }

        if (ALREADY_DOWNLOADED_PATTERN.matcher(line).find()) {
            return new ParseEvent.Skipped();
        }

        if (line.contains("[ExtractAudio] Destination:") || line.contains("[Merger] Merging formats into")) {
            return new ParseEvent.Completed();
        }

        Matcher progressMatcher = PROGRESS_PATTERN.matcher(line);
        if (progressMatcher.find()) {
            try {
                double percent = Double.parseDouble(progressMatcher.group(1));
                return new ParseEvent.Progress(percent);
            } catch (NumberFormatException e) {
                return new ParseEvent.Ignored();
            }
        }

        return new ParseEvent.Ignored();
    }
}
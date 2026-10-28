package org.example.parser;

import org.example.model.ErrorType;

import java.util.List;

public class ErrorClassifier {

    private record Rule(ErrorType type, List<String> keywords) {}

    private static final List<Rule> RULES = List.of(
            new Rule(ErrorType.INVALID_URL, List.of(
                    "is not a valid url", "invalid url", "unsupported url", "failed to resolve",
                    "getaddrinfo failed", "name or service not known", "incomplete youtube id",
                    "looks truncated", "video id is invalid"
            )),
            new Rule(ErrorType.GEO_BLOCKED, List.of(
                    "not available in your country", "unavailable in your country",
                    "not available in your region", "unavailable in your region", "geo restricted"
            )),
            new Rule(ErrorType.PRIVATE_VIDEO, List.of("private video")),
            new Rule(ErrorType.RATE_LIMITED, List.of("http error 429", "too many requests")),
            new Rule(ErrorType.NETWORK_ERROR, List.of(
                    "unable to download video data", "read timed out", "connection reset",
                    "connection refused", "network is unreachable"
            )),
            new Rule(ErrorType.BOT_CHECK, List.of(
                    "sign in to confirm you're not a bot", "confirm you're not a bot"
            )),
            new Rule(ErrorType.VIDEO_UNAVAILABLE, List.of(
                    "this video is not available", "video unavailable", "video has been removed"
            )),
            new Rule(ErrorType.PLAYLIST_NOT_FOUND, List.of(
                    "playlist does not exist", "the playlist does not exist"
            )),
            new Rule(ErrorType.SIGN_IN_REQUIRED, List.of(
                    "sign in to confirm your age", "login required", "sign in if you've been granted access"
            ))
    );

    public ErrorType classify(String errorLine) {
        if (errorLine == null || errorLine.isBlank()) {
            return ErrorType.UNKNOWN;
        }

        String lower = errorLine.toLowerCase();

        return RULES.stream()
                .filter(rule -> rule.keywords().stream().anyMatch(lower::contains))
                .map(Rule::type)
                .findFirst()
                .orElse(ErrorType.UNKNOWN);
    }
}
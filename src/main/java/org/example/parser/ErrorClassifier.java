package org.example.parser;

import org.example.model.ErrorType;

public class ErrorClassifier {

    public ErrorType classify(String errorLine) {
        if (errorLine == null || errorLine.isBlank()) {
            return ErrorType.UNKNOWN;
        }

        String lower = errorLine.toLowerCase();

        if (lower.contains("is not a valid url")
                || lower.contains("invalid url")
                || lower.contains("unsupported url")
                || lower.contains("failed to resolve")
                || lower.contains("getaddrinfo failed")
                || lower.contains("name or service not known")) {
            return ErrorType.INVALID_URL;
        }

        if (lower.contains("not available in your country")
                || lower.contains("unavailable in your country")
                || lower.contains("not available in your region")
                || lower.contains("unavailable in your region")
                || lower.contains("geo restricted")) {
            return ErrorType.GEO_BLOCKED;
        }

        if (lower.contains("private video")) {
            return ErrorType.PRIVATE_VIDEO;
        }

        if (lower.contains("http error 429")
                || lower.contains("too many requests")) {
            return ErrorType.RATE_LIMITED;
        }

        if (lower.contains("incomplete youtube id")
                || lower.contains("looks truncated")
                || lower.contains("video id is invalid")) {
            return ErrorType.INVALID_URL;
        }

        if (lower.contains("unable to download video data")
                || lower.contains("read timed out")
                || lower.contains("connection reset")
                || lower.contains("connection refused")
                || lower.contains("network is unreachable")) {
            return ErrorType.NETWORK_ERROR;
        }

        if (lower.contains("sign in to confirm you're not a bot")
                || lower.contains("confirm you're not a bot")) {
            return ErrorType.BOT_CHECK;
        }

        if (lower.contains("this video is not available")
                || lower.contains("video unavailable")
                || lower.contains("video has been removed")) {
            return ErrorType.VIDEO_UNAVAILABLE;
        }

        if (lower.contains("playlist does not exist")
                || lower.contains("the playlist does not exist")) {
            return ErrorType.PLAYLIST_NOT_FOUND;
        }

        if (lower.contains("sign in to confirm your age")
                || lower.contains("login required")
                || lower.contains("sign in if you've been granted access")) {
            return ErrorType.SIGN_IN_REQUIRED;
        }

        return ErrorType.UNKNOWN;
    }
}
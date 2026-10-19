package org.example.model;

public enum ErrorType {
    INVALID_URL("error.invalid_url"),
    GEO_BLOCKED("error.geo_blocked"),
    PRIVATE_VIDEO("error.private_video"),
    RATE_LIMITED("error.rate_limited"),
    NETWORK_ERROR("error.network"),
    BOT_CHECK("error.bot_check"),
    VIDEO_UNAVAILABLE("error.video_unavailable"),
    PLAYLIST_NOT_FOUND("error.playlist_not_found"),
    SIGN_IN_REQUIRED("error.sign_in"),
    UNKNOWN("error.unknown");

    private final String messageKey;

    ErrorType(String key) {
        this.messageKey = key;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
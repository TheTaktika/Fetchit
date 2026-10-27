package org.example.model;

import java.util.List;

public record PlaylistFetchResult(List<String> ids, ErrorType error, String title, String thumbnail) {

    public static PlaylistFetchResult success(List<String> ids, String title, String thumbnail) {
        return new PlaylistFetchResult(ids, null, title, thumbnail);
    }

    public static PlaylistFetchResult failure(ErrorType error) {
        return new PlaylistFetchResult(List.of(), error, null, null);
    }

    public boolean isSuccess() {
        return error == null;
    }
}
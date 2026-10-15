package org.example.model;

import java.util.List;

public record PlaylistFetchResult(List<String> ids, ErrorType error) {

    public static PlaylistFetchResult success(List<String> ids) {
        return new PlaylistFetchResult(ids, null);
    }

    public static PlaylistFetchResult failure(ErrorType error) {
        return new PlaylistFetchResult(List.of(), error);
    }

    public boolean isSuccess() {
        return error == null;
    }
}
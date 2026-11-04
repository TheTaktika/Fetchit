package org.example.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class TempManager {

    private final YtDlpPathResolver pathResolver;

    public TempManager(YtDlpPathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    public void clearCache() throws IOException {
        File tempDir = pathResolver.getTempDir();
        if (!tempDir.exists()) {
            return;
        }

        try (Stream<Path> walk = Files.walk(tempDir.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }
}
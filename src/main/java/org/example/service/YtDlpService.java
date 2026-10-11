package org.example.service;

import java.nio.file.Files;
import java.nio.file.Path;
import org.example.model.DownloadConfig;
import org.example.model.DownloadStats;
import org.example.model.ProgressUpdate;
import org.example.parser.OutputParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class YtDlpService {

    public int update(Consumer<String> onOutput) {
        try {
            ProcessBuilder pb = new ProcessBuilder("yt-dlp", "-U");
            Process process = pb.start();

            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        onOutput.accept("[OUT] " + line);
                    }
                } catch (Exception e) {
                    onOutput.accept("[ERR] " + e.getMessage());
                }
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        onOutput.accept("[ERR] " + line);
                    }
                } catch (Exception e) {
                    onOutput.accept("[ERR] " + e.getMessage());
                }
            });

            stdoutThread.start();
            stderrThread.start();

            int exitCode = process.waitFor();
            stdoutThread.join();
            stderrThread.join();

            return exitCode;

        } catch (Exception e) {
            onOutput.accept("[FATAL] " + e.getMessage());
            return -1;
        }
    }

    private void handleParsedLine(OutputParser parser, String line, Consumer<ProgressUpdate> onProgress) {
        double percent = parser.parseLine(line);
        if (percent >= 0) {
            DownloadStats stats = parser.getStats();
            onProgress.accept(new ProgressUpdate(
                    percent,
                    stats.getCompletedItems(),
                    stats.getTotalItems()
            ));
        }
    }

    private String resolveBundledTool(String toolName) {
        // TODO: искать бинарник в папке bundle/ рядом с приложением
        return toolName;
    }

    public List<String> getPlaylistVideoIds(String url, Consumer<String> onOutput) {
        List<String> ids = new ArrayList<>();
        try {
            List<String> command = new ArrayList<>();
            command.add(resolveBundledTool("yt-dlp.exe"));
            command.add("--flat-playlist");
            command.add("--print");
            command.add("%(id)s");
            command.add(url);

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        ids.add(trimmed);
                    }
                }
            }

            try (BufferedReader ignored = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                ignored.lines().forEach(l -> {});
            }

            process.waitFor();

        } catch (Exception e) {
            onOutput.accept("[WARN] Не удалось получить список видео: " + e.getMessage());
        }
        return ids;
    }

    public DownloadStats downloadByIds(DownloadConfig config,
                                       List<String> videoIds,
                                       Consumer<String> onOutput,
                                       Consumer<ProgressUpdate> onProgress) {
        if (videoIds.isEmpty()) {
            return new DownloadStats();
        }

        try {
            Path tempFile = Files.createTempFile("fetchit-urls-", ".txt");
            List<String> urls = videoIds.stream()
                    .map(id -> "https://www.youtube.com/watch?v=" + id)
                    .toList();
            Files.write(tempFile, urls);

            DownloadConfig listConfig = new DownloadConfig(
                    tempFile.toAbsolutePath().toString(),
                    config.getOutputDir(),
                    config.getProxy()
            );

            DownloadStats stats = downloadFromFile(listConfig, videoIds.size(), onOutput, onProgress);
            stats.setTotalItems(videoIds.size());

            Files.deleteIfExists(tempFile);
            return stats;

        } catch (Exception e) {
            onOutput.accept("[FATAL] Не удалось создать список URL: " + e.getMessage());
            return new DownloadStats();
        }
    }

    private DownloadStats downloadFromFile(DownloadConfig config,
                                           int expectedTotal,
                                           Consumer<String> onOutput,
                                           Consumer<ProgressUpdate> onProgress) {
        OutputParser parser = new OutputParser(expectedTotal);
        try {
            List<String> command = new ArrayList<>();
            command.add(resolveBundledTool("yt-dlp.exe"));

            if (config.hasProxy()) {
                command.add("--proxy");
                command.add(config.getProxy());
            }

            command.add("--newline");
            command.add("-a");
            command.add(config.getUrl());
            command.add("-x");
            command.add("--audio-format");
            command.add("mp3");
            command.add("--audio-quality");
            command.add("0");
            command.add("--embed-thumbnail");
            command.add("--add-metadata");
            command.add("--parse-metadata");
            command.add("%(id)s:%(meta_comment)s");
            command.add("-o");
            command.add(config.getOutputDir() + "/%(title)s.%(ext)s");

            onOutput.accept("[CMD] " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        onOutput.accept("[OUT] " + line);
                        handleParsedLine(parser, line, onProgress);
                    }
                } catch (Exception e) {
                    onOutput.accept("[ERR] " + e.getMessage());
                }
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        onOutput.accept("[ERR] " + line);
                        handleParsedLine(parser, line, onProgress);
                    }
                } catch (Exception e) {
                    onOutput.accept("[ERR] " + e.getMessage());
                }
            });

            stdoutThread.start();
            stderrThread.start();
            process.waitFor();
            stdoutThread.join();
            stderrThread.join();

        } catch (Exception e) {
            onOutput.accept("[FATAL] " + e.getMessage());
        }

        return parser.getStats();
    }
}
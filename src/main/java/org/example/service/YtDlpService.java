package org.example.service;

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

    public DownloadStats download(DownloadConfig config,
                                  Consumer<String> onOutput,
                                  Consumer<ProgressUpdate> onProgress) {
        OutputParser parser = new OutputParser();

        try {
            List<String> command = new ArrayList<>();
            command.add(resolveBundledTool("yt-dlp.exe"));

            if (config.hasProxy()) {
                command.add("--proxy");
                command.add(config.getProxy());
            }

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
            command.add(config.getUrl());

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
                    onOutput.accept("[ERR] Ошибка чтения stdout: " + e.getMessage());
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
                    onOutput.accept("[ERR] Ошибка чтения stderr: " + e.getMessage());
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

    public int getPlaylistItemCount(String url, Consumer<String> onOutput) {
        try {
            List<String> command = new ArrayList<>();
            command.add(resolveBundledTool("yt-dlp.exe"));
            command.add("--flat-playlist");
            command.add("--print");
            command.add("%(id)s");
            command.add(url);

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            int count = 0;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        count++;
                    }
                }
            }
            try (BufferedReader ignored = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                while (ignored.readLine() != null) { /* ничего */ }
            }
            process.waitFor();
            return count > 0 ? count : -1;
        } catch (Exception e) {
            onOutput.accept("[WARN] Не удалось определить размер плейлиста: " + e.getMessage());
            return -1;
        }
    }
}
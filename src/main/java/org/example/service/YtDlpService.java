package org.example.service;

import org.example.model.DownloadConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class YtDlpService {

    public int download(DownloadConfig config, Consumer<String> onOutput) {
        try {
            List<String> command = new ArrayList<>();
            command.add("yt-dlp");

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
                    }
                } catch (Exception e) {
                    onOutput.accept("[ERR] Ошибка чтения stderr: " + e.getMessage());
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
}
package org.example.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class YtDlpService {

    /**
     * Запускает скачивание через yt-dlp.
     *
     * @param url       ссылка на видео или плейлист
     * @param outputDir папка для сохранения
     * @param onOutput  callback для каждой строки вывода (для UI)
     * @return код возврата процесса
     */

    public int download(String url, String outputDir, Consumer<String> onOutput) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "yt-dlp",
                    "-x",
                    "--audio-format", "mp3",
                    "--audio-quality", "0",
                    "--embed-thumbnail",
                    "--add-metadata",
                    "--parse-metadata", "%(id)s:%(meta_comment)s",
                    "-o", outputDir + "/%(title)s.%(ext)s",
                    url
            );

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
}
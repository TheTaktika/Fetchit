package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "yt-dlp",
                    "-x",
                    "--audio-format", "mp3",
                    "--audio-quality", "0",
                    "--embed-thumbnail",
                    "--add-metadata",
                    "--parse-metadata", "%(id)s:%(meta_comment)s",
                    "-o", "test_output/%(title)s.%(ext)s",
                    "https://youtube.com/playlist"
            );
            Process process = pb.start();

            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[OUT] " + line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("[ERR] " + line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            stdoutThread.start();
            stderrThread.start();

            int exitCode = process.waitFor();
            stdoutThread.join();
            stderrThread.join();

            System.out.println("Процесс завершён с кодом: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
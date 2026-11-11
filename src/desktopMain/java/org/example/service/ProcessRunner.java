package org.example.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

public class ProcessRunner {

    public int run(List<String> command, Consumer<String> stdoutConsumer, Consumer<String> stderrConsumer, Consumer<Process> onProcessStarted) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            Process process = pb.start();

            if (onProcessStarted != null) {
                onProcessStarted.accept(process);
            }

            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdoutConsumer.accept(line);
                    }
                } catch (Exception e) {
                    stderrConsumer.accept(e.getMessage());
                }
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderrConsumer.accept(line);
                    }
                } catch (Exception e) {
                    stderrConsumer.accept(e.getMessage());
                }
            });

            stdoutThread.start();
            stderrThread.start();

            int exitCode = process.waitFor();
            stdoutThread.join();
            stderrThread.join();
            return exitCode;
        } catch (Exception e) {
            stderrConsumer.accept("Process execution failed: " + e.getMessage());
            return -1;
        }
    }
}
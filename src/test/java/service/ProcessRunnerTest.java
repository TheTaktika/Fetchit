package service;

import org.example.service.ProcessRunner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProcessRunnerTest {

    @Test
    void shouldExecuteProcessAndCaptureOutput() {
        ProcessRunner runner = new ProcessRunner();
        List<String> outputLines = new ArrayList<>();

        String os = System.getProperty("os.name").toLowerCase();
        List<String> command = os.contains("win")
                ? List.of("cmd.exe", "/c", "echo Hello FetchIt")
                : List.of("echo", "Hello FetchIt");

        int exitCode = runner.run(
                command,
                outputLines::add,
                System.err::println,
                null
        );

        assertEquals(0, exitCode);
        assertFalse(outputLines.isEmpty());
        assertEquals("Hello FetchIt", outputLines.get(0).trim());
    }
}
package org.example.service;

public class ProcessKiller {

    public void kill(Process process) {
        if (process != null && process.isAlive()) {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                try {
                    new ProcessBuilder("taskkill", "/f", "/t", "/pid", String.valueOf(process.pid()))
                            .start().waitFor();
                } catch (Exception e) {
                    process.destroyForcibly();
                }
            } else {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
            }
        }
    }
}
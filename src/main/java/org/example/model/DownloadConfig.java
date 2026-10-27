package org.example.model;

public record DownloadConfig(String url, String outputDir, String proxy, Preset preset) {

    public DownloadConfig {
        preset = (preset != null) ? preset : Preset.defaultAudioPreset();
        proxy = (proxy == null || proxy.isBlank()) ? null : proxy.trim();
    }

    public boolean hasProxy() {
        return proxy != null;
    }
}
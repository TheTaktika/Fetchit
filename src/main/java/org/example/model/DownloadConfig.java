package org.example.model;

import org.example.ui.LanguageManager;

import java.util.Objects;

public record DownloadConfig(String url, String outputDir, String proxy, Preset preset) {

    public DownloadConfig {
        Objects.requireNonNull(url, LanguageManager.getInstance().get("error.null_url"));
        preset = (preset != null) ? preset : Preset.defaultAudioPreset();
        proxy = (proxy == null || proxy.isBlank()) ? null : proxy.trim();
    }

    public boolean hasProxy() {
        return proxy != null;
    }
}
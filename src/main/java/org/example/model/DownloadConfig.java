package org.example.model;

@SuppressWarnings("ClassCanBeRecord")
public class DownloadConfig {

    private final String url;
    private final String outputDir;
    private final String proxy;
    private final Preset preset;

    public DownloadConfig(String url, String outputDir, String proxy, Preset preset) {
        this.url = url;
        this.outputDir = outputDir;
        this.proxy = proxy;
        this.preset = preset != null ? preset : Preset.defaultAudioPreset();
    }

    public String getUrl() {
        return url;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getProxy() {
        return (proxy == null || proxy.isBlank()) ? null : proxy.trim();
    }

    public boolean hasProxy() {
        return getProxy() != null;
    }

    public Preset getPreset() {
        return preset;
    }
}
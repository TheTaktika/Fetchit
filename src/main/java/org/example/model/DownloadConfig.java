package org.example.model;

public class DownloadConfig {

    private final String url;
    private final String outputDir;
    private final String proxy;

    public DownloadConfig(String url, String outputDir, String proxy) {
        this.url = url;
        this.outputDir = outputDir;
        this.proxy = proxy;
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
}
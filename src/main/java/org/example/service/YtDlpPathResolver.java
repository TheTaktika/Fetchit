package org.example.service;

import java.io.File;

public class YtDlpPathResolver {

    public String resolveBundledTool(String toolName) {
        File bundleDir = resolveBundleDir();
        if (bundleDir != null) {
            File tool = new File(bundleDir, toolName);
            if (tool.exists() && tool.canExecute()) {
                return tool.getAbsolutePath();
            }
        }
        return toolName;
    }

    public String resolveFfmpegLocation() {
        File bundleDir = resolveBundleDir();
        if (bundleDir != null) {
            File ffmpeg = new File(bundleDir, "ffmpeg.exe");
            if (ffmpeg.exists()) {
                return bundleDir.getAbsolutePath();
            }
        }
        return null;
    }

    private File resolveBundleDir() {
        try {
            File codeSource = new File(
                    YtDlpPathResolver.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI()
            );
            File appDir = codeSource.getParentFile();
            File rootDir = appDir.getParentFile();
            File bundle = new File(rootDir, "bundle");
            if (bundle.exists() && bundle.isDirectory()) {
                return bundle;
            }
        } catch (Exception ignored) {
        }

        File devBundle = new File("bundle");
        if (devBundle.exists() && devBundle.isDirectory()) {
            return devBundle;
        }

        return null;
    }
}
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
        String composeResourcesDir = System.getProperty("compose.application.resources.dir");
        if (composeResourcesDir != null) {
            File resDir = new File(composeResourcesDir);
            if (resDir.exists() && resDir.isDirectory()) {
                return resDir;
            }
        }

        File devBundle = new File("bundle/common");
        if (devBundle.exists() && devBundle.isDirectory()) {
            return devBundle;
        }

        try {
            File codeSource = new File(
                    YtDlpPathResolver.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI()
            );
            File appDir = codeSource.getParentFile();
            File resourcesDir = new File(appDir, "resources");
            if (resourcesDir.exists() && resourcesDir.isDirectory()) {
                return resourcesDir;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public java.io.File getTempDir() {
        return new java.io.File(System.getProperty("java.io.tmpdir"), "FetchIt_temp");
    }
}
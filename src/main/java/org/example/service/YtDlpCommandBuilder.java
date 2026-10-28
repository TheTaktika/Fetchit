package org.example.service;

import org.example.model.DownloadConfig;
import org.example.model.Preset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class YtDlpCommandBuilder {

    private final YtDlpPathResolver pathResolver;

    public YtDlpCommandBuilder(YtDlpPathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    public List<String> buildUpdateCommand() {
        List<String> command = new ArrayList<>();
        command.add(pathResolver.resolveBundledTool("yt-dlp.exe"));
        command.add("-U");
        return command;
    }

    public List<String> buildPlaylistCommand(DownloadConfig config) {
        List<String> command = new ArrayList<>();
        command.add(pathResolver.resolveBundledTool("yt-dlp.exe"));

        if (config.hasProxy()) {
            command.add("--proxy");
            command.add(config.proxy());
        }

        Preset preset = config.preset();
        if (preset.getCookiesFromBrowser() != null && !preset.getCookiesFromBrowser().isBlank()) {
            command.add("--cookies-from-browser");
            command.add(preset.getCookiesFromBrowser());
        }

        command.add("--flat-playlist");
        command.add("--print"); command.add("%(id)s");
        command.add("--print"); command.add("TITLE:%(playlist_title,title)s");
        command.add("--print"); command.add("PLAYLIST_THUMB:%(playlist_thumbnail)s");
        command.add("--print"); command.add("FIRST_VIDEO_THUMB:%(thumbnail)s");
        command.add(config.url());

        return command;
    }

    public List<String> buildDownloadCommand(DownloadConfig config) {
        List<String> command = new ArrayList<>();
        command.add(pathResolver.resolveBundledTool("yt-dlp.exe"));

        if (config.hasProxy()) {
            command.add("--proxy");
            command.add(config.proxy());
        }

        Preset preset = config.preset();

        command.add("--newline");
        command.add("--no-quiet");
        command.add("--encoding"); command.add("utf-8");
        command.add("--print"); command.add("before_dl:TITLE:%(title)s");
        command.add("--print"); command.add("before_dl:THUMB:%(thumbnail)s");
        command.add("-a"); command.add(config.url());

        if (preset.isAudio()) {
            command.add("-x");
            command.add("--audio-format"); command.add(preset.getAudioFormat());
            command.add("--audio-quality"); command.add(String.valueOf(preset.getAudioQuality()));
            if (preset.isKeepVideo()) command.add("-k");
        } else {
            String vq = preset.getVideoQuality();
            command.add("-f");
            if (vq == null || "best".equalsIgnoreCase(vq)) {
                command.add("bestvideo+bestaudio/best");
            } else {
                String height = vq.replace("p", "").trim();
                command.add("bestvideo[height<=" + height + "]+bestaudio/best[height<=" + height + "]");
            }
            if (preset.getMergeOutputFormat() != null && !preset.getMergeOutputFormat().isBlank()) {
                command.add("--merge-output-format"); command.add(preset.getMergeOutputFormat());
            }
            if (preset.getRecodeVideo() != null && !preset.getRecodeVideo().isBlank()) {
                command.add("--recode-video"); command.add(preset.getRecodeVideo());
            }
        }

        if (preset.isEmbedThumbnail()) command.add("--embed-thumbnail");
        if (preset.isAddMetadata()) command.add("--add-metadata");
        if (preset.isEmbedChapters()) command.add("--embed-chapters");

        if (preset.isWriteSubtitles()) {
            command.add("--write-subs");
            if (preset.getSubtitleLanguages() != null && !preset.getSubtitleLanguages().isBlank()) {
                command.add("--sub-langs"); command.add(preset.getSubtitleLanguages());
            }
        }
        if (preset.isEmbedSubtitles()) {
            command.add("--embed-subs");
            if (preset.getSubtitleLanguages() != null && !preset.getSubtitleLanguages().isBlank()) {
                command.add("--sub-langs"); command.add(preset.getSubtitleLanguages());
            }
        }

        if (preset.getSponsorblockRemove() != null && !preset.getSponsorblockRemove().isBlank()) {
            command.add("--sponsorblock-remove"); command.add(preset.getSponsorblockRemove());
        } else if (preset.getSponsorblockMark() != null && !preset.getSponsorblockMark().isBlank()) {
            command.add("--sponsorblock-mark"); command.add(preset.getSponsorblockMark());
        }

        if (preset.getConcurrentFragments() > 1) {
            command.add("--concurrent-fragments"); command.add(String.valueOf(preset.getConcurrentFragments()));
        }
        if (preset.getLimitRate() != null && !preset.getLimitRate().isBlank()) {
            command.add("--limit-rate"); command.add(preset.getLimitRate());
        }
        if (preset.getRetries() > 0) {
            command.add("--retries"); command.add(String.valueOf(preset.getRetries()));
        }
        if (preset.isNoOverwrites()) command.add("--no-overwrites");

        String ffmpegLocation = pathResolver.resolveFfmpegLocation();
        if (ffmpegLocation != null) {
            command.add("--ffmpeg-location"); command.add(ffmpegLocation);
        }

        command.add("--parse-metadata");
        command.add("%(id)s:%(meta_comment)s");

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "FetchIt_temp");
        File outputDir = new File(config.outputDir());

        if (!tempDir.exists()) tempDir.mkdirs();
        if (!outputDir.exists()) outputDir.mkdirs();

        command.add("-P"); command.add("temp:" + tempDir.getAbsolutePath());
        command.add("-P"); command.add("home:" + outputDir.getAbsolutePath());

        if (preset.isSplitChapters()) {
            command.add("--split-chapters");
            command.add("-o"); command.add("%(title)s.%(ext)s");
            command.add("-o"); command.add("chapter:%(section_title)s.%(ext)s");
        } else {
            command.add("-o"); command.add("%(title)s.%(ext)s");
        }

        return command;
    }
}
package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Preset {

    private String name;

    private String format;

    private String audioFormat;
    private int audioQuality;

    private String videoQuality;
    private String mergeOutputFormat;
    private String recodeVideo;

    private boolean embedThumbnail;
    private boolean addMetadata;
    private boolean embedSubtitles;
    private boolean writeSubtitles;
    private boolean embedChapters;
    private String subtitleLanguages;

    private int concurrentFragments;
    private String limitRate;
    private int retries;

    private String sponsorblockRemove;
    private String sponsorblockMark;

    private boolean keepVideo;
    private boolean noOverwrites;

    public Preset() {
    }

    public static Preset defaultAudioPreset() {
        Preset p = new Preset();
        p.name = "MP3 High Quality";
        p.format = "audio";
        p.audioFormat = "mp3";
        p.audioQuality = 0;
        p.embedThumbnail = true;
        p.addMetadata = true;
        p.concurrentFragments = 1;
        p.retries = 10;
        p.noOverwrites = true;
        return p;
    }

    public static Preset defaultVideoPreset() {
        Preset p = new Preset();
        p.name = "Video 1080p MP4";
        p.format = "video";
        p.videoQuality = "1080p";
        p.mergeOutputFormat = "mp4";
        p.addMetadata = true;
        p.concurrentFragments = 1;
        p.retries = 10;
        p.noOverwrites = true;
        return p;
    }

    @JsonIgnore
    public boolean isAudio() {
        return "audio".equalsIgnoreCase(format);
    }

    @JsonIgnore
    public boolean isVideo() {
        return "video".equalsIgnoreCase(format);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(String audioFormat) {
        this.audioFormat = audioFormat;
    }

    public int getAudioQuality() {
        return audioQuality;
    }

    public void setAudioQuality(int audioQuality) {
        this.audioQuality = audioQuality;
    }

    public String getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(String videoQuality) {
        this.videoQuality = videoQuality;
    }

    public String getMergeOutputFormat() {
        return mergeOutputFormat;
    }

    public void setMergeOutputFormat(String mergeOutputFormat) {
        this.mergeOutputFormat = mergeOutputFormat;
    }

    public String getRecodeVideo() {
        return recodeVideo;
    }

    @SuppressWarnings("unused")
    public void setRecodeVideo(String recodeVideo) {
        this.recodeVideo = recodeVideo;
    }

    public boolean isEmbedThumbnail() {
        return embedThumbnail;
    }

    public void setEmbedThumbnail(boolean embedThumbnail) {
        this.embedThumbnail = embedThumbnail;
    }

    public boolean isAddMetadata() {
        return addMetadata;
    }

    public void setAddMetadata(boolean addMetadata) {
        this.addMetadata = addMetadata;
    }

    public boolean isEmbedSubtitles() {
        return embedSubtitles;
    }

    public void setEmbedSubtitles(boolean embedSubtitles) {
        this.embedSubtitles = embedSubtitles;
    }

    public boolean isWriteSubtitles() {
        return writeSubtitles;
    }

    public void setWriteSubtitles(boolean writeSubtitles) {
        this.writeSubtitles = writeSubtitles;
    }

    public boolean isEmbedChapters() {
        return embedChapters;
    }

    public void setEmbedChapters(boolean embedChapters) {
        this.embedChapters = embedChapters;
    }

    public String getSubtitleLanguages() {
        return subtitleLanguages;
    }

    public void setSubtitleLanguages(String subtitleLanguages) {
        this.subtitleLanguages = subtitleLanguages;
    }

    public int getConcurrentFragments() {
        return concurrentFragments;
    }

    public void setConcurrentFragments(int concurrentFragments) {
        this.concurrentFragments = concurrentFragments;
    }

    public String getLimitRate() {
        return limitRate;
    }

    public void setLimitRate(String limitRate) {
        this.limitRate = limitRate;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getSponsorblockRemove() {
        return sponsorblockRemove;
    }

    public void setSponsorblockRemove(String sponsorblockRemove) {
        this.sponsorblockRemove = sponsorblockRemove;
    }

    public String getSponsorblockMark() {
        return sponsorblockMark;
    }

    @SuppressWarnings("unused")
    public void setSponsorblockMark(String sponsorblockMark) {
        this.sponsorblockMark = sponsorblockMark;
    }

    public boolean isKeepVideo() {
        return keepVideo;
    }

    public void setKeepVideo(boolean keepVideo) {
        this.keepVideo = keepVideo;
    }

    public boolean isNoOverwrites() {
        return noOverwrites;
    }

    public void setNoOverwrites(boolean noOverwrites) {
        this.noOverwrites = noOverwrites;
    }
}
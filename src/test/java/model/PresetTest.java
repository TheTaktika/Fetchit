package model;

import org.example.model.Preset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PresetTest {

    @Test
    void defaultAudioPreset_hasCorrectDefaults() {
        Preset p = Preset.defaultAudioPreset();
        assertEquals("MP3 High Quality", p.getName());
        assertEquals("audio", p.getFormat());
        assertEquals("mp3", p.getAudioFormat());
        assertEquals(0, p.getAudioQuality());
        assertTrue(p.isEmbedThumbnail());
        assertTrue(p.isAddMetadata());
        assertEquals(1, p.getConcurrentFragments());
        assertEquals(10, p.getRetries());
        assertTrue(p.isNoOverwrites());
    }

    @Test
    void defaultVideoPreset_hasCorrectDefaults() {
        Preset p = Preset.defaultVideoPreset();
        assertEquals("Video 1080p MP4", p.getName());
        assertEquals("video", p.getFormat());
        assertEquals("1080p", p.getVideoQuality());
        assertEquals("mp4", p.getMergeOutputFormat());
        assertFalse(p.isEmbedThumbnail());
        assertTrue(p.isAddMetadata());
    }

    @Test
    void isAudio_returnsTrueForAudioFormat() {
        Preset p = Preset.defaultAudioPreset();
        assertTrue(p.isAudio());
        assertFalse(p.isVideo());
    }

    @Test
    void isVideo_returnsTrueForVideoFormat() {
        Preset p = Preset.defaultVideoPreset();
        assertTrue(p.isVideo());
        assertFalse(p.isAudio());
    }

    @Test
    void isAudio_isCaseInsensitive() {
        Preset p = new Preset();
        p.setFormat("AUDIO");
        assertTrue(p.isAudio());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        Preset p = new Preset();
        p.setName("Test");
        p.setFormat("audio");
        p.setAudioFormat("flac");
        p.setAudioQuality(5);
        p.setConcurrentFragments(4);
        p.setRetries(20);
        p.setLimitRate("5M");
        p.setSponsorblockRemove("sponsor");

        assertEquals("Test", p.getName());
        assertEquals("audio", p.getFormat());
        assertEquals("flac", p.getAudioFormat());
        assertEquals(5, p.getAudioQuality());
        assertEquals(4, p.getConcurrentFragments());
        assertEquals(20, p.getRetries());
        assertEquals("5M", p.getLimitRate());
        assertEquals("sponsor", p.getSponsorblockRemove());
    }
}
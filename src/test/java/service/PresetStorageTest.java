package service;

import org.example.model.Preset;
import org.example.service.PresetStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PresetStorageTest {

    @TempDir
    Path tempDir;

    private File testFile;
    private PresetStorage storage;

    @BeforeEach
    void setUp() {
        testFile = tempDir.resolve("test-presets.json").toFile();
        storage = new PresetStorage(testFile);
    }

    @Test
    void load_whenFileDoesNotExist_createsDefaults() {
        assertFalse(testFile.exists());
        List<Preset> presets = storage.load();
        assertEquals(2, presets.size());
        assertTrue(testFile.exists());
    }

    @Test
    void saveAndLoad_roundTrip_preservesData() {
        Preset p = new Preset();
        p.setName("My Custom");
        p.setFormat("audio");
        p.setAudioFormat("flac");
        p.setAudioQuality(3);
        p.setConcurrentFragments(4);
        p.setRetries(15);
        p.setLimitRate("5M");

        storage.save(List.of(p));
        List<Preset> loaded = storage.load();

        assertEquals(1, loaded.size());
        Preset loadedPreset = loaded.get(0);
        assertEquals("My Custom", loadedPreset.getName());
        assertEquals("flac", loadedPreset.getAudioFormat());
        assertEquals(3, loadedPreset.getAudioQuality());
        assertEquals(4, loadedPreset.getConcurrentFragments());
        assertEquals(15, loadedPreset.getRetries());
        assertEquals("5M", loadedPreset.getLimitRate());
    }

    @Test
    void load_normalizesZeroConcurrentFragments() {
        Preset p = new Preset();
        p.setName("Broken");
        p.setConcurrentFragments(0);
        p.setRetries(0);
        storage.save(List.of(p));

        List<Preset> loaded = storage.load();
        assertEquals(1, loaded.size());
        assertEquals(1, loaded.get(0).getConcurrentFragments());
        assertEquals(10, loaded.get(0).getRetries());
    }

    @Test
    void load_normalizesNullAudioFormat() {
        Preset p = new Preset();
        p.setName("No format");
        p.setAudioFormat(null);
        p.setMergeOutputFormat(null);
        storage.save(List.of(p));

        List<Preset> loaded = storage.load();
        assertEquals("mp3", loaded.get(0).getAudioFormat());
        assertEquals("mp4", loaded.get(0).getMergeOutputFormat());
    }

    @Test
    void load_whenFileCorrupted_returnsDefaults() {
        try {
            java.nio.file.Files.writeString(testFile.toPath(), "not a valid json");
        } catch (Exception e) {
            fail("Не удалось подготовить тестовый файл");
        }

        List<Preset> presets = storage.load();
        assertEquals(2, presets.size());
        assertEquals("MP3 High Quality", presets.get(0).getName());
    }

    @Test
    void save_multiplePresets_persistsAll() {
        List<Preset> original = List.of(
                Preset.defaultAudioPreset(),
                Preset.defaultVideoPreset()
        );
        storage.save(original);

        List<Preset> loaded = storage.load();
        assertEquals(2, loaded.size());
        assertEquals("MP3 High Quality", loaded.get(0).getName());
        assertEquals("Video 1080p MP4", loaded.get(1).getName());
    }
}
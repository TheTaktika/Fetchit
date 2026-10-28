package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.model.Preset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class PresetStorage {

    private static final Logger log = LoggerFactory.getLogger(PresetStorage.class);
    private static final String FILE_NAME = "presets.json";
    private final ObjectMapper mapper;
    private final File file;

    public PresetStorage() {
        this(new File(FILE_NAME));
    }

    public PresetStorage(File file) {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.file = file;
    }

    public List<Preset> load() {
        if (!file.exists()) {
            List<Preset> defaults = createDefaultPresets();
            save(defaults);
            return defaults;
        }

        try {
            List<Preset> presets = mapper.readValue(file, new TypeReference<>() {});
            presets.forEach(this::normalize);
            return presets;
        } catch (Exception e) {
            log.error("Не удалось загрузить пресеты: {}", e.getMessage());
            return createDefaultPresets();
        }
    }

    private List<Preset> createDefaultPresets() {
        return List.of(Preset.defaultAudioPreset(), Preset.defaultVideoPreset());
    }

    private void normalize(Preset p) {
        if (p.getConcurrentFragments() <= 0) {
            p.setConcurrentFragments(1);
        }
        if (p.getRetries() <= 0) {
            p.setRetries(10);
        }
        if (p.getAudioFormat() == null || p.getAudioFormat().isBlank()) {
            p.setAudioFormat("mp3");
        }
        if (p.getMergeOutputFormat() == null || p.getMergeOutputFormat().isBlank()) {
            p.setMergeOutputFormat("mp4");
        }
    }

    public void save(List<Preset> presets) {
        try {
            mapper.writeValue(file, presets);
        } catch (Exception e) {
            log.error("Не удалось сохранить пресеты: {}", e.getMessage());
        }
    }
}
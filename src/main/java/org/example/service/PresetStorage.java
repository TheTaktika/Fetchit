package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.model.Preset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PresetStorage {

    private static final String FILE_NAME = "presets.json";
    private final ObjectMapper mapper;
    private final File file;

    public PresetStorage() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.file = new File(FILE_NAME);
    }

    public List<Preset> load() {
        if (!file.exists()) {
            List<Preset> defaults = new ArrayList<>();
            defaults.add(Preset.defaultAudioPreset());
            defaults.add(Preset.defaultVideoPreset());
            save(defaults);
            return defaults;
        }

        try {
            List<Preset> presets = mapper.readValue(file, new TypeReference<List<Preset>>() {
            });
            presets.forEach(this::normalize);
            return presets;
        } catch (Exception e) {
            System.err.println("Не удалось загрузить пресеты: " + e.getMessage());
            List<Preset> defaults = new ArrayList<>();
            defaults.add(Preset.defaultAudioPreset());
            defaults.add(Preset.defaultVideoPreset());
            return defaults;
        }
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
            System.err.println("Не удалось сохранить пресеты: " + e.getMessage());
        }
    }
}
package org.example.scanner;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagTextField;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class DownloaderScanner {
    private static final Pattern YOUTUBE_ID = Pattern.compile("^[A-Za-z0-9_-]{11}$");

    public Set<String> scanDownloadedIds(String folderPath) {
        Set<String> downloadedIds = new HashSet<>();

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            return downloadedIds;
        }

        File[] mp3Files = folder.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".mp3")
        );
        if (mp3Files == null) {
            return downloadedIds;
        }

        for (File file : mp3Files) {
            String id = extractVideoId(file);
            if (id != null && !id.isBlank()) {
                downloadedIds.add(id);
            }
        }

        return downloadedIds;
    }

    private String extractVideoId(File file) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            if (audioFile.getTag() == null) {
                return null;
            }

            Iterator<TagField> fields = audioFile.getTag().getFields();
            while (fields.hasNext()) {
                TagField field = fields.next();
                if (field instanceof TagTextField textField) {
                    String value = textField.getContent();
                    if (value == null) continue;
                    value = value.trim();
                    if (YOUTUBE_ID.matcher(value).matches()) {
                        return value;
                    }
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("Не удалось прочитать метаданные: " + file.getName()
                    + " (" + e.getMessage() + ")");
            return null;
        }
    }
}
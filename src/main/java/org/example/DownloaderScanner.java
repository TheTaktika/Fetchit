package org.example;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DownloaderScanner {

    public static Set<String> scanDownloadedIds(String folderPath) {
        Set<String> downloadedIds = new HashSet<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

        if (files == null) return downloadedIds;

        for (File file : files) {
            try {
                AudioFile audioFile = AudioFileIO.read(file);
                String comment = audioFile.getTag().getFirst(FieldKey.COMMENT);

                if (comment != null && !comment.trim().isEmpty()) {
                    downloadedIds.add(comment.trim());
                }
            } catch (Exception e) {
                System.err.println("Не удалось прочитать метаданные: " + file.getName());
            }
        }
        return downloadedIds;
    }
}
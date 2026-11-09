package org.example.scanner;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class DownloaderScanner {
    private static final Logger log = LoggerFactory.getLogger(DownloaderScanner.class);
    private static final Pattern MEDIA_ID = Pattern.compile("^[A-Za-z0-9_-]{5,50}$");
    private static final String[] SUPPORTED_EXTENSIONS = {".mp3", ".mp4", ".webm", ".mkv", ".m4a"};

    public Set<String> scanDownloadedIds(String folderPath) {
        Set<String> downloadedIds = new HashSet<>();

        log.info("Сканирую папку на наличие скачанных файлов: {}", folderPath);

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            log.warn("Папка не существует или не является директорией: {}", folderPath);
            return downloadedIds;
        }

        File[] files = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            for (String ext : SUPPORTED_EXTENSIONS) {
                if (lower.endsWith(ext)) return true;
            }
            return false;
        });

        if (files == null || files.length == 0) {
            log.info("В папке не найдено файлов с поддерживаемыми расширениями.");
            return downloadedIds;
        }

        for (File file : files) {
            String id = extractVideoId(file);
            if (id != null && !id.isBlank()) {
                log.info("Найден скачанный ID: {} в файле: {}", id, file.getName());
                downloadedIds.add(id);
            }
        }

        log.info("Всего найдено скачанных файлов: {}", downloadedIds.size());
        return downloadedIds;
    }

    private String extractVideoId(File file) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            if (audioFile.getTag() == null) {
                log.warn("У файла нет тегов: {}", file.getName());
                return null;
            }

            String comment = audioFile.getTag().getFirst(FieldKey.COMMENT);

            if (comment == null) {
                log.warn("Тег Comment пуст в файле: {}", file.getName());
                return null;
            }

            String value = comment.trim();
            if (MEDIA_ID.matcher(value).matches()) {
                return value;
            } else {
                log.warn("В теге Comment файла {} найден текст '{}', который не похож на ID.", file.getName(), value);
                return null;
            }

        } catch (Exception e) {
            log.warn("Не удалось прочитать метаданные файла: {} ({})", file.getName(), e.getMessage());
            return null;
        }
    }
}
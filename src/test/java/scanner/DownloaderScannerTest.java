package scanner;

import org.example.scanner.DownloaderScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DownloaderScannerTest {

    @TempDir
    Path tempDir;

    private DownloaderScanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new DownloaderScanner();
    }

    @Test
    void scanDownloadedIds_whenFolderDoesNotExist_returnsEmptySet() {
        Set<String> ids = scanner.scanDownloadedIds(
                tempDir.resolve("nonexistent").toString()
        );
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    void scanDownloadedIds_whenFolderIsEmpty_returnsEmptySet() {
        Set<String> ids = scanner.scanDownloadedIds(tempDir.toString());
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    void scanDownloadedIds_whenFolderHasOnlyNonMp3Files_returnsEmptySet() throws IOException {
        Files.writeString(tempDir.resolve("file.txt"), "hello");
        Files.writeString(tempDir.resolve("song.wav"), "audio");
        Files.writeString(tempDir.resolve("image.png"), "image");

        Set<String> ids = scanner.scanDownloadedIds(tempDir.toString());
        assertTrue(ids.isEmpty());
    }

    @Test
    void scanDownloadedIds_whenFolderHasInvalidMp3_returnsEmptySet() throws IOException {
        Files.writeString(tempDir.resolve("broken.mp3"), "not a real mp3");
        Set<String> ids = scanner.scanDownloadedIds(tempDir.toString());
        assertTrue(ids.isEmpty());
    }

    @Test
    void scanDownloadedIds_whenPathIsFile_returnsEmptySet() throws IOException {
        File file = tempDir.resolve("not-a-folder").toFile();
        Files.writeString(file.toPath(), "content");

        Set<String> ids = scanner.scanDownloadedIds(file.getAbsolutePath());
        assertTrue(ids.isEmpty());
    }

    @Test
    void shouldScanFolderAndIgnoreUnsupportedExtensions() throws IOException {
        File folder = tempDir.toFile();

        new File(folder, "video1.mp4").createNewFile();
        new File(folder, "audio1.mp3").createNewFile();
        new File(folder, "document.txt").createNewFile();

        DownloaderScanner scanner = new DownloaderScanner();
        Set<String> ids = scanner.scanDownloadedIds(folder.getAbsolutePath());

        assertTrue(ids.isEmpty());
    }
}
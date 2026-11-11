package service;

import org.example.model.DownloadConfig;
import org.example.model.Preset;
import org.example.service.YtDlpCommandBuilder;
import org.example.service.YtDlpPathResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YtDlpCommandBuilderTest {

    private YtDlpCommandBuilder builder;

    @BeforeEach
    void setUp() {
        YtDlpPathResolver mockResolver = mock(YtDlpPathResolver.class);
        when(mockResolver.resolveBundledTool(anyString())).thenReturn("yt-dlp.exe");
        when(mockResolver.getTempDir()).thenReturn(new File(System.getProperty("java.io.tmpdir")));
        builder = new YtDlpCommandBuilder(mockResolver);
    }

    @Test
    void shouldBuildBasicAudioCommandWithThumbnailFlags() {
        Preset preset = Preset.defaultAudioPreset();
        DownloadConfig config = new DownloadConfig("http://url.com", "/out", null, preset);

        List<String> command = builder.buildDownloadCommand(config);

        assertTrue(command.contains("-x"));
        assertTrue(command.contains("--audio-format"));
        assertTrue(command.contains("mp3"));
        assertFalse(command.contains("-f"));

        assertTrue(command.contains("--embed-thumbnail"));
        assertTrue(command.contains("--convert-thumbnails"));
        assertTrue(command.contains("jpg"));
        assertFalse(command.contains("--compat-options"));
    }

    @Test
    void shouldBuildVideoCommandWithQualityAndThumbnailFlags() {
        Preset preset = Preset.defaultVideoPreset();
        DownloadConfig config = new DownloadConfig("http://url.com", "/out", null, preset);

        List<String> command = builder.buildDownloadCommand(config);

        assertTrue(command.contains("-f"));
        assertTrue(command.contains("bestvideo[height<=1080]+bestaudio/best[height<=1080]"));
        assertTrue(command.contains("--merge-output-format"));

        assertTrue(command.contains("--embed-thumbnail"));
        assertTrue(command.contains("--compat-options"));
        assertTrue(command.contains("embed-thumbnail-atomicparsley"));
        assertTrue(command.contains("--remux-video"));
        assertTrue(command.contains("mp4"));
    }

    @Test
    void shouldIncludeProxyWhenPresent() {
        Preset preset = Preset.defaultAudioPreset();
        DownloadConfig config = new DownloadConfig("http://url.com", "/out", "127.0.0.1:8080", preset);

        List<String> command = builder.buildDownloadCommand(config);

        assertTrue(command.contains("--proxy"));
        assertTrue(command.contains("127.0.0.1:8080"));
    }

    @Test
    void shouldUseWebpageUrlInPlaylistCommand() {
        Preset preset = Preset.defaultAudioPreset();
        DownloadConfig config = new DownloadConfig("http://url.com", "/out", null, preset);

        List<String> command = builder.buildPlaylistCommand(config);

        assertTrue(command.contains("%(webpage_url)s"));
        assertFalse(command.contains("%(id)s"));
    }
}
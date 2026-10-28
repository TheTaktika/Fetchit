package service;

import org.example.model.DownloadConfig;
import org.example.model.DownloadStats;
import org.example.model.Preset;
import org.example.parser.ErrorClassifier;
import org.example.service.DownloadCallbacks;
import org.example.service.ProcessRunner;
import org.example.service.YtDlpCommandBuilder;
import org.example.service.YtDlpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YtDlpServiceTest {

    private YtDlpService service;
    private ProcessRunner mockRunner;

    @BeforeEach
    void setUp() {
        mockRunner = mock(ProcessRunner.class);
        YtDlpCommandBuilder mockBuilder = mock(YtDlpCommandBuilder.class);
        when(mockBuilder.buildDownloadCommand(any())).thenReturn(List.of("yt-dlp"));
        service = new YtDlpService(mockBuilder, mockRunner, new ErrorClassifier());
    }

    @Test
    void shouldUpdateStatsBasedOnOutput() {
        doAnswer(invocation -> {
            Consumer<String> stdout = invocation.getArgument(1);
            Consumer<String> stderr = invocation.getArgument(2);

            stdout.accept("TITLE: Test Video");
            stdout.accept("Downloading item 1 of 2");
            stdout.accept("[download]  50.0% of 10MiB");
            stdout.accept("[Merger] Merging formats into video.mp4");
            stderr.accept("ERROR: Video is private");

            return 0;
        }).when(mockRunner).run(anyList(), any(), any(), any());

        DownloadConfig config = new DownloadConfig("http://url", "/dir", null, Preset.defaultAudioPreset());
        DownloadCallbacks callbacks = mock(DownloadCallbacks.class);

        DownloadStats stats = service.downloadByIds(config, List.of("id1", "id2"), callbacks);

        assertEquals(2, stats.getTotalItems());
        assertEquals(1, stats.getCompletedItems());
        assertEquals(1, stats.getErrorsCount());

        verify(callbacks).onTitleFetched("Test Video");
        verify(callbacks, atLeastOnce()).onProgress(any());
    }
}
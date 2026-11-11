package service;

import org.example.model.*;
import org.example.service.QueueManager;
import org.example.service.YtDlpService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class QueueManagerTest {

    @Test
    void shouldProcessWaitingItemAndMarkDone() throws InterruptedException {
        YtDlpService mockService = mock(YtDlpService.class);

        PlaylistFetchResult fetchResult = PlaylistFetchResult.success(List.of("id1"), "Title", "thumb");
        when(mockService.getPlaylistVideoIds(any(), any())).thenReturn(fetchResult);

        DownloadStats mockStats = new DownloadStats();
        when(mockService.downloadByIds(any(), anyList(), any())).thenReturn(mockStats);

        DownloadConfig config = new DownloadConfig("url", "/dir", null, Preset.defaultAudioPreset());
        DownloadItem item = new DownloadItem("url", config);

        List<DownloadItem> queue = new ArrayList<>();
        queue.add(item);

        QueueManager manager = new QueueManager(queue, mockService, msg -> {});

        manager.processQueue();

        Thread.sleep(1200);

        assertEquals(DownloadStatus.DONE, item.getStatus());
        assertEquals(100.0, item.getProgress());
    }
}
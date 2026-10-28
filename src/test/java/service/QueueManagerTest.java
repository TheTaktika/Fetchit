package service;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.model.*;
import org.example.service.QueueManager;
import org.example.service.YtDlpService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class QueueManagerTest {

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        latch.await();
    }

    @Test
    void shouldProcessWaitingItemAndMarkDone() throws InterruptedException {
        YtDlpService mockService = mock(YtDlpService.class);

        PlaylistFetchResult fetchResult = PlaylistFetchResult.success(List.of("id1"), "Title", "thumb");
        when(mockService.getPlaylistVideoIds(any(), any())).thenReturn(fetchResult);

        DownloadStats mockStats = new DownloadStats();
        when(mockService.downloadByIds(any(), anyList(), any())).thenReturn(mockStats);

        DownloadConfig config = new DownloadConfig("url", "/dir", null, Preset.defaultAudioPreset());
        DownloadItem item = new DownloadItem("url", config);

        ObservableList<DownloadItem> queue = FXCollections.observableArrayList(item);
        QueueManager manager = new QueueManager(queue, mockService, msg -> {});

        manager.processQueue();

        Thread.sleep(1000);

        assertEquals(DownloadStatus.DONE, item.getStatus());
        assertEquals(100.0, item.getProgress());
    }
}
package model;

import javafx.application.Platform;
import org.example.model.DownloadConfig;
import org.example.model.DownloadItem;
import org.example.model.DownloadStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class DownloadItemTest {

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
    void shouldInitializeWithCorrectDefaults() {
        DownloadConfig config = new DownloadConfig("http://url.com", "/dir", null, null);
        DownloadItem item = new DownloadItem("http://url.com", config);

        assertEquals("http://url.com", item.getUrl());
        assertEquals("http://url.com", item.getTitle());
        assertEquals(DownloadStatus.WAITING, item.getStatus());
        assertEquals(0.0, item.getProgress());
        assertEquals("", item.getSummary());
        assertNull(item.getThumbnail());
        assertEquals(config, item.getConfig());
    }

    @Test
    void shouldChangeStatusOnPause() {
        DownloadItem item = createValidItem();
        item.pause();
        assertEquals(DownloadStatus.PAUSED, item.getStatus());
    }

    @Test
    void shouldChangeStatusOnCancel() {
        DownloadItem item = createValidItem();
        item.cancel();
        assertEquals(DownloadStatus.CANCELLED, item.getStatus());
    }

    private DownloadItem createValidItem() {
        DownloadConfig config = new DownloadConfig("http://url.com", "/dir", null, null);
        return new DownloadItem("http://url.com", config);
    }
}
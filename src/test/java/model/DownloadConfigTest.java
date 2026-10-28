package model;

import org.example.model.DownloadConfig;
import org.example.model.Preset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DownloadConfigTest {

    @Test
    void shouldUseDefaultPresetWhenNull() {
        DownloadConfig config = new DownloadConfig("http://url.com", "/dir", null, null);
        assertNotNull(config.preset());
        assertEquals(Preset.defaultAudioPreset(), config.preset());
    }

    @Test
    void shouldTrimProxyAndReturnTrueIfPresent() {
        DownloadConfig config = new DownloadConfig("http://url.com", "/dir", "  127.0.0.1:8080  ", null);
        assertTrue(config.hasProxy());
        assertEquals("127.0.0.1:8080", config.proxy());
    }

    @Test
    void shouldReturnFalseForBlankProxy() {
        DownloadConfig config = new DownloadConfig("http://url.com", "/dir", "   ", null);
        assertFalse(config.hasProxy());
        assertNull(config.proxy());
    }

    @Test
    void shouldThrowExceptionWhenUrlIsNull() {
        assertThrows(NullPointerException.class, () ->
                new DownloadConfig(null, "/dir", null, null)
        );
    }
}
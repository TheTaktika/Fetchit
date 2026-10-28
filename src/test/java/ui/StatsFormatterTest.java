package ui;

import org.example.model.DownloadStats;
import org.example.model.ErrorType;
import org.example.ui.LanguageManager;
import org.example.ui.StatsFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsFormatterTest {

    @Test
    void shouldFormatStatsCorrectly() {
        DownloadStats stats = new DownloadStats();
        stats.setPlaylistTotal(5);
        stats.setAlreadyOnDisk(1);
        stats.incrementDownloaded();
        stats.incrementErrors();
        stats.addError(ErrorType.PRIVATE_VIDEO, "Video is private");

        String result = StatsFormatter.format(stats);

        String expectedErrorTranslation = LanguageManager.getInstance().get(ErrorType.PRIVATE_VIDEO.getMessageKey());

        assertTrue(result.contains("5"));
        assertTrue(result.contains("1"));
        assertTrue(result.contains(expectedErrorTranslation));
    }
}
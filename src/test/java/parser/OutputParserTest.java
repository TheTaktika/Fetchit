package parser;

import org.example.model.DownloadStats;
import org.example.parser.OutputParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputParserTest {

    private OutputParser parser;

    @BeforeEach
    void setUp() {
        parser = new OutputParser();
    }

    @Test
    void parseProgressLine_returnsCorrectPercent() {
        double result = parser.parseLine("[download]  45.2% of 3.21MiB at 1.20MiB/s ETA 00:01");
        assertEquals(45.2, result, 0.001);
    }

    @Test
    void parseProgressLine_withFullPercent_returns100() {
        double result = parser.parseLine("[download] 100% of 3.21MiB");
        assertEquals(100.0, result, 0.001);
    }

    @Test
    void parseNonProgressLine_returnsMinusOne() {
        double result = parser.parseLine("some random output line");
        assertEquals(-1, result);
    }

    @Test
    void parseItemOfLine_setsTotalItems() {
        parser.parseLine("[download] Downloading item 3 of 10");
        assertEquals(10, parser.getStats().getTotalItems());
    }

    @Test
    void parseErrorLine_incrementsErrors() {
        parser.parseLine("ERROR: unable to download video data");
        DownloadStats stats = parser.getStats();
        assertEquals(1, stats.getErrorsCount());
        assertEquals(1, stats.getErrorMessages().size());
        assertTrue(stats.getErrorMessages().get(0).contains("unable to download"));
    }

    @Test
    void parseAlreadyDownloadedLine_incrementsSkippedAndCompleted() {
        parser.parseLine("test_output/song.mp3 has already been downloaded");
        DownloadStats stats = parser.getStats();
        assertEquals(1, stats.getSkippedCount());
        assertEquals(1, stats.getCompletedItems());
    }

    @Test
    void parseExtractAudioLine_incrementsDownloadedAndCompleted() {
        parser.parseLine("[ExtractAudio] Destination: test_output/song.mp3");
        DownloadStats stats = parser.getStats();
        assertEquals(1, stats.getDownloadedCount());
        assertEquals(1, stats.getCompletedItems());
    }

    @Test
    void constructorWithTotalItems_setsTotalItemsImmediately() {
        OutputParser p = new OutputParser(5);
        assertEquals(5, p.getStats().getTotalItems());
    }

    @Test
    void parseItemOfLine_doesNotOverrideInitialTotalItems() {
        OutputParser p = new OutputParser(5);
        p.parseLine("[download] Downloading item 1 of 10");
        assertEquals(5, p.getStats().getTotalItems());
    }

    @Test
    void parseMultipleErrors_accumulatesMessages() {
        parser.parseLine("ERROR: first error");
        parser.parseLine("ERROR: second error");
        DownloadStats stats = parser.getStats();
        assertEquals(2, stats.getErrorsCount());
        assertEquals(2, stats.getErrorMessages().size());
    }
}
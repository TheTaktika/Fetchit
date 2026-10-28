package parser;

import org.example.model.ErrorType;
import org.example.parser.OutputParser;
import org.example.parser.event.ParseEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputParserTest {

    private final OutputParser parser = new OutputParser();

    @Test
    void shouldParseProgress() {
        ParseEvent event = parser.parseLine("[download]  45.5% of 10.00MiB");
        assertInstanceOf(ParseEvent.Progress.class, event);
        assertEquals(45.5, ((ParseEvent.Progress) event).percent(), 0.001);
    }

    @Test
    void shouldParseError() {
        ParseEvent event = parser.parseLine("ERROR: Video is private");
        assertInstanceOf(ParseEvent.Error.class, event);
        ParseEvent.Error error = (ParseEvent.Error) event;
        assertEquals(ErrorType.PRIVATE_VIDEO, error.type());
        assertEquals("Video is private", error.message());
    }

    @Test
    void shouldParseTotalItems() {
        ParseEvent event = parser.parseLine("Downloading item 1 of 5");
        assertInstanceOf(ParseEvent.TotalItems.class, event);
        assertEquals(5, ((ParseEvent.TotalItems) event).total());
    }

    @Test
    void shouldParseSkipped() {
        ParseEvent event = parser.parseLine("video.mp4 has already been downloaded");
        assertInstanceOf(ParseEvent.Skipped.class, event);
    }

    @Test
    void shouldParseCompleted() {
        ParseEvent event1 = parser.parseLine("[ExtractAudio] Destination: audio.mp3");
        assertInstanceOf(ParseEvent.Completed.class, event1);

        ParseEvent event2 = parser.parseLine("[Merger] Merging formats into video.mp4");
        assertInstanceOf(ParseEvent.Completed.class, event2);
    }

    @Test
    void shouldIgnoreUnknownLines() {
        ParseEvent event = parser.parseLine("Some random yt-dlp log line");
        assertInstanceOf(ParseEvent.Ignored.class, event);
    }
}
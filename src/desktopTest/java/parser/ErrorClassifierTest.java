package parser;

import org.example.model.ErrorType;
import org.example.parser.ErrorClassifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorClassifierTest {

    private ErrorClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new ErrorClassifier();
    }

    @Test
    void classify_geoBlocked() {
        assertEquals(ErrorType.GEO_BLOCKED,
                classifier.classify("Video not available in your country"));
    }

    @Test
    void classify_privateVideo() {
        assertEquals(ErrorType.PRIVATE_VIDEO,
                classifier.classify("Private video. Sign in if you've been granted access"));
    }

    @Test
    void classify_rateLimited() {
        assertEquals(ErrorType.RATE_LIMITED,
                classifier.classify("HTTP Error 429: Too Many Requests"));
    }

    @Test
    void classify_networkError() {
        assertEquals(ErrorType.NETWORK_ERROR,
                classifier.classify("unable to download video data: read timed out"));
    }

    @Test
    void classify_botCheck() {
        assertEquals(ErrorType.BOT_CHECK,
                classifier.classify("Sign in to confirm you're not a bot"));
    }

    @Test
    void classify_videoUnavailable() {
        assertEquals(ErrorType.VIDEO_UNAVAILABLE,
                classifier.classify("This video is not available"));
    }

    @Test
    void classify_playlistNotFound() {
        assertEquals(ErrorType.PLAYLIST_NOT_FOUND,
                classifier.classify("The playlist does not exist"));
    }

    @Test
    void classify_signInRequired() {
        assertEquals(ErrorType.SIGN_IN_REQUIRED,
                classifier.classify("Sign in to confirm your age"));
    }

    @Test
    void classify_unknown() {
        assertEquals(ErrorType.UNKNOWN,
                classifier.classify("some weird error we don't know"));
    }

    @Test
    void classify_empty_returnsUnknown() {
        assertEquals(ErrorType.UNKNOWN, classifier.classify(""));
        assertEquals(ErrorType.UNKNOWN, classifier.classify(null));
    }

    @Test
    void classify_botCheckTakesPrecedenceOverSignIn() {
        assertEquals(ErrorType.BOT_CHECK,
                classifier.classify("Sign in to confirm you're not a bot"));
    }

    @Test
    void shouldReturnUnknownForNullOrBlank() {
        assertEquals(ErrorType.UNKNOWN, classifier.classify(null));
        assertEquals(ErrorType.UNKNOWN, classifier.classify("   "));
    }

    @Test
    void shouldClassifyInvalidUrl() {
        assertEquals(ErrorType.INVALID_URL, classifier.classify("ERROR: YouTube said: Invalid URL"));
        assertEquals(ErrorType.INVALID_URL, classifier.classify("getaddrinfo failed"));
    }

    @Test
    void shouldClassifyGeoBlocked() {
        assertEquals(ErrorType.GEO_BLOCKED, classifier.classify("Video is not available in your country"));
    }

    @Test
    void shouldClassifyBotCheck() {
        assertEquals(ErrorType.BOT_CHECK, classifier.classify("Sign in to confirm you're not a bot"));
    }

    @Test
    void shouldClassifyNetworkError() {
        assertEquals(ErrorType.NETWORK_ERROR, classifier.classify("Read timed out"));
    }

    @Test
    void shouldReturnUnknownForUnrecognizedError() {
        assertEquals(ErrorType.UNKNOWN, classifier.classify("Some completely new error"));
    }
}
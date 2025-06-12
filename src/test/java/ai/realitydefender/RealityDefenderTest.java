        package ai.realitydefender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ai.realitydefender.core.RealityDefenderConfig;
import ai.realitydefender.detection.DetectionService;
import ai.realitydefender.exceptions.RealityDefenderException;
import ai.realitydefender.models.DetectionResult;
import ai.realitydefender.models.UploadResponse;
import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RealityDefenderTest {

    @Mock
    private DetectionService detectionService;

    private RealityDefender client;
    private File testFile;

    @BeforeEach
    void setUp() {
        RealityDefenderConfig config = new RealityDefenderConfig(
                "test-api-key",
                "https://api.realitydefender.com",
                Duration.ofSeconds(30)
        );

        // We'll need to modify the constructor to accept DetectionService for testing
        // For now, we'll test the builder
        testFile = new File("test-image.jpg");
    }

    @Test
    void testBuilderWithValidApiKey() {
        RealityDefender client = RealityDefender.builder()
                .apiKey("test-api-key")
                .build();

        assertNotNull(client);
    }

    @Test
    void testBuilderWithNullApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            RealityDefender.builder()
                    .apiKey(null)
                    .build();
        });
    }

    @Test
    void testBuilderWithEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            RealityDefender.builder()
                    .apiKey("")
                    .build();
        });
    }

    @Test
    void testBuilderWithCustomConfiguration() {
        Duration customTimeout = Duration.ofMinutes(2);
        String customBaseUrl = "https://custom.api.com";

        RealityDefender client = RealityDefender.builder()
                .apiKey("test-api-key")
                .baseUrl(customBaseUrl)
                .timeout(customTimeout)
                .build();

        assertNotNull(client);
    }
}



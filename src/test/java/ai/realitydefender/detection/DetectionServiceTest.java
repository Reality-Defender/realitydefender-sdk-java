package ai.realitydefender.detection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ai.realitydefender.client.HttpClient;
import ai.realitydefender.exceptions.RealityDefenderException;
import ai.realitydefender.models.DetectionResult;
import ai.realitydefender.models.UploadResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DetectionServiceTest {

  @Mock private HttpClient httpClient;

  private DetectionService detectionService;
  private ObjectMapper objectMapper;
  private File testFile;

  @TempDir File tempDir;

  @BeforeEach
  void setUp() throws Exception {
    detectionService = new DetectionService(httpClient);
    objectMapper = new ObjectMapper();

    // Create a temporary test file
    testFile = new File(tempDir, "test-image.jpg");
    testFile.createNewFile();
  }

  @Test
  void testUploadSuccess() throws Exception {
    // Arrange
    String uploadResponseJson =
        "{\n" + "    \"request_id\": \"req-123\",\n" + "    \"media_id\": \"media-456\"\n" + "}";
    JsonNode uploadResponse = objectMapper.readTree(uploadResponseJson);
    when(httpClient.uploadFile(testFile)).thenReturn(uploadResponse);

    // Act
    UploadResponse result = detectionService.upload(testFile);

    // Assert
    assertEquals("req-123", result.getRequestId());
    assertEquals("media-456", result.getMediaId());
    verify(httpClient).uploadFile(testFile);
  }

  @Test
  void testUploadAsync() throws Exception {
    // Arrange
    String uploadResponseJson =
        "{\n" + "    \"request_id\": \"req-123\",\n" + "    \"media_id\": \"media-456\"\n" + "}";
    JsonNode uploadResponse = objectMapper.readTree(uploadResponseJson);
    when(httpClient.uploadFile(testFile)).thenReturn(uploadResponse);

    // Act
    CompletableFuture<UploadResponse> future = detectionService.uploadAsync(testFile);
    UploadResponse result = future.get();

    // Assert
    assertEquals("req-123", result.getRequestId());
    assertEquals("media-456", result.getMediaId());
  }

  @Test
  void testGetResultPollingSuccess() throws Exception {
    // Arrange
    String processingResponseJson =
        "{\n"
            + "    \"status\": \"PROCESSING\",\n"
            + "    \"score\": null,\n"
            + "    \"models\": []\n"
            + "}";
    String completedResponseJson =
        "{\n"
            + "    \"status\": \"ARTIFICIAL\",\n"
            + "    \"score\": 0.95,\n"
            + "    \"models\": [\n"
            + "        {\n"
            + "            \"name\": \"model1\",\n"
            + "            \"status\": \"ARTIFICIAL\",\n"
            + "            \"score\": 0.95\n"
            + "        }\n"
            + "    ]\n"
            + "}";

    JsonNode processingResponse = objectMapper.readTree(processingResponseJson);
    JsonNode completedResponse = objectMapper.readTree(completedResponseJson);

    when(httpClient.getResults("req-123"))
        .thenReturn(processingResponse)
        .thenReturn(completedResponse);

    // Act
    DetectionResult result =
        detectionService.getResult("req-123", Duration.ofMillis(10), Duration.ofSeconds(5));

    // Assert
    assertEquals("ARTIFICIAL", result.getStatus());
    assertEquals(0.95, result.getScore());
    assertEquals(1, result.getModels().size());
    assertEquals("model1", result.getModels().get(0).getName());

    verify(httpClient, times(2)).getResults("req-123");
  }

  @Test
  void testGetResultAsync() throws Exception {
    // Arrange
    String completedResponseJson =
        "{\n"
            + "    \"status\": \"AUTHENTIC\",\n"
            + "    \"score\": 0.15,\n"
            + "    \"models\": []\n"
            + "}";
    JsonNode completedResponse = objectMapper.readTree(completedResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(completedResponse);

    // Act
    CompletableFuture<DetectionResult> future = detectionService.getResultAsync("req-123");
    DetectionResult result = future.get();

    // Assert
    assertEquals("AUTHENTIC", result.getStatus());
    assertEquals(0.15, result.getScore());
  }

  @Test
  void testDetectFile() throws Exception {
    // Arrange upload response
    String uploadResponseJson =
        "{\n" + "    \"request_id\": \"req-123\",\n" + "    \"media_id\": \"media-456\"\n" + "}";
    JsonNode uploadResponse = objectMapper.readTree(uploadResponseJson);
    when(httpClient.uploadFile(testFile)).thenReturn(uploadResponse);

    // Arrange detection response
    String detectionResponseJson =
        "{\n"
            + "    \"status\": \"ARTIFICIAL\",\n"
            + "    \"score\": 0.87,\n"
            + "    \"models\": []\n"
            + "}";
    JsonNode detectionResponse = objectMapper.readTree(detectionResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(detectionResponse);

    // Act
    DetectionResult result = detectionService.detectFile(testFile);

    // Assert
    assertEquals("ARTIFICIAL", result.getStatus());
    assertEquals(0.87, result.getScore());
    verify(httpClient).uploadFile(testFile);
    verify(httpClient).getResults("req-123");
  }

  @Test
  void testDetectFileAsync() throws Exception {
    // Arrange upload response
    String uploadResponseJson =
        "{\n" + "    \"request_id\": \"req-123\",\n" + "    \"media_id\": \"media-456\"\n" + "}";
    JsonNode uploadResponse = objectMapper.readTree(uploadResponseJson);
    when(httpClient.uploadFile(testFile)).thenReturn(uploadResponse);

    // Arrange detection response
    String detectionResponseJson =
        "{\n"
            + "    \"status\": \"AUTHENTIC\",\n"
            + "    \"score\": 0.23,\n"
            + "    \"models\": []\n"
            + "}";
    JsonNode detectionResponse = objectMapper.readTree(detectionResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(detectionResponse);

    // Act
    CompletableFuture<DetectionResult> future = detectionService.detectFileAsync(testFile);
    DetectionResult result = future.get();

    // Assert
    assertEquals("AUTHENTIC", result.getStatus());
    assertEquals(0.23, result.getScore());
  }

  @Test
  void testCheckStatus() throws Exception {
    // Arrange
    String responseJson =
        "{\n"
            + "    \"status\": \"PROCESSING\",\n"
            + "    \"score\": null,\n"
            + "    \"models\": []\n"
            + "}";
    JsonNode response = objectMapper.readTree(responseJson);
    when(httpClient.getResults("req-123")).thenReturn(response);

    // Act
    DetectionResult result = detectionService.checkStatus("req-123");

    // Assert
    assertEquals("PROCESSING", result.getStatus());
    assertNull(result.getScore());
    verify(httpClient).getResults("req-123");
  }

  @Test
  void testPollForResultsWithCallbacks() throws Exception {
    // Arrange
    String completedResponseJson =
        "{\n"
            + "    \"status\": \"ARTIFICIAL\",\n"
            + "    \"score\": 0.91,\n"
            + "    \"models\": []\n"
            + "}";
    JsonNode completedResponse = objectMapper.readTree(completedResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(completedResponse);

    // Act
    AtomicReference<DetectionResult> resultRef = new AtomicReference<>();
    AtomicReference<RealityDefenderException> errorRef = new AtomicReference<>();

    detectionService.pollForResults(
        "req-123", Duration.ofMillis(10), Duration.ofSeconds(5), resultRef::set, errorRef::set);

    // Wait a bit for the callback to be called
    Thread.sleep(100);

    // Assert
    assertNotNull(resultRef.get());
    assertNull(errorRef.get());
    assertEquals("ARTIFICIAL", resultRef.get().getStatus());
    assertEquals(0.91, resultRef.get().getScore());
  }

  @Test
  void testPollForResultsTimeout() throws Exception {
    // Arrange - always return processing status
    String processingResponseJson =
        "{\n"
            + "    \"status\": \"PROCESSING\",\n"
            + "    \"score\": null,\n"
            + "    \"models\": []\n"
            + "}";
    JsonNode processingResponse = objectMapper.readTree(processingResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(processingResponse);

    // Act & Assert
    assertThrows(
        RealityDefenderException.class,
        () -> {
          detectionService.getResult("req-123", Duration.ofMillis(10), Duration.ofMillis(50));
        });
  }

  @Test
  void testUploadFailure() throws Exception {
    // Arrange
    when(httpClient.uploadFile(testFile))
        .thenThrow(new RealityDefenderException("Upload failed", "UPLOAD_FAILED"));

    // Act & Assert
    RealityDefenderException exception =
        assertThrows(
            RealityDefenderException.class,
            () -> {
              detectionService.upload(testFile);
            });

    assertEquals("Upload failed", exception.getMessage());
    assertEquals("UPLOAD_FAILED", exception.getCode());
  }

  @Test
  void testGetResultFailure() throws Exception {
    // Arrange
    when(httpClient.getResults("req-123"))
        .thenThrow(new RealityDefenderException("Results failed", "RESULTS_FAILED"));

    // Act & Assert
    RealityDefenderException exception =
        assertThrows(
            RealityDefenderException.class,
            () -> {
              detectionService.getResult("req-123", Duration.ofMillis(10), Duration.ofSeconds(1));
            });

    assertEquals("Results failed", exception.getMessage());
    assertEquals("RESULTS_FAILED", exception.getCode());
  }

  @Test
  void testPollForResultsAsync() throws Exception {
    // Arrange
    String completedResponseJson =
        "{\n"
            + "    \"status\": \"AUTHENTIC\",\n"
            + "    \"score\": 0.25,\n"
            + "    \"models\": []\n"
            + "}";
    JsonNode completedResponse = objectMapper.readTree(completedResponseJson);
    when(httpClient.getResults("req-456")).thenReturn(completedResponse);

    // Act
    CompletableFuture<DetectionResult> future =
        detectionService.pollForResultsAsync(
            "req-456", Duration.ofMillis(10), Duration.ofSeconds(5));
    DetectionResult result = future.get();

    // Assert
    assertEquals("AUTHENTIC", result.getStatus());
    assertEquals(0.25, result.getScore());
  }

  @Test
  void testCheckStatusAsync() throws Exception {
    // Arrange
    String responseJson =
        "{\n"
            + "    \"status\": \"ANALYZING\",\n"
            + "    \"score\": null,\n"
            + "    \"models\": []\n"
            + "}";
    JsonNode response = objectMapper.readTree(responseJson);
    when(httpClient.getResults("req-789")).thenReturn(response);

    // Act
    CompletableFuture<DetectionResult> future = detectionService.checkStatusAsync("req-789");
    DetectionResult result = future.get();

    // Assert
    assertEquals("ANALYZING", result.getStatus());
    assertNull(result.getScore());
  }

  @Test
  void testIsProcessingWithDifferentStatuses() throws Exception {
    // Test different processing statuses
    String[] processingStatuses = {"PROCESSING", "ANALYZING", "QUEUED"};
    String[] completedStatuses = {"ARTIFICIAL", "AUTHENTIC", "ERROR"};

    for (String status : processingStatuses) {
      String responseJson =
          "{\n"
              + "    \"status\": \""
              + status
              + "\",\n"
              + "    \"score\": null,\n"
              + "    \"models\": []\n"
              + "}";
      JsonNode response = objectMapper.readTree(responseJson);
      when(httpClient.getResults("req-" + status)).thenReturn(response);

      DetectionResult result = detectionService.checkStatus("req-" + status);
      assertEquals(status, result.getStatus());
    }

    for (String status : completedStatuses) {
      String responseJson =
          "{\n"
              + "    \"status\": \""
              + status
              + "\",\n"
              + "    \"score\": 0.5,\n"
              + "    \"models\": []\n"
              + "}";
      JsonNode response = objectMapper.readTree(responseJson);
      when(httpClient.getResults("req-" + status)).thenReturn(response);

      DetectionResult result = detectionService.checkStatus("req-" + status);
      assertEquals(status, result.getStatus());
    }
  }

  @Test
  void testShutdown() {
    // Act
    detectionService.shutdown();

    // Assert - no exception should be thrown
    // This test mainly ensures the shutdown method doesn't throw exceptions
    assertDoesNotThrow(() -> detectionService.shutdown());
  }
}

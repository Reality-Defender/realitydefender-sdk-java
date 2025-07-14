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
    String processingResponseJson = createDetectionResultJson("PROCESSING", "req-123", null);
    String completedResponseJson = createDetectionResultJson("FAKE", "req-123", createModelsJson());

    JsonNode processingResponse = objectMapper.readTree(processingResponseJson);
    JsonNode completedResponse = objectMapper.readTree(completedResponseJson);

    when(httpClient.getResults("req-123"))
        .thenReturn(processingResponse)
        .thenReturn(completedResponse);

    // Act
    DetectionResult result = detectionService.getResult("req-123", Duration.ofMillis(10), 30);

    // Assert
    assertEquals("MANIPULATED", result.getOverallStatus());
    assertEquals("req-123", result.getRequestId());
    assertEquals(1, result.getModels().size());
    assertEquals("model1", result.getModels().get(0).getName());
    assertEquals("MANIPULATED", result.getModels().get(0).getStatus());

    verify(httpClient, times(2)).getResults("req-123");
  }

  @Test
  void testGetResultAsync() throws Exception {
    // Arrange
    String completedResponseJson = createDetectionResultJson("AUTHENTIC", "req-123", "[]");
    JsonNode completedResponse = objectMapper.readTree(completedResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(completedResponse);

    // Act
    CompletableFuture<DetectionResult> future = detectionService.getResultAsync("req-123");
    DetectionResult result = future.get();

    // Assert
    assertEquals("AUTHENTIC", result.getOverallStatus());
    assertEquals("req-123", result.getRequestId());
  }

  @Test
  void testDetectFile() throws Exception {
    // Arrange upload response
    String uploadResponseJson =
        "{\n" + "    \"request_id\": \"req-123\",\n" + "    \"media_id\": \"media-456\"\n" + "}";
    JsonNode uploadResponse = objectMapper.readTree(uploadResponseJson);
    when(httpClient.uploadFile(testFile)).thenReturn(uploadResponse);

    // Arrange detection response
    String detectionResponseJson = createDetectionResultJson("FAKE", "req-123", "[]");
    JsonNode detectionResponse = objectMapper.readTree(detectionResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(detectionResponse);

    // Act
    DetectionResult result = detectionService.detectFile(testFile);

    // Assert
    assertEquals("MANIPULATED", result.getOverallStatus());
    assertEquals("req-123", result.getRequestId());
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
    String detectionResponseJson = createDetectionResultJson("AUTHENTIC", "req-123", "[]");
    JsonNode detectionResponse = objectMapper.readTree(detectionResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(detectionResponse);

    // Act
    CompletableFuture<DetectionResult> future = detectionService.detectFileAsync(testFile);
    DetectionResult result = future.get();

    // Assert
    assertEquals("AUTHENTIC", result.getOverallStatus());
    assertEquals("req-123", result.getRequestId());
  }

  @Test
  void testCheckStatus() throws Exception {
    // Arrange
    String responseJson = createDetectionResultJson("PROCESSING", "req-123", "[]");
    JsonNode response = objectMapper.readTree(responseJson);
    when(httpClient.getResults("req-123")).thenReturn(response);

    // Act
    DetectionResult result = detectionService.checkStatus("req-123");

    // Assert
    assertEquals("PROCESSING", result.getOverallStatus());
    assertEquals("req-123", result.getRequestId());
    verify(httpClient).getResults("req-123");
  }

  @Test
  void testPollForResultsWithCallbacks() throws Exception {
    // Arrange
    String completedResponseJson = createDetectionResultJson("FAKE", "req-123", "[]");
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
    assertEquals("MANIPULATED", resultRef.get().getOverallStatus());
    assertEquals("req-123", resultRef.get().getRequestId());
  }

  @Test
  void testPollForResultsAsync() throws Exception {
    // Arrange
    String completedResponseJson = createDetectionResultJson("FAKE", "req-123", "[]");
    JsonNode completedResponse = objectMapper.readTree(completedResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(completedResponse);

    // Act
    CompletableFuture<DetectionResult> future =
        detectionService.pollForResultsAsync(
            "req-123", Duration.ofMillis(10), Duration.ofSeconds(5));
    DetectionResult result = future.get();

    // Assert
    assertEquals("MANIPULATED", result.getOverallStatus());
    assertEquals("req-123", result.getRequestId());
  }

  @Test
  void testCheckStatusAsync() throws Exception {
    // Arrange
    String responseJson = createDetectionResultJson("ANALYZING", "req-123", "[]");
    JsonNode response = objectMapper.readTree(responseJson);
    when(httpClient.getResults("req-123")).thenReturn(response);

    // Act
    CompletableFuture<DetectionResult> future = detectionService.checkStatusAsync("req-123");
    DetectionResult result = future.get();

    // Assert
    assertEquals("ANALYZING", result.getOverallStatus());
    assertEquals("req-123", result.getRequestId());
  }

  @Test
  void testIsProcessingWithDifferentStatuses() throws Exception {
    // Test different status values to ensure proper processing detection
    String[] processingStatuses = {"PROCESSING", "ANALYZING", "QUEUED"};
    String[] completedStatuses = {"MANIPULATED", "AUTHENTIC", "COMPLETED", "FAILED"};

    for (String status : processingStatuses) {
      String responseJson = createDetectionResultJson(status, "req-123", "[]");
      JsonNode response = objectMapper.readTree(responseJson);
      when(httpClient.getResults("req-123")).thenReturn(response);

      DetectionResult result = detectionService.checkStatus("req-123");
      assertEquals(status, result.getOverallStatus());
    }

    for (String status : completedStatuses) {
      String responseJson = createDetectionResultJson(status, "req-123", "[]");
      JsonNode response = objectMapper.readTree(responseJson);
      when(httpClient.getResults("req-123")).thenReturn(response);

      DetectionResult result = detectionService.checkStatus("req-123");
      assertEquals(status, result.getOverallStatus());
    }
  }

  @Test
  void testUploadFailure() throws Exception {
    // Arrange
    when(httpClient.uploadFile(testFile))
        .thenThrow(new RealityDefenderException("Upload failed", "UPLOAD_ERROR"));

    // Act & Assert
    assertThrows(RealityDefenderException.class, () -> detectionService.upload(testFile));
  }

  @Test
  void testGetResultTimeout() throws Exception {
    // Arrange - Always return processing s
    String processingResponseJson = createDetectionResultJson("PROCESSING", "req-123", "[]");
    JsonNode processingResponse = objectMapper.readTree(processingResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(processingResponse);

    // Act & Assert
    assertThrows(
        RealityDefenderException.class,
        () -> detectionService.getResult("req-123", Duration.ofMillis(10), 30));
  }

  // Helper methods to create JSON responses
  private String createDetectionResultJson(String status, String requestId, String modelsJson) {
    if (modelsJson == null) {
      modelsJson = "[]";
    }

    return "{\n"
        + "    \"name\": \"test-name\",\n"
        + "    \"filename\": \"test-filename\",\n"
        + "    \"aggregationResultUrl\": null,\n"
        + "    \"originalFileName\": \"test-original.jpg\",\n"
        + "    \"storageLocation\": \"https://storage.location\",\n"
        + "    \"convertedFileName\": \"\",\n"
        + "    \"convertedFileLocation\": \"\",\n"
        + "    \"socialLink\": \"\",\n"
        + "    \"socialLinkDownloaded\": false,\n"
        + "    \"socialLinkDownloadFailed\": false,\n"
        + "    \"requestId\": \""
        + requestId
        + "\",\n"
        + "    \"uploadedDate\": null,\n"
        + "    \"mediaType\": \"IMAGE\",\n"
        + "    \"userInfo\": null,\n"
        + "    \"audioExtractionFileName\": \"\",\n"
        + "    \"showAudioResult\": false,\n"
        + "    \"audioRequestId\": \"\",\n"
        + "    \"thumbnail\": \"\",\n"
        + "    \"contentPreview\": null,\n"
        + "    \"userId\": \"test-user-id\",\n"
        + "    \"institutionId\": \"test-institution-id\",\n"
        + "    \"releaseVersion\": \"1.0.0\",\n"
        + "    \"webhookUrls\": [],\n"
        + "    \"createdAt\": null,\n"
        + "    \"updatedAt\": null,\n"
        + "    \"audioExtractionProcessed\": false,\n"
        + "    \"overallStatus\": \""
        + status
        + "\",\n"
        + "    \"resultsSummary\": {\n"
        + "        \"status\": \""
        + status
        + "\",\n"
        + "        \"metadata\": {}\n"
        + "    },\n"
        + "    \"models\": "
        + modelsJson
        + ",\n"
        + "    \"rdModels\": [],\n"
        + "    \"media_metadata_info\": null,\n"
        + "    \"modelMetadataUrl\": \"\",\n"
        + "    \"explainabilityUrl\": \"\",\n"
        + "    \"heatmaps\": {}\n"
        + "}";
  }

  private String createModelsJson() {
    return "[\n"
        + "    {\n"
        + "        \"name\": \"model1\",\n"
        + "        \"data\": null,\n"
        + "        \"error\": null,\n"
        + "        \"code\": null,\n"
        + "        \"status\": \"FAKE\",\n"
        + "        \"predictionNumber\": 0.95,\n"
        + "        \"normalizedPredictionNumber\": 0.95,\n"
        + "        \"rollingAvgNumber\": 0.95,\n"
        + "        \"finalScore\": 0.95\n"
        + "    }\n"
        + "]";
  }
}

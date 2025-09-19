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
    detectionService = new DetectionService(httpClient, Duration.ofSeconds(2));
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
    assertEquals("MANIPULATED", result.getStatus());
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
    assertEquals("AUTHENTIC", result.getStatus());
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
    assertEquals("MANIPULATED", result.getStatus());
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
    assertEquals("AUTHENTIC", result.getStatus());
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
    assertEquals("PROCESSING", result.getStatus());
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
    assertEquals("MANIPULATED", resultRef.get().getStatus());
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
    assertEquals("MANIPULATED", result.getStatus());
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
    assertEquals("ANALYZING", result.getStatus());
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
      assertEquals(status, result.getStatus());
    }

    for (String status : completedStatuses) {
      String responseJson = createDetectionResultJson(status, "req-123", "[]");
      JsonNode response = objectMapper.readTree(responseJson);
      when(httpClient.getResults("req-123")).thenReturn(response);

      DetectionResult result = detectionService.checkStatus("req-123");
      assertEquals(status, result.getStatus());
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
    // Arrange - Always return processing status
    String processingResponseJson = createDetectionResultJson("PROCESSING", "req-123", "[]");
    JsonNode processingResponse = objectMapper.readTree(processingResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(processingResponse);

    // Act & Assert
    assertThrows(
        RealityDefenderException.class,
        () -> detectionService.getResult("req-123", Duration.ofMillis(10), 30));
  }

  @Test
  void testUploadSocialMediaSuccess() throws Exception {
    String testUrl = "https://twitter.com/example/status/123";
    String socialMediaResponseJson = "{\"requestId\": \"social-req-123\"}";
    JsonNode socialMediaResponse = objectMapper.readTree(socialMediaResponseJson);
    when(httpClient.postSocialMedia(testUrl)).thenReturn(socialMediaResponse);

    UploadResponse result = detectionService.uploadSocialMedia(testUrl);

    assertEquals("social-req-123", result.getRequestId());
    assertNull(result.getMediaId());
    verify(httpClient).postSocialMedia(testUrl);
  }

  @Test
  void testUploadSocialMediaHttpClientException() throws Exception {
    String testUrl = "https://instagram.com/p/ABC123/";
    when(httpClient.postSocialMedia(testUrl))
        .thenThrow(new RealityDefenderException("Invalid URL", "invalid_request"));

    assertThrows(RealityDefenderException.class, () -> detectionService.uploadSocialMedia(testUrl));
    verify(httpClient).postSocialMedia(testUrl);
  }

  @Test
  void testUploadSocialMediaParseException() throws Exception {
    String testUrl = "https://youtube.com/watch?v=test123";
    JsonNode invalidResponse = objectMapper.readTree("{}");
    when(httpClient.postSocialMedia(testUrl)).thenReturn(invalidResponse);

    RealityDefenderException exception =
        assertThrows(
            RealityDefenderException.class, () -> detectionService.uploadSocialMedia(testUrl));

    assertEquals("Failed to parse upload response", exception.getMessage());
  }

  @Test
  void testUploadSocialMediaWithComplexUrls() throws Exception {
    String complexUrl = "https://facebook.com/user/posts/123?ref=share#comments";
    String socialMediaResponseJson = "{\"requestId\": \"complex-req-456\"}";
    JsonNode socialMediaResponse = objectMapper.readTree(socialMediaResponseJson);
    when(httpClient.postSocialMedia(complexUrl)).thenReturn(socialMediaResponse);

    UploadResponse result = detectionService.uploadSocialMedia(complexUrl);

    assertEquals("complex-req-456", result.getRequestId());
    assertNull(result.getMediaId());
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
        + "    \"status\": \""
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

  @Test
  void testMaxAttemptsCalculationFromTimeoutConfiguration() throws Exception {
    // Arrange - Create DetectionService with different timeout values to test maxAttempts calculation
    DetectionService shortTimeoutService = new DetectionService(httpClient, Duration.ofSeconds(4)); // Should result in maxAttempts = 2
    DetectionService longTimeoutService = new DetectionService(httpClient, Duration.ofSeconds(10)); // Should result in maxAttempts = 5

    String processingResponseJson = createDetectionResultJson("PROCESSING", "req-123", "[]");
    JsonNode processingResponse = objectMapper.readTree(processingResponseJson);
    when(httpClient.getResults("req-123")).thenReturn(processingResponse);

    // Act & Assert - Short timeout service should timeout after fewer attempts (2 attempts)
    assertThrows(
      RealityDefenderException.class,
      () -> shortTimeoutService.getResult("req-123"));

    // Verify the number of calls matches expected maxAttempts for short timeout (2 calls)
    verify(httpClient, times(2)).getResults("req-123");

    // Reset mock for next test
    reset(httpClient);
    when(httpClient.getResults("req-456")).thenReturn(processingResponse);

    // Act & Assert - Long timeout service should make more attempts (5 attempts)
    assertThrows(
      RealityDefenderException.class,
      () -> longTimeoutService.getResult("req-456"));

    // Verify the number of calls matches expected maxAttempts for long timeout (5 calls)
    verify(httpClient, times(5)).getResults("req-456");
  }

  @Test
  void testCustomMaxAttemptsOverridesDefaultBehavior() throws Exception {
    // Arrange - Use custom maxAttempts parameter that overrides the instance default
    int customMaxAttempts = 3;
    Duration customPollingInterval = Duration.ofMillis(50);

    String processingResponseJson = createDetectionResultJson("PROCESSING", "req-789", "[]");
    JsonNode processingResponse = objectMapper.readTree(processingResponseJson);
    when(httpClient.getResults("req-789")).thenReturn(processingResponse);

    // Act - Call getResult with custom maxAttempts that should override the default instance setting
    assertThrows(
      RealityDefenderException.class,
      () -> detectionService.getResult("req-789", customPollingInterval, customMaxAttempts));

    // Assert - Verify exactly the custom number of attempts were made
    verify(httpClient, times(customMaxAttempts)).getResults("req-789");

    // Test that the custom maxAttempts works with eventual success
    reset(httpClient);
    String completedResponseJson = createDetectionResultJson("AUTHENTIC", "req-success", "[]");
    JsonNode completedResponse = objectMapper.readTree(completedResponseJson);

    // Mock to return processing twice, then success on third attempt
    when(httpClient.getResults("req-success"))
      .thenReturn(processingResponse)
      .thenReturn(processingResponse)
      .thenReturn(completedResponse);

    // Act - Should succeed on the third attempt within our custom maxAttempts limit
    DetectionResult result = detectionService.getResult("req-success", customPollingInterval, customMaxAttempts);

    // Assert - Should complete successfully and make exactly 3 calls
    assertEquals("AUTHENTIC", result.getStatus());
    assertEquals("req-success", result.getRequestId());
    verify(httpClient, times(3)).getResults("req-success");
  }
}

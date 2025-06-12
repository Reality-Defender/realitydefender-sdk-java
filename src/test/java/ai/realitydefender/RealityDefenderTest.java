package ai.realitydefender;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import ai.realitydefender.core.RealityDefenderConfig;
import ai.realitydefender.detection.DetectionService;
import ai.realitydefender.exceptions.RealityDefenderException;
import ai.realitydefender.models.DetectionResult;
import ai.realitydefender.models.UploadResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RealityDefenderTest {

  @Mock private DetectionService detectionService;

  private RealityDefender realityDefender;
  private RealityDefenderConfig config;
  private File testFile;

  @BeforeEach
  void setUp() {
    config =
        new RealityDefenderConfig("test-api-key", "https://api.test.com", Duration.ofSeconds(30));
    realityDefender = new RealityDefender(config, detectionService);
    testFile = new File("test.jpg");
  }

  @Test
  void testBuilderWithValidApiKey() {
    RealityDefender client = RealityDefender.builder().apiKey("valid-api-key").build();

    assertThat(client).isNotNull();
  }

  @Test
  void testBuilderWithCustomConfiguration() {
    RealityDefender client =
        RealityDefender.builder()
            .apiKey("test-key")
            .baseUrl("https://custom.api.com")
            .timeout(Duration.ofMinutes(1))
            .build();

    assertThat(client).isNotNull();
  }

  @Test
  void testBuilderThrowsExceptionWhenApiKeyIsNull() {
    assertThatThrownBy(() -> RealityDefender.builder().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("API key is required");
  }

  @Test
  void testBuilderThrowsExceptionWhenApiKeyIsEmpty() {
    assertThatThrownBy(() -> RealityDefender.builder().apiKey("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("API key is required");
  }

  @Test
  void testBuilderThrowsExceptionWhenApiKeyIsWhitespace() {
    assertThatThrownBy(() -> RealityDefender.builder().apiKey("   ").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("API key is required");
  }

  @Test
  void testUpload() throws RealityDefenderException {
    UploadResponse expectedResponse = new UploadResponse("request-123", "media-456");
    when(detectionService.upload(testFile)).thenReturn(expectedResponse);

    UploadResponse result = realityDefender.upload(testFile);

    assertThat(result).isEqualTo(expectedResponse);
    verify(detectionService).upload(testFile);
  }

  @Test
  void testUploadAsync() throws ExecutionException, InterruptedException {
    UploadResponse expectedResponse = new UploadResponse("request-123", "media-456");
    when(detectionService.uploadAsync(testFile))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    CompletableFuture<UploadResponse> result = realityDefender.uploadAsync(testFile);

    assertThat(result.get()).isEqualTo(expectedResponse);
    verify(detectionService).uploadAsync(testFile);
  }

  @Test
  void testGetResult() throws RealityDefenderException, JsonProcessingException {
    DetectionResult expectedResult = createDetectionResult();
    when(detectionService.getResult("request-123")).thenReturn(expectedResult);

    DetectionResult result = realityDefender.getResult("request-123");

    assertThat(result).isEqualTo(expectedResult);
    verify(detectionService).getResult("request-123");
  }

  @Test
  void testGetResultWithPollingSettings() throws RealityDefenderException, JsonProcessingException {
    DetectionResult expectedResult = createDetectionResult();
    Duration pollingInterval = Duration.ofSeconds(5);
    Duration timeout = Duration.ofMinutes(2);

    when(detectionService.getResult("request-123", pollingInterval, timeout))
        .thenReturn(expectedResult);

    DetectionResult result = realityDefender.getResult("request-123", pollingInterval, timeout);

    assertThat(result).isEqualTo(expectedResult);
    verify(detectionService).getResult("request-123", pollingInterval, timeout);
  }

  @Test
  void testGetResultAsync() throws ExecutionException, InterruptedException {
    DetectionResult expectedResult = createDetectionResult();
    when(detectionService.getResultAsync("request-123"))
        .thenReturn(CompletableFuture.completedFuture(expectedResult));

    CompletableFuture<DetectionResult> result = realityDefender.getResultAsync("request-123");

    assertThat(result.get()).isEqualTo(expectedResult);
    verify(detectionService).getResultAsync("request-123");
  }

  @Test
  void testGetResultAsyncWithSettings() throws ExecutionException, InterruptedException {
    DetectionResult expectedResult = createDetectionResult();
    Duration pollingInterval = Duration.ofSeconds(3);
    Duration timeout = Duration.ofMinutes(1);

    when(detectionService.getResultAsync("request-123", pollingInterval, timeout))
        .thenReturn(CompletableFuture.completedFuture(expectedResult));

    CompletableFuture<DetectionResult> result =
        realityDefender.getResultAsync("request-123", pollingInterval, timeout);

    assertThat(result.get()).isEqualTo(expectedResult);
    verify(detectionService).getResultAsync("request-123", pollingInterval, timeout);
  }

  @Test
  void testDetectFile() throws RealityDefenderException, JsonProcessingException {
    DetectionResult expectedResult = createDetectionResult();
    when(detectionService.detectFile(testFile)).thenReturn(expectedResult);

    DetectionResult result = realityDefender.detectFile(testFile);

    assertThat(result).isEqualTo(expectedResult);
    verify(detectionService).detectFile(testFile);
  }

  @Test
  void testDetectFileAsync() throws ExecutionException, InterruptedException {
    DetectionResult expectedResult = createDetectionResult();
    when(detectionService.detectFileAsync(testFile))
        .thenReturn(CompletableFuture.completedFuture(expectedResult));

    CompletableFuture<DetectionResult> result = realityDefender.detectFileAsync(testFile);

    assertThat(result.get()).isEqualTo(expectedResult);
    verify(detectionService).detectFileAsync(testFile);
  }

  @Test
  void testCheckStatus() throws RealityDefenderException, JsonProcessingException {
    DetectionResult expectedResult = createDetectionResult();
    when(detectionService.checkStatus("request-123")).thenReturn(expectedResult);

    DetectionResult result = realityDefender.checkStatus("request-123");

    assertThat(result).isEqualTo(expectedResult);
    verify(detectionService).checkStatus("request-123");
  }

  @Test
  void testCheckStatusAsync() throws ExecutionException, InterruptedException {
    DetectionResult expectedResult = createDetectionResult();
    when(detectionService.checkStatusAsync("request-123"))
        .thenReturn(CompletableFuture.completedFuture(expectedResult));

    CompletableFuture<DetectionResult> result = realityDefender.checkStatusAsync("request-123");

    assertThat(result.get()).isEqualTo(expectedResult);
    verify(detectionService).checkStatusAsync("request-123");
  }

  @Test
  void testPollForResults() {
    Duration pollingInterval = Duration.ofSeconds(2);
    Duration timeout = Duration.ofMinutes(1);
    Consumer<DetectionResult> onResult = mock(Consumer.class);
    Consumer<RealityDefenderException> onError = mock(Consumer.class);

    realityDefender.pollForResults("request-123", pollingInterval, timeout, onResult, onError);

    verify(detectionService)
        .pollForResults("request-123", pollingInterval, timeout, onResult, onError);
  }

  @Test
  void testPollForResultsAsync() throws ExecutionException, InterruptedException {
    DetectionResult expectedResult = createDetectionResult();
    Duration pollingInterval = Duration.ofSeconds(2);
    Duration timeout = Duration.ofMinutes(1);

    when(detectionService.pollForResultsAsync("request-123", pollingInterval, timeout))
        .thenReturn(CompletableFuture.completedFuture(expectedResult));

    CompletableFuture<DetectionResult> result =
        realityDefender.pollForResultsAsync("request-123", pollingInterval, timeout);

    assertThat(result.get()).isEqualTo(expectedResult);
    verify(detectionService).pollForResultsAsync("request-123", pollingInterval, timeout);
  }

  @Test
  void testClose() throws IOException {
    realityDefender.close();

    verify(detectionService).shutdown();
  }

  @Test
  void testCloseWithNullDetectionService() throws IOException {
    RealityDefender clientWithNullService = new RealityDefender(config, null);

    // Should not throw exception
    assertThatCode(() -> clientWithNullService.close()).doesNotThrowAnyException();
  }

  private DetectionResult createDetectionResult() {
    return new DetectionResult("completed", 1.0, new ArrayList<>());
  }
}

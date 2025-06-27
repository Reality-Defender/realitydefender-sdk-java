package ai.realitydefender;

import ai.realitydefender.client.HttpClient;
import ai.realitydefender.core.RealityDefenderConfig;
import ai.realitydefender.detection.DetectionService;
import ai.realitydefender.exceptions.RealityDefenderException;
import ai.realitydefender.models.DetectionResult;
import ai.realitydefender.models.UploadResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.Closeable;
import java.io.File;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Main SDK client for Reality Defender API.
 *
 * <p>This client provides both synchronous and asynchronous methods for detecting deepfakes and
 * manipulated media through the Reality Defender API.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * RealityDefender client = RealityDefender.builder()
 *     .apiKey("your-api-key")
 *     .build();
 *
 * try {
 *     // Synchronous detection
 *     DetectionResult result = client.detectFile(new File("image.jpg"));
 *     System.out.println("Detection result: " + result.getStatus());
 *
 *     // Asynchronous detection
 *     CompletableFuture<DetectionResult> future = client.detectFileAsync(new File("image.jpg"));
 *     future.thenAccept(r -> System.out.println("Async result: " + r.getStatus()));
 * } finally {
 *     client.close();
 * }
 * }</pre>
 */
public class RealityDefender implements Closeable {

  private final HttpClient httpClient;
  private final DetectionService detectionService;

  /**
   * Creates a new RealityDefender client with the specified configuration.
   *
   * @param config the configuration for the client
   */
  public RealityDefender(RealityDefenderConfig config) {
    this.httpClient = new HttpClient(config);
    this.detectionService = new DetectionService(httpClient);
  }

  /** Package-private constructor for testing. */
  RealityDefender(RealityDefenderConfig config, DetectionService detectionService) {
    this.httpClient = null; // Will be null in tests
    this.detectionService = detectionService;
  }

  /**
   * Creates a builder for configuring the RealityDefender client.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Uploads a file for analysis.
   *
   * @param file the file to upload
   * @return the upload response containing request and media IDs
   * @throws RealityDefenderException if an error occurs during upload
   */
  public UploadResponse upload(File file) throws RealityDefenderException {
    return detectionService.upload(file);
  }

  /**
   * Uploads a file for analysis asynchronously.
   *
   * @param file the file to upload
   * @return a CompletableFuture containing the upload response
   */
  public CompletableFuture<UploadResponse> uploadAsync(File file) {
    return detectionService.uploadAsync(file);
  }

  /**
   * Gets the detection result for a request ID.
   *
   * @param requestId the request ID from the upload response
   * @return the detection result
   * @throws RealityDefenderException if an error occurs while getting results
   */
  public DetectionResult getResult(String requestId)
      throws RealityDefenderException, JsonProcessingException {
    return detectionService.getResult(requestId);
  }

  /**
   * Gets the detection result for a request ID with custom polling settings.
   *
   * @param requestId the request ID from the upload response
   * @param pollingInterval interval between polling attempts
   * @param maxAttempts maximum number of attempts
   * @return the detection result
   * @throws RealityDefenderException if an error occurs while getting results
   */
  public DetectionResult getResult(String requestId, Duration pollingInterval, Integer maxAttempts)
      throws RealityDefenderException, JsonProcessingException {
    return detectionService.getResult(requestId, pollingInterval, maxAttempts);
  }

  /**
   * Gets the detection result for a request ID asynchronously.
   *
   * @param requestId the request ID from the upload response
   * @return a CompletableFuture containing the detection result
   */
  public CompletableFuture<DetectionResult> getResultAsync(String requestId) {
    return detectionService.getResultAsync(requestId);
  }

  /**
   * Gets the detection result for a request ID asynchronously with custom settings.
   *
   * @param requestId the request ID from the upload response
   * @param pollingInterval interval between polling attempts
   * @param maxAttempts
   * @return a CompletableFuture containing the detection result
   */
  public CompletableFuture<DetectionResult> getResultAsync(
      String requestId, Duration pollingInterval, Integer maxAttempts) {
    return detectionService.getResultAsync(requestId, pollingInterval, maxAttempts);
  }

  /**
   * Detects a file in one step (upload and wait for results).
   *
   * @param file the file to analyze
   * @return the detection result
   * @throws RealityDefenderException if an error occurs during detection
   */
  public DetectionResult detectFile(File file)
      throws RealityDefenderException, JsonProcessingException {
    return detectionService.detectFile(file);
  }

  /**
   * Detects a file in one step asynchronously.
   *
   * @param file the file to analyze
   * @return a CompletableFuture containing the detection result
   */
  public CompletableFuture<DetectionResult> detectFileAsync(File file) {
    return detectionService.detectFileAsync(file);
  }

  /**
   * Checks the current status of a detection without polling.
   *
   * @param requestId the request ID to check
   * @return the current detection result
   * @throws RealityDefenderException if an error occurs while checking status
   */
  public DetectionResult checkStatus(String requestId)
      throws RealityDefenderException, JsonProcessingException {
    return detectionService.checkStatus(requestId);
  }

  /**
   * Checks the current status of a detection asynchronously.
   *
   * @param requestId the request ID to check
   * @return a CompletableFuture containing the current detection result
   */
  public CompletableFuture<DetectionResult> checkStatusAsync(String requestId) {
    return detectionService.checkStatusAsync(requestId);
  }

  /**
   * Polls for results with callbacks.
   *
   * @param requestId the request ID to poll for
   * @param pollingInterval the interval between polls
   * @param timeout the maximum time to wait
   * @param onResult callback for when results are available
   * @param onError callback for when an error occurs
   */
  public void pollForResults(
      String requestId,
      Duration pollingInterval,
      Duration timeout,
      Consumer<DetectionResult> onResult,
      Consumer<RealityDefenderException> onError) {
    detectionService.pollForResults(requestId, pollingInterval, timeout, onResult, onError);
  }

  /**
   * Polls for results asynchronously.
   *
   * @param requestId the request ID to poll for
   * @param pollingInterval the interval between polls
   * @param timeout the maximum time to wait
   * @return a CompletableFuture that completes when results are available
   */
  public CompletableFuture<DetectionResult> pollForResultsAsync(
      String requestId, Duration pollingInterval, Duration timeout) {
    return detectionService.pollForResultsAsync(requestId, pollingInterval, timeout);
  }

  @Override
  public void close() {
    if (detectionService != null) {
      detectionService.close();
    }
    if (httpClient != null) {
      httpClient.close();
    }
  }

  /** Builder for configuring RealityDefender instances. */
  public static class Builder {
    private String apiKey;
    private String baseUrl = "https://api.prd.realitydefender.xyz";
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Sets the API key.
     *
     * @param apiKey the API key
     * @return this builder
     */
    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    /**
     * Sets the base URL for the API.
     *
     * @param baseUrl the base URL
     * @return this builder
     */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /**
     * Sets the request timeout.
     *
     * @param timeout the timeout duration
     * @return this builder
     */
    public Builder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    /**
     * Builds the RealityDefender client.
     *
     * @return a new RealityDefender instance
     * @throws IllegalArgumentException if required configuration is missing
     */
    public RealityDefender build() {
      if (apiKey == null || apiKey.trim().isEmpty()) {
        throw new IllegalArgumentException("API key is required");
      }
      return new RealityDefender(new RealityDefenderConfig(apiKey, baseUrl, timeout));
    }
  }
}

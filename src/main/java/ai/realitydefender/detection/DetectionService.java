package ai.realitydefender.detection;

import ai.realitydefender.client.HttpClient;
import ai.realitydefender.exceptions.RealityDefenderException;
import ai.realitydefender.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.Closeable;
import java.io.File;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling file uploads and detection operations.
 */
public class DetectionService implements Closeable {

  private static final Logger logger = LoggerFactory.getLogger(DetectionService.class);
  private static final String STATUS_PROCESSING = "PROCESSING";
  private static final String STATUS_ANALYZING = "ANALYZING";
  private static final String STATUS_QUEUED = "QUEUED";

  private static final Duration DEFAULT_POLLING_INTERVAL = Duration.ofSeconds(2);

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final ScheduledExecutorService scheduler;

  private final int maxAttempts;

  public DetectionService(HttpClient httpClient, Duration timeout) {
    this.httpClient = httpClient;
    this.objectMapper =
      JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .configure(
          com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
          false)
        .build();
    this.scheduler = Executors.newScheduledThreadPool(2);
    this.maxAttempts = timeout.toSeconds() > 0 ? (int) (timeout.toSeconds() / DEFAULT_POLLING_INTERVAL.toSeconds()) : 1;
  }

  /**
   * Uploads a file for analysis.
   *
   * @param file the file to upload
   * @return the upload response
   * @throws RealityDefenderException if upload fails
   */
  public UploadResponse upload(File file) throws RealityDefenderException {
    logger.info("Uploading file: {}", file.getName());

    JsonNode response = httpClient.uploadFile(file);

    try {
      UploadResponse uploadResponse = objectMapper.treeToValue(response, UploadResponse.class);
      logger.info(
        "File uploaded successfully. Request ID: {}, Media ID: {}",
        uploadResponse.getRequestId(),
        uploadResponse.getMediaId());
      return uploadResponse;
    } catch (Exception e) {
      throw new RealityDefenderException("Failed to parse upload response", "PARSE_ERROR", e);
    }
  }

  /**
   * Uploads a file for analysis asynchronously.
   *
   * @param file the file to upload
   * @return a CompletableFuture containing the upload response
   */
  public CompletableFuture<UploadResponse> uploadAsync(File file) {
    return CompletableFuture.supplyAsync(
      () -> {
        try {
          return upload(file);
        } catch (RealityDefenderException e) {
          throw new RuntimeException(e);
        }
      });
  }

  /**
   * Uploads a social media link for analysis.
   *
   * @param url the URL to upload
   * @return the upload response
   * @throws RealityDefenderException if upload fails
   */
  public UploadResponse uploadSocialMedia(String url) throws RealityDefenderException {
    JsonNode response = httpClient.postSocialMedia(url);

    try {
      SocialMediaResponse uploadResponse =
        objectMapper.treeToValue(response, SocialMediaResponse.class);
      logger.info(
        "Social media link uploaded successfully. Request ID: {}", uploadResponse.getRequestId());

      return new UploadResponse(uploadResponse.getRequestId(), null);
    } catch (Exception e) {
      throw new RealityDefenderException("Failed to parse upload response", "server_error", e);
    }
  }

  /**
   * Returns the summarized detection result for a request ID, polling until complete.
   *
   * @param requestId the request ID from upload
   * @return the detection result
   * @throws RealityDefenderException if getting results fails
   */
  public DetectionResult getResult(String requestId)
    throws RealityDefenderException, JsonProcessingException {
    return getResult(requestId, DEFAULT_POLLING_INTERVAL, this.maxAttempts).summarize();
  }

  /**
   * Gets the detection result for a request ID with custom polling settings.
   *
   * @param requestId       the request ID from upload
   * @param pollingInterval interval between polling attempts
   * @param maxAttempts     maximum number of attempts.
   * @return the detection result
   * @throws RealityDefenderException if getting results fails
   */
  public DetectionResult getResult(String requestId, Duration pollingInterval, Integer maxAttempts)
    throws RealityDefenderException, JsonProcessingException {
    logger.info("Getting results for request ID: {}", requestId);

    for (int i = 0; i < maxAttempts; i++) {
      try {
        JsonNode response = httpClient.getResults(requestId);
        DetectionResult result = objectMapper.treeToValue(response, DetectionResult.class);

        if (isProcessed(result.getStatus())) {
          logger.info(
            "Detection completed for request ID: {} with status: {}",
            requestId,
            result.getStatus());
          return result.summarize();
        }

        logger.debug(
          "Detection still processing for request ID: {}, result: {}",
          requestId,
          result);

        Thread.sleep(pollingInterval.toMillis());

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RealityDefenderException("Polling interrupted", "INTERRUPTED", e);
      } catch (Exception e) {
        if (e instanceof RealityDefenderException) {
          throw e;
        }
        throw new RealityDefenderException("Failed to get results", "RESULTS_FAILED", e);
      }
    }

    throw new RealityDefenderException("Timeout waiting for results", "TIMEOUT");
  }

  /**
   * Gets the detection result for a request ID asynchronously.
   *
   * @param requestId the request ID from upload
   * @return a CompletableFuture containing the detection result
   */
  public CompletableFuture<DetectionResult> getResultAsync(String requestId) {
    return getResultAsync(requestId, DEFAULT_POLLING_INTERVAL, this.maxAttempts);
  }

  /**
   * Gets the detection result for a request ID asynchronously with custom settings.
   *
   * @param requestId       the request ID from upload
   * @param pollingInterval interval between polling attempts
   * @param maxAttempts     maximum number of attempts
   * @return a CompletableFuture containing the detection result
   */
  public CompletableFuture<DetectionResult> getResultAsync(
    String requestId, Duration pollingInterval, Integer maxAttempts) {
    return CompletableFuture.supplyAsync(
      () -> {
        try {
          return getResult(requestId, pollingInterval, maxAttempts);
        } catch (RealityDefenderException | JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      });
  }

  /**
   * Detects a file in one step (upload and wait for results).
   *
   * @param file the file to analyze
   * @return the detection result
   * @throws RealityDefenderException if detection fails
   */
  public DetectionResult detectFile(File file)
    throws RealityDefenderException, JsonProcessingException {
    UploadResponse uploadResponse = upload(file);
    return getResult(uploadResponse.getRequestId());
  }

  /**
   * Detects a file in one step asynchronously.
   *
   * @param file the file to analyze
   * @return a CompletableFuture containing the detection result
   */
  public CompletableFuture<DetectionResult> detectFileAsync(File file) {
    return uploadAsync(file)
      .thenCompose(uploadResponse -> getResultAsync(uploadResponse.getRequestId(), DEFAULT_POLLING_INTERVAL, this.maxAttempts));
  }

  /**
   * Polls for results with callbacks.
   *
   * @param requestId       the request ID to poll for
   * @param pollingInterval the interval between polls
   * @param timeout         the maximum time to wait
   * @param onResult        callback for when results are available
   * @param onError         callback for when an error occurs
   */
  public void pollForResults(
    String requestId,
    Duration pollingInterval,
    Duration timeout,
    Consumer<DetectionResult> onResult,
    Consumer<RealityDefenderException> onError) {

    logger.info("Starting polling for request ID: {}", requestId);

    final long startTime = System.currentTimeMillis();
    final long timeoutMillis = timeout.toMillis();

    Runnable pollTask =
      new Runnable() {
        @Override
        public void run() {
          try {
            if (System.currentTimeMillis() - startTime >= timeoutMillis) {
              onError.accept(
                new RealityDefenderException("Timeout waiting for results", "TIMEOUT"));
              return;
            }

            JsonNode response = httpClient.getResults(requestId);
            DetectionResult result = objectMapper.treeToValue(response, DetectionResult.class);

            if (isProcessed(result.getStatus())) {
              logger.info(
                "Polling completed for request ID: {} with status: {}",
                requestId,
                result.getStatus());
              onResult.accept(result.summarize());
            } else {
              logger.debug(
                "Still processing for request ID: {}, status: {}",
                requestId,
                result.getStatus());
              // Schedule next poll
              scheduler.schedule(this, pollingInterval.toMillis(), TimeUnit.MILLISECONDS);
            }

          } catch (Exception e) {
            RealityDefenderException rde =
              (e instanceof RealityDefenderException)
                ? (RealityDefenderException) e
                : new RealityDefenderException("Polling failed", "POLLING_ERROR", e);
            onError.accept(rde);
          }
        }
      };

    // Start polling immediately
    scheduler.execute(pollTask);
  }

  /**
   * Polls for results asynchronously.
   *
   * @param requestId       the request ID to poll for
   * @param pollingInterval the interval between polls
   * @param timeout         the maximum time to wait
   * @return a CompletableFuture that completes when results are available
   */
  public CompletableFuture<DetectionResult> pollForResultsAsync(
    String requestId, Duration pollingInterval, Duration timeout) {
    CompletableFuture<DetectionResult> future = new CompletableFuture<>();

    pollForResults(
      requestId, pollingInterval, timeout, future::complete, future::completeExceptionally);

    return future;
  }

  /**
   * Checks a single summarized result status without polling.
   *
   * @param requestId the request ID to check
   * @return the current detection result
   * @throws RealityDefenderException if the check fails
   */
  public DetectionResult checkStatus(String requestId)
    throws RealityDefenderException, JsonProcessingException {
    logger.debug("Checking status for request ID: {}", requestId);

    try {
      JsonNode response = httpClient.getResults(requestId);
      DetectionResult result = objectMapper.treeToValue(response, DetectionResult.class);
      return result.summarize();
    } catch (Exception e) {
      if (e instanceof RealityDefenderException) {
        throw e;
      }
      throw new RealityDefenderException("Failed to check status", "STATUS_CHECK_FAILED", e);
    }
  }

  /**
   * Checks a single result status asynchronously.
   *
   * @param requestId the request ID to check
   * @return a CompletableFuture containing the current detection result
   */
  public CompletableFuture<DetectionResult> checkStatusAsync(String requestId) {
    return CompletableFuture.supplyAsync(
      () -> {
        try {
          return checkStatus(requestId);
        } catch (RealityDefenderException | JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      });
  }

  /**
   * Determines if a status indicates the detection has been processed.
   *
   * @param status the status to check
   * @return true if it is processed, false otherwise
   */
  private boolean isProcessed(String status) {
    return !(status == null
      || "UNKNOWN".equalsIgnoreCase(status)
      || STATUS_PROCESSING.equalsIgnoreCase(status)
      || STATUS_ANALYZING.equalsIgnoreCase(status)
      || STATUS_QUEUED.equalsIgnoreCase(status));
  }

  /**
   * Gets paginated detection results with optional filters.
   *
   * @param options options for filtering and pagination
   * @return paginated list of detection results
   * @throws RealityDefenderException if getting results fails
   */
  public DetectionResultList getResults(GetResultsOptions options)
    throws RealityDefenderException, JsonProcessingException {
    if (options == null) {
      options = GetResultsOptions.builder().build();
    }

    int pageNumber = options.getPageNumber() != null ? options.getPageNumber() : 0;
    Integer size = options.getSize();
    String name = options.getName();
    java.time.LocalDate startDate = options.getStartDate();
    java.time.LocalDate endDate = options.getEndDate();
    int maxAttempts = options.getMaxAttempts() != null ? options.getMaxAttempts() : 1;
    Duration pollingInterval =
      options.getPollingInterval() != null ? options.getPollingInterval() : Duration.ofSeconds(2);

    logger.info("Getting paginated results for page: {}", pageNumber);

    for (int attempt = 0; attempt < maxAttempts; attempt++) {
      try {
        JsonNode response = httpClient.getResults(pageNumber, size, name, startDate, endDate);
        logger.debug("HTTP response received successfully, parsing to DetectionResultList...");
        DetectionResultList resultList =
          objectMapper.treeToValue(response, DetectionResultList.class);
        logger.debug(
          "DetectionResultList parsed successfully: {} items",
          resultList.getCurrentPageItemsCount());

        // Check if any results are still analyzing (if polling is enabled)
        if (maxAttempts > 1) {
          boolean stillAnalyzing =
            resultList.getItems().stream().anyMatch(item -> isAnalyzing(item.getStatus()));

          if (!stillAnalyzing) {
            logger.info("All results completed for page: {}", pageNumber);
            return resultList.summarize();
          }

          if (attempt < maxAttempts - 1) {
            logger.debug(
              "Some results still analyzing, waiting {} ms before retry",
              pollingInterval.toMillis());
            Thread.sleep(pollingInterval.toMillis());
          }
        } else {
          return resultList.summarize();
        }

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RealityDefenderException("Polling interrupted", "INTERRUPTED", e);
      } catch (Exception e) {
        if (e instanceof RealityDefenderException) {
          throw e;
        }
        throw new RealityDefenderException("Failed to get results", "RESULTS_FAILED", e);
      }
    }

    throw new RealityDefenderException("Timeout waiting for results", "TIMEOUT");
  }

  /**
   * Gets paginated detection results with optional filters asynchronously.
   *
   * @param options options for filtering and pagination
   * @return a CompletableFuture containing paginated list of detection results
   */
  public CompletableFuture<DetectionResultList> getResultsAsync(GetResultsOptions options) {
    return CompletableFuture.supplyAsync(
      () -> {
        try {
          return getResults(options);
        } catch (RealityDefenderException | JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      });
  }

  /**
   * Determines if a status indicates the detection is still analyzing.
   *
   * @param status the status to check
   * @return true if still analyzing, false if complete
   */
  private boolean isAnalyzing(String status) {
    return STATUS_ANALYZING.equalsIgnoreCase(status);
  }

  /**
   * Shuts down the internal scheduler.
   */
  @Override
  public void close() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
      try {
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
          scheduler.shutdownNow();
        }
      } catch (InterruptedException e) {
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }
}

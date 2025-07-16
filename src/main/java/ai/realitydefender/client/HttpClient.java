package ai.realitydefender.client;

import ai.realitydefender.core.RealityDefenderConfig;
import ai.realitydefender.exceptions.RealityDefenderException;
import ai.realitydefender.models.SignedUrlRequest;
import ai.realitydefender.models.SignedUrlResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** HTTP client for Reality Defender API communication. */
public class HttpClient implements Closeable {

  private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  private final OkHttpClient client;
  private final ObjectMapper objectMapper;
  private final RealityDefenderConfig config;

  public HttpClient(RealityDefenderConfig config) {
    this.config = config;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    this.client =
        new OkHttpClient.Builder()
            .connectTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .readTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .writeTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .build();
  }

  /**
   * Gets a signed URL for file upload.
   *
   * @param fileName the name of the file to get signed URL for
   * @return the signed URL response
   * @throws RealityDefenderException if the request fails
   */
  private SignedUrlResponse getSignedUrl(String fileName) throws RealityDefenderException {
    try {
      // Create request object
      SignedUrlRequest request = new SignedUrlRequest(fileName);

      // Make POST request to signed URL endpoint
      JsonNode response =
          post("/api/files/aws-presigned", objectMapper.writeValueAsString(request));

      // Convert response to SignedUrlResponse object
      return objectMapper.treeToValue(response, SignedUrlResponse.class);

    } catch (RealityDefenderException e) {
      // Re-throw RealityDefenderException as-is
      throw e;
    } catch (Exception e) {
      // Wrap other exceptions
      throw new RealityDefenderException(
          "Failed to get signed URL: " + e.getMessage(), "UNKNOWN_ERROR", e);
    }
  }

  /**
   * Uploads a file to the Reality Defender API.
   *
   * @param file the file to upload
   * @return JSON response as JsonNode
   * @throws RealityDefenderException if upload fails
   */
  public JsonNode uploadFile(File file) throws RealityDefenderException {
    if (!file.exists()) {
      throw new RealityDefenderException(
          "File not found: " + file.getAbsolutePath(), "INVALID_FILE");
    }

    if (!file.canRead()) {
      throw new RealityDefenderException(
          "Cannot read file: " + file.getAbsolutePath(), "INVALID_FILE");
    }

    SignedUrlResponse signedUrlResponse = getSignedUrl(file.getName());

    RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));

    Request request =
        new Request.Builder()
            .url(signedUrlResponse.getSignedUrl())
            .addHeader("X-API-KEY", config.getApiKey())
            .addHeader("User-Agent", "RealityDefender-Java-SDK/1.0.0")
            .addHeader("Content-Type", "application/octet-stream")
            .put(fileBody)
            .build();

    try (Response response = client.newCall(request).execute()) {
      handleResponse(response, "UPLOAD_FAILED");
      String uploadResponseJson =
          "{\n"
              + "    \"request_id\": \""
              + signedUrlResponse.getRequestId()
              + "\",\n"
              + "    \"media_id\": \""
              + signedUrlResponse.getMediaId()
              + "\"\n"
              + "}";
      return objectMapper.readTree(uploadResponseJson);
    } catch (IOException e) {
      throw new RealityDefenderException("Failed to upload file", "UPLOAD_FAILED", e);
    }
  }

  /**
   * Gets detection results for a request ID.
   *
   * @param requestId the request ID to check
   * @return JSON response as JsonNode
   * @throws RealityDefenderException if request fails
   */
  public JsonNode getResults(String requestId) throws RealityDefenderException {
    Request request =
        new Request.Builder()
            .url(config.getBaseUrl() + "/api/media/users/" + requestId)
            .addHeader("X-API-KEY", config.getApiKey())
            .addHeader("User-Agent", "RealityDefender-Java-SDK/1.0.0")
            .addHeader("Content-Type", "application/json")
            .get()
            .build();

    logger.debug("Getting results for request ID: {}", requestId);

    try (Response response = client.newCall(request).execute()) {
      return handleResponse(response, "RESULTS_FAILED");
    } catch (IOException e) {
      throw new RealityDefenderException("Failed to get results", "RESULTS_FAILED", e);
    }
  }

  /**
   * Gets paginated detection results with optional filters.
   *
   * @param pageNumber the page number (0-based)
   * @param size the number of results per page
   * @param name optional name filter
   * @param startDate optional start date filter
   * @param endDate optional end date filter
   * @return JSON response as JsonNode
   * @throws RealityDefenderException if request fails
   */
  public JsonNode getResults(
      int pageNumber, 
      Integer size, 
      String name, 
      java.time.LocalDate startDate, 
      java.time.LocalDate endDate) throws RealityDefenderException {
    
    // Use OkHttp's HttpUrl.Builder for proper URL construction
    okhttp3.HttpUrl.Builder urlBuilder = okhttp3.HttpUrl.parse(config.getBaseUrl() + "/api/v2/media/users/pages/" + pageNumber).newBuilder();
    
    // Build query parameters - always include size (default to 10 if not specified)
    int actualSize = size != null ? size : 10;
    urlBuilder.addQueryParameter("size", String.valueOf(actualSize));
    
    if (name != null && !name.trim().isEmpty()) {
      urlBuilder.addQueryParameter("name", name);
    }
    if (startDate != null) {
      urlBuilder.addQueryParameter("startDate", startDate.toString());
    }
    if (endDate != null) {
      urlBuilder.addQueryParameter("endDate", endDate.toString());
    }
    
    String finalUrl = urlBuilder.build().toString();
    Request request =
        new Request.Builder()
            .url(finalUrl)
            .addHeader("X-API-KEY", config.getApiKey())
            .addHeader("User-Agent", "RealityDefender-Java-SDK/1.0.0")
            .addHeader("Content-Type", "application/json")
            .get()
            .build();

    logger.debug("Getting paginated results for page: {}, URL: {}", pageNumber, finalUrl);

    try (Response response = client.newCall(request).execute()) {
      logger.debug("Response code: {}, isSuccessful: {}", response.code(), response.isSuccessful());
      return handleResponse(response, "RESULTS_FAILED");
    } catch (IOException e) {
      logger.error("IOException in getResults: {}", e.getMessage());
      throw new RealityDefenderException("Failed to get results", "RESULTS_FAILED", e);
    }
  }

  /**
   * Makes a generic POST request with JSON body.
   *
   * @param endpoint the API endpoint
   * @param jsonBody the JSON request body
   * @return JSON response as JsonNode
   * @throws RealityDefenderException if request fails
   */
  public JsonNode post(String endpoint, String jsonBody) throws RealityDefenderException {
    RequestBody body = RequestBody.create(jsonBody, JSON);
    Request request =
        new Request.Builder()
            .url(config.getBaseUrl() + endpoint)
            .addHeader("X-API-KEY", config.getApiKey())
            .addHeader("User-Agent", "RealityDefender-Java-SDK/1.0.0")
            .addHeader("Content-Type", "application/json; charset=UTF-8")
            .post(body)
            .build();

    logger.debug("POST request to: {}", endpoint);

    try (Response response = client.newCall(request).execute()) {
      return handleResponse(response, "REQUEST_FAILED");
    } catch (IOException e) {
      throw new RealityDefenderException("Request failed", "REQUEST_FAILED", e);
    }
  }

  private JsonNode handleResponse(Response response, String defaultErrorCode)
      throws RealityDefenderException {
    try {
      String responseBody = response.body() != null ? response.body().string() : "";
      logger.debug("Response body length: {}", responseBody.length());

      if (!response.isSuccessful()) {
        String errorCode = mapStatusCodeToErrorCode(response.code(), defaultErrorCode);
        String errorMessage = extractErrorMessage(responseBody, response.code());

        // Log the detailed error for debugging
        logger.error("HTTP {} error for URL {}: {}", response.code(), response.request().url(), responseBody);
        
        throw new RealityDefenderException(errorMessage, errorCode, response.code());
      }

      logger.debug("Parsing JSON response body...");
      JsonNode result = objectMapper.readTree(responseBody);
      logger.debug("JSON parsing successful, result: {}", result != null ? "non-null" : "null");
      return result;

    } catch (IOException e) {
      logger.error("JSON parsing error: {}", e.getMessage());
      throw new RealityDefenderException("Failed to parse response", "PARSE_ERROR", e);
    }
  }

  private String mapStatusCodeToErrorCode(int statusCode, String defaultCode) {
    switch (statusCode) {
      case 400:
        return "BAD_REQUEST";
      case 401:
        return "UNAUTHORIZED";
      case 403:
        return "FORBIDDEN";
      case 404:
        return "NOT_FOUND";
      case 413:
        return "FILE_TOO_LARGE";
      case 415:
        return "UNSUPPORTED_MEDIA_TYPE";
      case 429:
        return "RATE_LIMITED";
      case 500:
        return "SERVER_ERROR";
      case 502:
      case 503:
      case 504:
        return "SERVICE_UNAVAILABLE";
      default:
        return defaultCode;
    }
  }

  private String extractErrorMessage(String responseBody, int statusCode) {
    try {
      if (responseBody != null && !responseBody.trim().isEmpty()) {
        JsonNode errorNode = objectMapper.readTree(responseBody);
        if (errorNode.has("error")) {
          JsonNode error = errorNode.get("error");
          if (error.has("message")) {
            return error.get("message").asText();
          }
        }
        if (errorNode.has("message")) {
          return errorNode.get("message").asText();
        }
      }
    } catch (Exception e) {
      logger.warn("Failed to parse error response: {}", e.getMessage());
    }

    return "HTTP " + statusCode + ": " + getDefaultErrorMessage(statusCode);
  }

  private String getDefaultErrorMessage(int statusCode) {
    switch (statusCode) {
      case 400:
        return "Bad Request";
      case 401:
        return "Unauthorized - Check your API key";
      case 403:
        return "Forbidden";
      case 404:
        return "Not Found";
      case 413:
        return "File too large";
      case 415:
        return "Unsupported media type";
      case 429:
        return "Rate limit exceeded";
      case 500:
        return "Internal server error";
      case 502:
        return "Bad Gateway";
      case 503:
        return "Service Unavailable";
      case 504:
        return "Gateway Timeout";
      default:
        return "Request failed";
    }
  }

  @Override
  public void close() {
    if (client != null) {
      ExecutorService executor = client.dispatcher().executorService();
      executor.shutdown();
      try {
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        logger.warn("Interrupted while waiting for executor to terminate", e);
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      } finally {
        // Always try to evict connections
        try {
          client.connectionPool().evictAll();
        } catch (Exception e) {
          // Log but don't rethrow to avoid masking shutdown issues
          logger.warn("Error evicting connections during close", e);
        }
      }
    }
  }
}

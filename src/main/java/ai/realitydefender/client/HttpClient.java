package ai.realitydefender.client;

import ai.realitydefender.core.RealityDefenderConfig;
import ai.realitydefender.exceptions.RealityDefenderException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
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

    this.client =
        new OkHttpClient.Builder()
            .connectTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .readTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .writeTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .build();
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

    RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
    RequestBody requestBody =
        new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.getName(), fileBody)
            .build();

    Request request =
        new Request.Builder()
            .url(config.getBaseUrl() + "/v1/upload")
            .addHeader("Authorization", "Bearer " + config.getApiKey())
            .addHeader("User-Agent", "RealityDefender-Java-SDK/1.0.0")
            .post(requestBody)
            .build();

    try (Response response = client.newCall(request).execute()) {
      return handleResponse(response, "UPLOAD_FAILED");
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
            .url(config.getBaseUrl() + "/v1/results/" + requestId)
            .addHeader("Authorization", "Bearer " + config.getApiKey())
            .addHeader("User-Agent", "RealityDefender-Java-SDK/1.0.0")
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
   * Makes a generic GET request.
   *
   * @param endpoint the API endpoint
   * @return JSON response as JsonNode
   * @throws RealityDefenderException if request fails
   */
  public JsonNode get(String endpoint) throws RealityDefenderException {
    Request request =
        new Request.Builder()
            .url(config.getBaseUrl() + endpoint)
            .addHeader("Authorization", "Bearer " + config.getApiKey())
            .addHeader("User-Agent", "RealityDefender-Java-SDK/1.0.0")
            .get()
            .build();

    logger.debug("GET request to: {}", endpoint);

    try (Response response = client.newCall(request).execute()) {
      return handleResponse(response, "REQUEST_FAILED");
    } catch (IOException e) {
      throw new RealityDefenderException("Request failed", "REQUEST_FAILED", e);
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
            .addHeader("Authorization", "Bearer " + config.getApiKey())
            .addHeader("User-Agent", "RealityDefender-Java-SDK/1.0.0")
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

      if (!response.isSuccessful()) {
        String errorCode = mapStatusCodeToErrorCode(response.code(), defaultErrorCode);
        String errorMessage = extractErrorMessage(responseBody, response.code());

        throw new RealityDefenderException(errorMessage, errorCode, response.code());
      }

      return objectMapper.readTree(responseBody);

    } catch (IOException e) {
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
  public void close() throws IOException {
    if (client != null) {
      client.dispatcher().executorService().shutdown();
      client.connectionPool().evictAll();
    }
  }
}

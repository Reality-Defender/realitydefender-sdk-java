package ai.realitydefender.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import ai.realitydefender.core.RealityDefenderConfig;
import ai.realitydefender.exceptions.RealityDefenderException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.io.TempDir;

@DisabledInNativeImage
class HttpClientTest {

  private WireMockServer wireMockServer;
  private HttpClient httpClient;
  private RealityDefenderConfig config;

  @TempDir File tempDir;

  @BeforeEach
  void setUp() {
    // Start WireMock server
    wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMockServer.start();

    // Create config with WireMock server URL
    config =
        new RealityDefenderConfig(
            "test-api-key", "http://localhost:" + wireMockServer.port(), Duration.ofSeconds(30));

    // Create HttpClient
    httpClient = new HttpClient(config);
  }

  @AfterEach
  void tearDown() {
    if (httpClient != null) {
      httpClient.close();
    }
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  void testUploadFileSuccess() throws Exception {
    // Create a test file
    File testFile = new File(tempDir, "test.jpg");
    Files.write(testFile.toPath(), "test file content".getBytes());

    // Mock signed URL endpoint
    wireMockServer.stubFor(
        post(urlEqualTo("/api/files/aws-presigned"))
            .withHeader("X-API-KEY", equalTo("test-api-key"))
            .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
            .withRequestBody(containing("test.jpg"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\n"
                            + "  \"code\": \"ok\",\n"
                            + "  \"response\": {\n"
                            + "    \"signedUrl\": \"http://localhost:"
                            + wireMockServer.port()
                            + "/upload\"\n"
                            + "  },\n"
                            + "  \"errno\": 0,\n"
                            + "  \"mediaId\": \"media123\",\n"
                            + "  \"requestId\": \"req456\"\n"
                            + "}")));

    // Mock file upload endpoint
    wireMockServer.stubFor(
        put(urlEqualTo("/upload"))
            .withHeader("X-API-KEY", equalTo("test-api-key"))
            .withHeader("Content-Type", equalTo("application/octet-stream"))
            .willReturn(aResponse().withStatus(200)));

    JsonNode result = httpClient.uploadFile(testFile);

    assertNotNull(result);
    assertEquals("req456", result.get("request_id").asText());
    assertEquals("media123", result.get("media_id").asText());

    // Verify both requests were made
    wireMockServer.verify(postRequestedFor(urlEqualTo("/api/files/aws-presigned")));
    wireMockServer.verify(putRequestedFor(urlEqualTo("/upload")));
  }

  @Test
  void testUploadFileNotFound() {
    File nonExistentFile = new File(tempDir, "nonexistent.jpg");

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.uploadFile(nonExistentFile));

    assertEquals("INVALID_FILE", exception.getCode());
    assertTrue(exception.getMessage().contains("File not found"));
  }

  @Test
  void testUploadFileSignedUrlFails() throws Exception {
    File testFile = new File(tempDir, "test.jpg");
    Files.write(testFile.toPath(), "test content".getBytes());

    // Mock signed URL endpoint to return error
    wireMockServer.stubFor(
        post(urlEqualTo("/api/files/aws-presigned"))
            .willReturn(
                aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"response\": \"Invalid API key\"}")));

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.uploadFile(testFile));

    assertEquals("UNAUTHORIZED", exception.getCode());
    assertEquals(401, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("Invalid API key"));
  }

  @Test
  void testUploadFileUploadFails() throws Exception {
    File testFile = new File(tempDir, "test.jpg");
    Files.write(testFile.toPath(), "test content".getBytes());

    // Mock successful signed URL response
    wireMockServer.stubFor(
        post(urlEqualTo("/api/files/aws-presigned"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json; charset=UTF-8")
                    .withBody(
                        "{\n"
                            + "  \"code\": \"ok\",\n"
                            + "  \"response\": {\n"
                            + "    \"signedUrl\": \"http://localhost:"
                            + wireMockServer.port()
                            + "/upload\"\n"
                            + "  },\n"
                            + "  \"errno\": 0,\n"
                            + "  \"mediaId\": \"media123\",\n"
                            + "  \"requestId\": \"req456\"\n"
                            + "}")));

    // Mock failed upload
    wireMockServer.stubFor(
        put(urlEqualTo("/upload"))
            .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.uploadFile(testFile));

    assertEquals("SERVER_ERROR", exception.getCode());
    assertEquals(500, exception.getStatusCode());
  }

  @Test
  void testGetResultsSuccess() throws Exception {
    String requestId = "test-request-123";
    String expectedResponse =
        "{\n"
            + "  \"request_id\": \""
            + requestId
            + "\",\n"
            + "  \"status\": \"COMPLETED\",\n"
            + "  \"results\": {\n"
            + "    \"overall_status\": \"FAKE\"\n"
            + "  }\n"
            + "}";

    wireMockServer.stubFor(
        get(urlEqualTo("/api/media/users/" + requestId))
            .withHeader("X-API-KEY", equalTo("test-api-key"))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(expectedResponse)));

    JsonNode result = httpClient.getResults(requestId);

    assertNotNull(result);
    assertEquals(requestId, result.get("request_id").asText());
    assertEquals("COMPLETED", result.get("status").asText());

    wireMockServer.verify(getRequestedFor(urlEqualTo("/api/media/users/" + requestId)));
  }

  @Test
  void testGetResultsNotFound() {
    String requestId = "nonexistent-request";

    wireMockServer.stubFor(
        get(urlEqualTo("/api/media/users/" + requestId))
            .willReturn(
                aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"response\": \"Request not found\"}")));

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.getResults(requestId));

    assertEquals("NOT_FOUND", exception.getCode());
    assertEquals(404, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("Resource not found"));
  }

  @Test
  void testPostSuccess() throws Exception {
    String endpoint = "/api/test";
    String requestBody = "{\"test\": \"data\"}";
    String responseBody = "{\"success\": true, \"id\": \"12345\"}";

    wireMockServer.stubFor(
        post(urlEqualTo(endpoint))
            .withHeader("X-API-KEY", equalTo("test-api-key"))
            .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
            .withHeader("User-Agent", equalTo("RealityDefender-Java-SDK/1.0.0"))
            .withRequestBody(equalTo(requestBody))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody)));

    JsonNode result = httpClient.post(endpoint, requestBody);

    assertNotNull(result);
    assertTrue(result.get("success").asBoolean());
    assertEquals("12345", result.get("id").asText());

    wireMockServer.verify(
        postRequestedFor(urlEqualTo(endpoint)).withRequestBody(equalTo(requestBody)));
  }

  @Test
  void testPostWithBadRequest() {
    String endpoint = "/api/test";
    String requestBody = "{\"invalid\": \"data\"}";

    wireMockServer.stubFor(
        post(urlEqualTo(endpoint))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"code\": \"BAD_REQUEST\", \"response\": \"Invalid request format\", \"extra_field\": \"whatever\"}")));

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.post(endpoint, requestBody));

    assertEquals("INVALID_REQUEST", exception.getCode());
    assertEquals(400, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("Invalid request"));
  }

  @Test
  void testErrorCodeMapping() {
    testErrorResponse(401, "UNAUTHORIZED", "Invalid API key");
    testErrorResponse(404, "NOT_FOUND", "Resource not found");
  }

  private void testErrorResponse(int statusCode, String expectedErrorCode, String expectedMessage) {
    String endpoint = "/api/error-test-" + statusCode;

    wireMockServer.stubFor(
        post(urlEqualTo(endpoint)).willReturn(aResponse().withStatus(statusCode)));

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.post(endpoint, "{}"));

    assertEquals(expectedErrorCode, exception.getCode());
    assertEquals(statusCode, exception.getStatusCode());
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  void testEmptyResponseBody() {
    String endpoint = "/api/empty-response";

    wireMockServer.stubFor(
        post(urlEqualTo(endpoint)).willReturn(aResponse().withStatus(500).withBody("")));

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.post(endpoint, "{}"));

    assertEquals("SERVER_ERROR", exception.getCode());
    assertTrue(exception.getMessage().contains("Unknown error"));
  }

  @Test
  void testRequestHeaders() throws Exception {
    String endpoint = "/api/headers-test";

    wireMockServer.stubFor(
        post(urlEqualTo(endpoint))
            .willReturn(aResponse().withStatus(200).withBody("{\"success\": true}")));

    httpClient.post(endpoint, "{}");

    wireMockServer.verify(
        postRequestedFor(urlEqualTo(endpoint))
            .withHeader("X-API-KEY", equalTo("test-api-key"))
            .withHeader("User-Agent", equalTo("RealityDefender-Java-SDK/1.0.0"))
            .withHeader("Content-Type", equalTo("application/json; charset=UTF-8")));
  }

  @Test
  void testNetworkError() {
    // Stop the server to simulate network error
    wireMockServer.stop();

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.post("/api/test", "{}"));

    assertEquals("REQUEST_FAILED", exception.getCode());
    assertTrue(exception.getMessage().contains("Request failed"));
  }

  @Test
  void testClose() {
    // Test that close() doesn't throw exceptions
    assertDoesNotThrow(() -> httpClient.close());

    // Test that we can call close multiple times
    assertDoesNotThrow(() -> httpClient.close());
  }

  @Test
  void testConfigurationTimeout() {
    RealityDefenderConfig shortTimeoutConfig =
        new RealityDefenderConfig(
            "test-key",
            "http://localhost:" + wireMockServer.port(),
            Duration.ofMillis(1) // Very short timeout
            );

    try (HttpClient shortTimeoutClient = new HttpClient(shortTimeoutConfig)) {
      // Mock a slow response
      wireMockServer.stubFor(
          post(urlMatching("/.*"))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withFixedDelay(100) // 100ms delay with 1ms timeout
                      .withBody("{\"success\": true}")));

      // This should timeout
      RealityDefenderException exception =
          assertThrows(
              RealityDefenderException.class, () -> shortTimeoutClient.post("/api/test", "{}"));

      assertEquals("REQUEST_FAILED", exception.getCode());
    }
  }

  @Test
  void testRateLimitHandling() {
    String endpoint = "/api/rate-limit-test";

    wireMockServer.stubFor(
        post(urlEqualTo(endpoint))
            .willReturn(
                aResponse()
                    .withStatus(429)
                    .withHeader("Content-Type", "application/json; charset=UTF-8")
                    .withBody("{\"response\": \"Rate limit exceeded. Please try again later.\"}")));

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.post(endpoint, "{}"));

    assertEquals("SERVER_ERROR", exception.getCode());
    assertEquals(429, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("Rate limit exceeded"));
  }

  @Test
  void testUploadFileUnsupportedExtension() throws Exception {
    File testFile = new File(tempDir, "test.pdf");
    Files.write(testFile.toPath(), "test content".getBytes());

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.uploadFile(testFile));

    assertEquals("invalid_file", exception.getCode());
    assertTrue(exception.getMessage().contains("Unsupported file test.pdf!"));
  }

  @Test
  void testUploadFileTooLarge() throws Exception {
    File testFile = new File(tempDir, "test.txt");
    // Create a file larger than txt limit (5,242,880 bytes)
    byte[] largeContent = new byte[5_242_881];
    Files.write(testFile.toPath(), largeContent);

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.uploadFile(testFile));

    assertEquals("file_too_large", exception.getCode());
    assertTrue(exception.getMessage().contains("File too large to upload: test.txt"));
  }

  @Test
  void testUploadFileValidSizes() throws Exception {
    // Test each file type at its maximum allowed size
    testValidFileSize("test.txt", 5_242_880);
    testValidFileSize("test.mp3", 20_971_520);
    testValidFileSize("test.jpg", 52_428_800);
    testValidFileSize("test.mp4", 262_144_000);
  }

  @Test
  void testUploadFileUnreadableFile() throws Exception {
    File testFile = new File(tempDir, "test.jpg");
    Files.write(testFile.toPath(), "test content".getBytes());

    // Make file unreadable (this might not work on all systems)
    testFile.setReadable(false);

    try {
      RealityDefenderException exception =
          assertThrows(RealityDefenderException.class, () -> httpClient.uploadFile(testFile));

      assertEquals("INVALID_FILE", exception.getCode());
      assertTrue(exception.getMessage().contains("Cannot read file"));
    } finally {
      // Restore readability for cleanup
      testFile.setReadable(true);
    }
  }

  @Test
  void testUploadFileWithComplexFileName() throws Exception {
    // Test file with path separators and multiple dots
    File testFile = new File(tempDir, "my.test.file.with.dots.jpg");
    Files.write(testFile.toPath(), "test content".getBytes());

    mockSuccessfulUpload();

    JsonNode result = httpClient.uploadFile(testFile);

    assertNotNull(result);
    assertEquals("req456", result.get("request_id").asText());
  }

  @Test
  void testUploadFileCaseSensitiveExtension() throws Exception {
    // Test that uppercase extensions are not supported
    File testFile = new File(tempDir, "test.JPG");
    Files.write(testFile.toPath(), "test content".getBytes());

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.uploadFile(testFile));

    assertEquals("invalid_file", exception.getCode());
    assertTrue(exception.getMessage().contains("Unsupported file test.JPG!"));
  }

  @Test
  void testUploadFileNoExtension() throws Exception {
    File testFile = new File(tempDir, "testfile");
    Files.write(testFile.toPath(), "test content".getBytes());

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.uploadFile(testFile));

    assertEquals("invalid_file", exception.getCode());
  }

  @Test
  void testUploadFileWithAllSupportedExtensions() throws Exception {
    String[] supportedExtensions = {
      ".mp4", ".mov", ".jpg", ".png", ".jpeg", ".gif", ".webp", ".flac", ".wav", ".mp3", ".m4a",
      ".aac", ".alac", ".ogg", ".txt"
    };

    mockSuccessfulUpload();

    for (String extension : supportedExtensions) {
      File testFile = new File(tempDir, "test" + extension);
      Files.write(testFile.toPath(), "test content".getBytes());

      JsonNode result = httpClient.uploadFile(testFile);

      assertNotNull(result, "Failed for extension: " + extension);
      assertEquals("req456", result.get("request_id").asText());

      // Clean up for next iteration
      testFile.delete();
    }
  }

  private void testValidFileSize(String fileName, int maxSize) throws Exception {
    File testFile = new File(tempDir, fileName);
    byte[] content = new byte[maxSize]; // Exactly at the limit
    Files.write(testFile.toPath(), content);

    mockSuccessfulUpload();

    JsonNode result = httpClient.uploadFile(testFile);

    assertNotNull(result);
    assertEquals("req456", result.get("request_id").asText());

    testFile.delete(); // Clean up
  }

  @Test
  void testPostSocialMediaSuccess() throws Exception {
    String testUrl = "https://twitter.com/example/status/123456789";
    String expectedResponse = "{\"request_id\": \"social-req-123\"}";

    wireMockServer.stubFor(
        post(urlEqualTo("/api/files/social"))
            .withHeader("X-API-KEY", equalTo("test-api-key"))
            .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
            .withHeader("User-Agent", equalTo("RealityDefender-Java-SDK/1.0.0"))
            .withRequestBody(containing("\"socialLink\":\"" + testUrl + "\""))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(expectedResponse)));

    JsonNode result = httpClient.postSocialMedia(testUrl);

    assertNotNull(result);
    assertEquals("social-req-123", result.get("request_id").asText());

    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/files/social"))
            .withRequestBody(containing("\"socialLink\":\"" + testUrl + "\"")));
  }

  @Test
  void testPostSocialMediaWithHttpsUrl() throws Exception {
    String httpsUrl = "https://instagram.com/p/ABC123DEF456/";
    String expectedResponse = "{\"request_id\": \"instagram-req-456\"}";

    wireMockServer.stubFor(
        post(urlEqualTo("/api/files/social"))
            .withRequestBody(containing("\"socialLink\":\"" + httpsUrl + "\""))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(expectedResponse)));

    JsonNode result = httpClient.postSocialMedia(httpsUrl);

    assertEquals("instagram-req-456", result.get("request_id").asText());
  }

  @Test
  void testPostSocialMediaWithHttpUrl() throws Exception {
    String httpUrl = "http://example.com/social/post/123";
    String expectedResponse = "{\"request_id\": \"http-req-789\"}";

    wireMockServer.stubFor(
        post(urlEqualTo("/api/files/social"))
            .withRequestBody(containing("\"socialLink\":\"" + httpUrl + "\""))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(expectedResponse)));

    JsonNode result = httpClient.postSocialMedia(httpUrl);

    assertEquals("http-req-789", result.get("request_id").asText());
  }

  @Test
  void testPostSocialMediaInvalidUrl() {
    String invalidUrl = "not-a-valid-url";

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.postSocialMedia(invalidUrl));

    assertEquals("INVALID_REQUEST", exception.getCode());
    assertEquals("Invalid social media link: " + invalidUrl, exception.getMessage());

    // Verify no HTTP request was made
    wireMockServer.verify(0, postRequestedFor(urlEqualTo("/api/files/social")));
  }

  @Test
  void testPostSocialMediaNullUrl() {
    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.postSocialMedia(null));

    assertEquals("INVALID_REQUEST", exception.getCode());
    assertEquals("Invalid social media link: null", exception.getMessage());
  }

  @Test
  void testPostSocialMediaEmptyUrl() {
    String emptyUrl = "";

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.postSocialMedia(emptyUrl));

    assertEquals("INVALID_REQUEST", exception.getCode());
    assertEquals("Invalid social media link: ", exception.getMessage());
  }

  @Test
  void testPostSocialMediaNonHttpScheme() {
    String ftpUrl = "ftp://example.com/file.txt";

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.postSocialMedia(ftpUrl));

    assertEquals("INVALID_REQUEST", exception.getCode());
    assertEquals("Invalid social media link: " + ftpUrl, exception.getMessage());
  }

  @Test
  void testPostSocialMediaServerError() {
    String testUrl = "https://youtube.com/watch?v=example123";

    wireMockServer.stubFor(
        post(urlEqualTo("/api/files/social"))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"response\": \"Internal server error\"}")));

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.postSocialMedia(testUrl));

    assertEquals("UPLOAD_FAILED", exception.getCode());
  }

  @Test
  void testPostSocialMediaBadRequest() {
    String testUrl = "https://facebook.com/user/posts/123456789";

    wireMockServer.stubFor(
        post(urlEqualTo("/api/files/social"))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"code\": \"invalid_url\", \"response\": \"URL format not supported\"}")));

    RealityDefenderException exception =
        assertThrows(RealityDefenderException.class, () -> httpClient.postSocialMedia(testUrl));

    assertTrue(exception.getMessage().contains("Upload failed"));
  }

  @Test
  void testPostSocialMediaComplexUrls() throws Exception {
    String[] complexUrls = {
      "https://youtube.com/watch?v=dQw4w9WgXcQ&t=30s&list=PLrAXtmRdnEQy6nuLMArC6M6",
      "https://twitter.com/user/status/123456789?ref_src=twsrc%5Etfw#reply",
      "https://instagram.com/p/ABC123DEF456/?utm_source=ig_web_copy_link",
      "https://reddit.com/r/test/comments/123456/title/?sort=top"
    };

    for (int i = 0; i < complexUrls.length; i++) {
      String url = complexUrls[i];
      String requestId = "complex-req-" + i;

      wireMockServer.stubFor(
          post(urlEqualTo("/api/files/social"))
              .withRequestBody(containing("\"socialLink\":\"" + url + "\""))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody("{\"request_id\": \"" + requestId + "\"}")));

      JsonNode result = httpClient.postSocialMedia(url);
      assertEquals(requestId, result.get("request_id").asText());
    }
  }

  @Test
  void testPostSocialMediaRequestBodyFormat() throws Exception {
    String testUrl = "https://linkedin.com/posts/user_activity-123456789";

    wireMockServer.stubFor(
        post(urlEqualTo("/api/files/social"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"request_id\": \"linkedin-req\"}")));

    httpClient.postSocialMedia(testUrl);

    // Verify the request body contains properly formatted SocialMediaRequest JSON
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/files/social"))
            .withRequestBody(matchingJsonPath("$.socialLink", equalTo(testUrl))));
  }

  private void mockSuccessfulUpload() {
    // Mock signed URL endpoint
    wireMockServer.stubFor(
        post(urlEqualTo("/api/files/aws-presigned"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\n"
                            + "  \"code\": \"ok\",\n"
                            + "  \"response\": {\n"
                            + "    \"signedUrl\": \"http://localhost:"
                            + wireMockServer.port()
                            + "/upload\"\n"
                            + "  },\n"
                            + "  \"errno\": 0,\n"
                            + "  \"mediaId\": \"media123\",\n"
                            + "  \"requestId\": \"req456\"\n"
                            + "}")));

    // Mock file upload endpoint
    wireMockServer.stubFor(put(urlEqualTo("/upload")).willReturn(aResponse().withStatus(200)));
  }
}

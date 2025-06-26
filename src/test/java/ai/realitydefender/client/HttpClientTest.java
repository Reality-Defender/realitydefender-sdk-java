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
import org.junit.jupiter.api.io.TempDir;

class HttpClientTest {

    private WireMockServer wireMockServer;
    private HttpClient httpClient;
    private RealityDefenderConfig config;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        // Start WireMock server
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        // Create config with WireMock server URL
        config = new RealityDefenderConfig(
                "test-api-key",
                "http://localhost:" + wireMockServer.port(),
                Duration.ofSeconds(30)
        );

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
        wireMockServer.stubFor(post(urlEqualTo("/api/files/aws-presigned"))
                .withHeader("X-API-KEY", equalTo("test-api-key"))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withRequestBody(containing("test.jpg"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"code\": \"ok\",\n" +
                                "  \"response\": {\n" +
                                "    \"signedUrl\": \"http://localhost:" + wireMockServer.port() + "/upload\"\n" +
                                "  },\n" +
                                "  \"errno\": 0,\n" +
                                "  \"mediaId\": \"media123\",\n" +
                                "  \"requestId\": \"req456\"\n" +
                                "}")));

        // Mock file upload endpoint
        wireMockServer.stubFor(put(urlEqualTo("/upload"))
                .withHeader("X-API-KEY", equalTo("test-api-key"))
                .withHeader("Content-Type", equalTo("application/octet-stream"))
                .willReturn(aResponse()
                        .withStatus(200)));

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

        RealityDefenderException exception = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.uploadFile(nonExistentFile)
        );

        assertEquals("INVALID_FILE", exception.getCode());
        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void testUploadFileSignedUrlFails() throws Exception {
        File testFile = new File(tempDir, "test.jpg");
        Files.write(testFile.toPath(), "test content".getBytes());

        // Mock signed URL endpoint to return error
        wireMockServer.stubFor(post(urlEqualTo("/api/files/aws-presigned"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": {\"message\": \"Invalid API key\"}}")));

        RealityDefenderException exception = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.uploadFile(testFile)
        );

        assertEquals("UNAUTHORIZED", exception.getCode());
        assertEquals(401, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Invalid API key"));
    }

    @Test
    void testUploadFileUploadFails() throws Exception {
        File testFile = new File(tempDir, "test.jpg");
        Files.write(testFile.toPath(), "test content".getBytes());

        // Mock successful signed URL response
        wireMockServer.stubFor(post(urlEqualTo("/api/files/aws-presigned"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBody("{\n" +
                                "  \"code\": \"ok\",\n" +
                                "  \"response\": {\n" +
                                "    \"signedUrl\": \"http://localhost:" + wireMockServer.port() + "/upload\"\n" +
                                "  },\n" +
                                "  \"errno\": 0,\n" +
                                "  \"mediaId\": \"media123\",\n" +
                                "  \"requestId\": \"req456\"\n" +
                                "}")));

        // Mock failed upload
        wireMockServer.stubFor(put(urlEqualTo("/upload"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        RealityDefenderException exception = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.uploadFile(testFile)
        );

        assertEquals("SERVER_ERROR", exception.getCode());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void testGetResultsSuccess() throws Exception {
        String requestId = "test-request-123";
        String expectedResponse = "{\n" +
                "  \"request_id\": \"" + requestId + "\",\n" +
                "  \"status\": \"COMPLETED\",\n" +
                "  \"results\": {\n" +
                "    \"overall_status\": \"ARTIFICIAL\"\n" +
                "  }\n" +
                "}";

        wireMockServer.stubFor(get(urlEqualTo("/api/media/users/" + requestId))
                .withHeader("X-API-KEY", equalTo("test-api-key"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
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

        wireMockServer.stubFor(get(urlEqualTo("/api/media/users/" + requestId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": {\"message\": \"Request not found\"}}")));

        RealityDefenderException exception = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.getResults(requestId)
        );

        assertEquals("NOT_FOUND", exception.getCode());
        assertEquals(404, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Request not found"));
    }

    @Test
    void testPostSuccess() throws Exception {
        String endpoint = "/api/test";
        String requestBody = "{\"test\": \"data\"}";
        String responseBody = "{\"success\": true, \"id\": \"12345\"}";

        wireMockServer.stubFor(post(urlEqualTo(endpoint))
                .withHeader("X-API-KEY", equalTo("test-api-key"))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withHeader("User-Agent", equalTo("RealityDefender-Java-SDK/1.0.0"))
                .withRequestBody(equalTo(requestBody))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        JsonNode result = httpClient.post(endpoint, requestBody);

        assertNotNull(result);
        assertTrue(result.get("success").asBoolean());
        assertEquals("12345", result.get("id").asText());

        wireMockServer.verify(postRequestedFor(urlEqualTo(endpoint))
                .withRequestBody(equalTo(requestBody)));
    }

    @Test
    void testPostWithBadRequest() {
        String endpoint = "/api/test";
        String requestBody = "{\"invalid\": \"data\"}";

        wireMockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": {\"message\": \"Invalid request format\"}}")));

        RealityDefenderException exception = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.post(endpoint, requestBody)
        );

        assertEquals("BAD_REQUEST", exception.getCode());
        assertEquals(400, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Invalid request format"));
    }

    @Test
    void testErrorCodeMapping() {
        testErrorResponse(401, "UNAUTHORIZED", "Unauthorized");
        testErrorResponse(403, "FORBIDDEN", "Forbidden");
        testErrorResponse(404, "NOT_FOUND", "Not Found");
        testErrorResponse(413, "FILE_TOO_LARGE", "File too large");
        testErrorResponse(415, "UNSUPPORTED_MEDIA_TYPE", "Unsupported media type");
        testErrorResponse(429, "RATE_LIMITED", "Rate limit exceeded");
        testErrorResponse(500, "SERVER_ERROR", "Internal server error");
        testErrorResponse(502, "SERVICE_UNAVAILABLE", "Bad Gateway");
        testErrorResponse(503, "SERVICE_UNAVAILABLE", "Service Unavailable");
        testErrorResponse(504, "SERVICE_UNAVAILABLE", "Gateway Timeout");
    }

    private void testErrorResponse(int statusCode, String expectedErrorCode, String expectedMessage) {
        String endpoint = "/api/error-test-" + statusCode;

        wireMockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(statusCode)));

        RealityDefenderException exception = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.post(endpoint, "{}")
        );

        assertEquals(expectedErrorCode, exception.getCode());
        assertEquals(statusCode, exception.getStatusCode());
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    void testErrorMessageExtraction() {
        String endpoint = "/api/error-message-test";

        // Test nested error message
        wireMockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": {\"message\": \"Nested error message\"}}")));

        RealityDefenderException exception1 = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.post(endpoint, "{}")
        );

        assertTrue(exception1.getMessage().contains("Nested error message"));

        // Test top-level message
        wireMockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"Top level message\"}")));

        RealityDefenderException exception2 = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.post(endpoint, "{}")
        );

        assertTrue(exception2.getMessage().contains("Top level message"));

        // Test malformed JSON (should fall back to default message)
        wireMockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Invalid JSON response")));

        RealityDefenderException exception3 = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.post(endpoint, "{}")
        );

        assertTrue(exception3.getMessage().contains("Bad Request"));
    }

    @Test
    void testEmptyResponseBody() {
        String endpoint = "/api/empty-response";

        wireMockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("")));

        RealityDefenderException exception = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.post(endpoint, "{}")
        );

        assertEquals("SERVER_ERROR", exception.getCode());
        assertTrue(exception.getMessage().contains("Internal server error"));
    }

    @Test
    void testRequestHeaders() throws Exception {
        String endpoint = "/api/headers-test";

        wireMockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"success\": true}")));

        httpClient.post(endpoint, "{}");

        wireMockServer.verify(postRequestedFor(urlEqualTo(endpoint))
                .withHeader("X-API-KEY", equalTo("test-api-key"))
                .withHeader("User-Agent", equalTo("RealityDefender-Java-SDK/1.0.0"))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8")));
    }

    @Test
    void testNetworkError() {
        // Stop the server to simulate network error
        wireMockServer.stop();

        RealityDefenderException exception = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.post("/api/test", "{}")
        );

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
        RealityDefenderConfig shortTimeoutConfig = new RealityDefenderConfig(
                "test-key",
                "http://localhost:" + wireMockServer.port(),
                Duration.ofMillis(1) // Very short timeout
        );

        try (HttpClient shortTimeoutClient = new HttpClient(shortTimeoutConfig)) {
            // Mock a slow response
            wireMockServer.stubFor(post(urlMatching("/.*"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withFixedDelay(100) // 100ms delay with 1ms timeout
                            .withBody("{\"success\": true}")));

            // This should timeout
            RealityDefenderException exception = assertThrows(
                    RealityDefenderException.class,
                    () -> shortTimeoutClient.post("/api/test", "{}")
            );

            assertEquals("REQUEST_FAILED", exception.getCode());
        }
    }

    @Test
    void testRateLimitHandling() {
        String endpoint = "/api/rate-limit-test";

        wireMockServer.stubFor(post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBody("{\"error\": {\"message\": \"Rate limit exceeded. Please try again later.\"}}")));

        RealityDefenderException exception = assertThrows(
                RealityDefenderException.class,
                () -> httpClient.post(endpoint, "{}")
        );

        assertEquals("RATE_LIMITED", exception.getCode());
        assertEquals(429, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
    }
}
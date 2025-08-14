package ai.realitydefender.models;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SignedUrlResponseTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Test
  void testSignedUrlResponseCreation() {
    SignedUrlResponse.SignedUrlData data =
        new SignedUrlResponse.SignedUrlData("https://example.com/signed-url");
    SignedUrlResponse response = new SignedUrlResponse("ok", data, 0, "media123", "request456");

    assertEquals("ok", response.getCode());
    assertEquals(data, response.getResponse());
    assertEquals(0, response.getErrno());
    assertEquals("media123", response.getMediaId());
    assertEquals("request456", response.getRequestId());
    assertEquals("https://example.com/signed-url", response.getSignedUrl());
  }

  @Test
  void testSignedUrlResponseWithNullData() {
    SignedUrlResponse response = new SignedUrlResponse("error", null, 1, "media123", "request456");

    assertEquals("error", response.getCode());
    assertNull(response.getResponse());
    assertEquals(1, response.getErrno());
    assertEquals("media123", response.getMediaId());
    assertEquals("request456", response.getRequestId());
    assertNull(response.getSignedUrl()); // Should return null when response is null
  }

  @Test
  void testSignedUrlDataCreation() {
    String url = "https://storage.googleapis.com/bucket/file?signature=abc123";
    SignedUrlResponse.SignedUrlData data = new SignedUrlResponse.SignedUrlData(url);

    assertEquals(url, data.getSignedUrl());
  }

  @Test
  void testSignedUrlDataWithNullUrl() {
    SignedUrlResponse.SignedUrlData data = new SignedUrlResponse.SignedUrlData(null);

    assertNull(data.getSignedUrl());
  }

  @Test
  void testSignedUrlResponseEquality() {
    SignedUrlResponse.SignedUrlData data1 =
        new SignedUrlResponse.SignedUrlData("https://example.com/url1");
    SignedUrlResponse.SignedUrlData data2 =
        new SignedUrlResponse.SignedUrlData("https://example.com/url1");

    SignedUrlResponse response1 = new SignedUrlResponse("ok", data1, 0, "media123", "request456");
    SignedUrlResponse response2 = new SignedUrlResponse("ok", data2, 0, "media123", "request456");
    SignedUrlResponse response3 =
        new SignedUrlResponse("error", data1, 1, "media123", "request456");

    assertEquals(response1, response2);
    assertEquals(response1.hashCode(), response2.hashCode());
    assertNotEquals(response1, response3);
    assertNotEquals(response1.hashCode(), response3.hashCode());
  }

  @Test
  void testSignedUrlDataEquality() {
    SignedUrlResponse.SignedUrlData data1 =
        new SignedUrlResponse.SignedUrlData("https://example.com/url");
    SignedUrlResponse.SignedUrlData data2 =
        new SignedUrlResponse.SignedUrlData("https://example.com/url");
    SignedUrlResponse.SignedUrlData data3 =
        new SignedUrlResponse.SignedUrlData("https://different.com/url");

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
    assertNotEquals(data1, data3);
    assertNotEquals(data1.hashCode(), data3.hashCode());
  }

  @Test
  void testSignedUrlResponseToString() {
    SignedUrlResponse.SignedUrlData data =
        new SignedUrlResponse.SignedUrlData("https://example.com/signed");
    SignedUrlResponse response = new SignedUrlResponse("ok", data, 0, "media789", "request123");

    String toString = response.toString();
    assertTrue(toString.contains("SignedUrlResponse"));
    assertTrue(toString.contains("code='ok'"));
    assertTrue(toString.contains("errno=0"));
    assertTrue(toString.contains("mediaId='media789'"));
    assertTrue(toString.contains("requestId='request123'"));
  }

  @Test
  void testSignedUrlDataToString() {
    SignedUrlResponse.SignedUrlData data =
        new SignedUrlResponse.SignedUrlData("https://test.com/url");

    String toString = data.toString();
    assertTrue(toString.contains("SignedUrlData"));
    assertTrue(toString.contains("signedUrl='https://test.com/url'"));
  }

  @Test
  void testJsonSerialization() throws Exception {
    SignedUrlResponse.SignedUrlData data =
        new SignedUrlResponse.SignedUrlData("https://example.com/upload");
    SignedUrlResponse response = new SignedUrlResponse("ok", data, 0, "media456", "request789");

    String json = objectMapper.writeValueAsString(response);
    assertTrue(json.contains("\"code\":\"ok\""));
    assertTrue(json.contains("\"errno\":0"));
    assertTrue(json.contains("\"mediaId\":\"media456\""));
    assertTrue(json.contains("\"requestId\":\"request789\""));
    assertTrue(json.contains("\"signedUrl\":\"https://example.com/upload\""));
  }

  @Test
  void testJsonDeserialization() throws Exception {
    String json =
        "{\n"
            + "  \"code\": \"ok\",\n"
            + "  \"response\": {\n"
            + "    \"signedUrl\": \"https://storage.example.com/upload?token=xyz\"\n"
            + "  },\n"
            + "  \"errno\": 0,\n"
            + "  \"mediaId\": \"media_001\",\n"
            + "  \"requestId\": \"req_001\"\n"
            + "}";

    SignedUrlResponse response = objectMapper.readValue(json, SignedUrlResponse.class);

    assertEquals("ok", response.getCode());
    assertNotNull(response.getResponse());
    assertEquals(
        "https://storage.example.com/upload?token=xyz", response.getResponse().getSignedUrl());
    assertEquals("https://storage.example.com/upload?token=xyz", response.getSignedUrl());
    assertEquals(0, response.getErrno());
    assertEquals("media_001", response.getMediaId());
    assertEquals("req_001", response.getRequestId());
  }

  @Test
  void testJsonDeserializationWithNullResponse() throws Exception {
    String json =
        "{\n"
            + "  \"code\": \"error\",\n"
            + "  \"response\": null,\n"
            + "  \"errno\": 404,\n"
            + "  \"mediaId\": \"media_404\",\n"
            + "  \"requestId\": \"req_404\"\n"
            + "}";

    SignedUrlResponse response = objectMapper.readValue(json, SignedUrlResponse.class);

    assertEquals("error", response.getCode());
    assertNull(response.getResponse());
    assertNull(response.getSignedUrl());
    assertEquals(404, response.getErrno());
    assertEquals("media_404", response.getMediaId());
    assertEquals("req_404", response.getRequestId());
  }

  @Test
  void testJsonRoundTrip() throws Exception {
    SignedUrlResponse.SignedUrlData originalData =
        new SignedUrlResponse.SignedUrlData("https://bucket.s3.amazonaws.com/file");
    SignedUrlResponse original =
        new SignedUrlResponse("success", originalData, 0, "media_roundtrip", "req_roundtrip");

    String json = objectMapper.writeValueAsString(original);
    SignedUrlResponse deserialized = objectMapper.readValue(json, SignedUrlResponse.class);

    assertEquals(original.getCode(), deserialized.getCode());
    assertEquals(original.getResponse().getSignedUrl(), deserialized.getResponse().getSignedUrl());
    assertEquals(original.getErrno(), deserialized.getErrno());
    assertEquals(original.getMediaId(), deserialized.getMediaId());
    assertEquals(original.getRequestId(), deserialized.getRequestId());
    assertEquals(original.getSignedUrl(), deserialized.getSignedUrl());
  }

  @Test
  void testErrorResponse() {
    SignedUrlResponse errorResponse = new SignedUrlResponse("error", null, 500, null, "req_error");

    assertEquals("error", errorResponse.getCode());
    assertNull(errorResponse.getResponse());
    assertEquals(500, errorResponse.getErrno());
    assertNull(errorResponse.getMediaId());
    assertEquals("req_error", errorResponse.getRequestId());
    assertNull(errorResponse.getSignedUrl());
  }

  @Test
  void testSuccessfulResponse() {
    SignedUrlResponse.SignedUrlData data =
        new SignedUrlResponse.SignedUrlData("https://upload.example.com/secure-upload");
    SignedUrlResponse response =
        new SignedUrlResponse("ok", data, 0, "media_success", "req_success");

    assertEquals("ok", response.getCode());
    assertEquals(0, response.getErrno());
    assertNotNull(response.getResponse());
    assertEquals("https://upload.example.com/secure-upload", response.getSignedUrl());
    assertEquals("media_success", response.getMediaId());
    assertEquals("req_success", response.getRequestId());
  }

  @Test
  void testSignedUrlResponseReflexivity() {
    SignedUrlResponse.SignedUrlData data =
        new SignedUrlResponse.SignedUrlData("https://example.com/test");
    SignedUrlResponse response = new SignedUrlResponse("ok", data, 0, "media", "request");

    assertEquals("ok", response.getCode());
    assertEquals(data, response.getResponse());
    assertEquals(0, response.getErrno());
    assertEquals("media", response.getMediaId());
    assertEquals("request", response.getRequestId());
  }

  @Test
  void testSignedUrlResponseNullComparison() {
    SignedUrlResponse.SignedUrlData data =
        new SignedUrlResponse.SignedUrlData("https://example.com/test");
    SignedUrlResponse response = new SignedUrlResponse("ok", data, 0, "media", "request");

    assertNotEquals(null, response);
    assertNotEquals(null, data);
  }

  @Test
  void testSignedUrlResponseDifferentClassComparison() {
    SignedUrlResponse.SignedUrlData data =
        new SignedUrlResponse.SignedUrlData("https://example.com/test");
    SignedUrlResponse response = new SignedUrlResponse("ok", data, 0, "media", "request");

    assertNotEquals("not a SignedUrlResponse", response);
    assertNotEquals("not a SignedUrlData", data);
  }
}

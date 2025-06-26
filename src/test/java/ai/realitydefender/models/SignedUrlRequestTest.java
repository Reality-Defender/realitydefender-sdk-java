package ai.realitydefender.models;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SignedUrlRequestTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Test
  void testSignedUrlRequestCreation() {
    String fileName = "test-file.jpg";
    SignedUrlRequest request = new SignedUrlRequest(fileName);

    assertEquals(fileName, request.getFileName());
  }

  @Test
  void testSignedUrlRequestWithNullFileName() {
    SignedUrlRequest request = new SignedUrlRequest(null);

    assertNull(request.getFileName());
  }

  @Test
  void testSignedUrlRequestWithEmptyFileName() {
    String fileName = "";
    SignedUrlRequest request = new SignedUrlRequest(fileName);

    assertEquals(fileName, request.getFileName());
  }

  @Test
  void testSignedUrlRequestWithSpecialCharacters() {
    String fileName = "test-file with spaces & symbols!@#.png";
    SignedUrlRequest request = new SignedUrlRequest(fileName);

    assertEquals(fileName, request.getFileName());
  }

  @Test
  void testJsonSerialization() throws Exception {
    String fileName = "document.pdf";
    SignedUrlRequest request = new SignedUrlRequest(fileName);

    String json = objectMapper.writeValueAsString(request);
    assertTrue(json.contains("\"fileName\":\"document.pdf\""));
  }

  @Test
  void testJsonDeserialization() throws Exception {
    String json = "{\"fileName\":\"video.mp4\"}";
    SignedUrlRequest request = objectMapper.readValue(json, SignedUrlRequest.class);

    assertEquals("video.mp4", request.getFileName());
  }

  @Test
  void testJsonDeserializationWithNullFileName() throws Exception {
    String json = "{\"fileName\":null}";
    SignedUrlRequest request = objectMapper.readValue(json, SignedUrlRequest.class);

    assertNull(request.getFileName());
  }

  @Test
  void testJsonRoundTrip() throws Exception {
    String originalFileName = "test-image.jpeg";
    SignedUrlRequest original = new SignedUrlRequest(originalFileName);

    String json = objectMapper.writeValueAsString(original);
    SignedUrlRequest deserialized = objectMapper.readValue(json, SignedUrlRequest.class);

    assertEquals(original.getFileName(), deserialized.getFileName());
  }

  @Test
  void testToString() {
    SignedUrlRequest request = new SignedUrlRequest("test.txt");
    String toString = request.toString();

    assertNotNull(toString);
    assertTrue(toString.contains("SignedUrlRequest"));
  }

  @Test
  void testEqualsAndHashCode() {
    SignedUrlRequest request1 = new SignedUrlRequest("file.jpg");
    SignedUrlRequest request2 = new SignedUrlRequest("file.jpg");
    SignedUrlRequest request3 = new SignedUrlRequest("different.jpg");
    SignedUrlRequest request4 = new SignedUrlRequest(null);
    SignedUrlRequest request5 = new SignedUrlRequest(null);

    // Test equality
    assertEquals(request1, request2);
    assertNotEquals(request1, request3);
    assertEquals(request4, request5);
    assertNotEquals(request1, request4);

    // Test hash code consistency
    assertEquals(request1.hashCode(), request2.hashCode());
    assertEquals(request4.hashCode(), request5.hashCode());

    // Test reflexivity
    assertEquals(request1, request1);

    // Test null comparison
    assertNotEquals(null, request1);

    // Test different class comparison
    assertNotEquals("not a SignedUrlRequest", request1);
  }
}

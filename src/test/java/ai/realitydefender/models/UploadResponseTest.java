package ai.realitydefender.models;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UploadResponseTest {

  @Test
  void testConstructorWithParameters() {
    String requestId = "request-123";
    String mediaId = "media-456";

    UploadResponse response = new UploadResponse(requestId, mediaId);

    assertThat(response.getRequestId()).isEqualTo(requestId);
    assertThat(response.getMediaId()).isEqualTo(mediaId);
  }

  @Test
  void testDefaultConstructor() {
    UploadResponse response = new UploadResponse(null, null);

    assertThat(response.getRequestId()).isNull();
    assertThat(response.getMediaId()).isNull();
  }

  @Test
  void testEqualsAndHashCode() {
    UploadResponse response1 = new UploadResponse("request-123", "media-456");
    UploadResponse response2 = new UploadResponse("request-123", "media-456");
    UploadResponse response3 = new UploadResponse("request-789", "media-456");

    assertThat(response1).isEqualTo(response2);
    assertThat(response1).isNotEqualTo(response3);
    assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
  }

  @Test
  void testToString() {
    UploadResponse response = new UploadResponse("request-123", "media-456");
    String toString = response.toString();

    assertThat(toString).contains("UploadResponse");
    assertThat(toString).contains("requestId='request-123'");
    assertThat(toString).contains("mediaId='media-456'");
  }
}

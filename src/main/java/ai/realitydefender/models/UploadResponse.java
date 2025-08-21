package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/** Response from file upload operations. */
public class UploadResponse {

  private final String requestId;
  private final String mediaId;

  @JsonCreator
  public UploadResponse(
      @JsonProperty("request_id") String requestId,
      @JsonProperty(value = "media_id", required = false, defaultValue = "") String mediaId) {
    this.requestId = requestId;
    this.mediaId = mediaId;
  }

  /**
   * Gets the request ID for tracking the analysis.
   *
   * @return the request ID
   */
  public String getRequestId() {
    return requestId;
  }

  /**
   * Gets the media ID for the uploaded file.
   *
   * @return the media ID
   */
  public String getMediaId() {
    return mediaId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UploadResponse that = (UploadResponse) o;
    return Objects.equals(requestId, that.requestId) && Objects.equals(mediaId, that.mediaId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, mediaId);
  }

  @Override
  public String toString() {
    return "UploadResponse{"
        + "requestId='"
        + requestId
        + '\''
        + ", mediaId='"
        + mediaId
        + '\''
        + '}';
  }
}

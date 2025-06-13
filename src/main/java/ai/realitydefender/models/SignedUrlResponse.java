package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/** Response from signed URL operations. */
public class SignedUrlResponse {

  private final String code;
  private final SignedUrlData response;
  private final int errno;
  private final String mediaId;
  private final String requestId;

  @JsonCreator
  public SignedUrlResponse(
      @JsonProperty("code") String code,
      @JsonProperty("response") SignedUrlData response,
      @JsonProperty("errno") int errno,
      @JsonProperty("mediaId") String mediaId,
      @JsonProperty("requestId") String requestId) {
    this.code = code;
    this.response = response;
    this.errno = errno;
    this.mediaId = mediaId;
    this.requestId = requestId;
  }

  /**
   * Gets the response code.
   *
   * @return the response code (e.g., "ok")
   */
  public String getCode() {
    return code;
  }

  /**
   * Gets the response data containing the signed URL.
   *
   * @return the response data
   */
  public SignedUrlData getResponse() {
    return response;
  }

  /**
   * Gets the signed URL for file upload. Convenience method that extracts the signedUrl from the
   * nested response object.
   *
   * @return the signed URL, or null if response is null
   */
  public String getSignedUrl() {
    return response != null ? response.getSignedUrl() : null;
  }

  /**
   * Gets the error number.
   *
   * @return the error number (0 typically means success)
   */
  public int getErrno() {
    return errno;
  }

  /**
   * Gets the media ID.
   *
   * @return the media ID
   */
  public String getMediaId() {
    return mediaId;
  }

  /**
   * Gets the request ID.
   *
   * @return the request ID
   */
  public String getRequestId() {
    return requestId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SignedUrlResponse that = (SignedUrlResponse) o;
    return errno == that.errno
        && Objects.equals(code, that.code)
        && Objects.equals(response, that.response)
        && Objects.equals(mediaId, that.mediaId)
        && Objects.equals(requestId, that.requestId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, response, errno, mediaId, requestId);
  }

  @Override
  public String toString() {
    return "SignedUrlResponse{"
        + "code='"
        + code
        + '\''
        + ", response="
        + response
        + ", errno="
        + errno
        + ", mediaId='"
        + mediaId
        + '\''
        + ", requestId='"
        + requestId
        + '\''
        + '}';
  }

  /** Nested response data containing the signed URL. */
  public static class SignedUrlData {
    private final String signedUrl;

    @JsonCreator
    public SignedUrlData(@JsonProperty("signedUrl") String signedUrl) {
      this.signedUrl = signedUrl;
    }

    /**
     * Gets the signed URL for file upload.
     *
     * @return the signed URL
     */
    public String getSignedUrl() {
      return signedUrl;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SignedUrlData that = (SignedUrlData) o;
      return Objects.equals(signedUrl, that.signedUrl);
    }

    @Override
    public int hashCode() {
      return Objects.hash(signedUrl);
    }

    @Override
    public String toString() {
      return "SignedUrlData{" + "signedUrl='" + signedUrl + '\'' + '}';
    }
  }
}

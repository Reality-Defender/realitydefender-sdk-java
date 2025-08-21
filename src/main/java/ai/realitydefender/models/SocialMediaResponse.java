package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SocialMediaResponse extends BasicResponse {
  private final String requestId;

  @JsonCreator
  public SocialMediaResponse(
      @JsonProperty(value = "requestId", required = true) String requestId,
      @JsonProperty("code") String code,
      @JsonProperty("response") String response,
      @JsonProperty("errno") int errno) {
    super(code, response, errno);
    this.requestId = requestId;
  }

  public String getRequestId() {
    return this.requestId;
  }
}

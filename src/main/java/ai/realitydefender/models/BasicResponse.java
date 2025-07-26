package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicResponse {
  private final String code;
  private final String response;
  private final int errno;

  @JsonCreator
  public BasicResponse(
      @JsonProperty("code") String code,
      @JsonProperty("response") String response,
      @JsonProperty("errno") int errno) {
    this.code = code;
    this.response = response;
    this.errno = errno;
  }

  public BasicResponse() {
    this("", "Unknown error", 0);
  }

  public String getCode() {
    return Objects.requireNonNullElse(this.code, "");
  }

  public String getResponse() {
    return Objects.requireNonNullElse(this.response, "");
  }

  public int getErrno() {
    return errno;
  }

  @Override
  public String toString() {
    return "BasicResponse{"
        + "code='"
        + this.getCode()
        + '\''
        + ", response='"
        + this.getResponse()
        + '\''
        + ", errno="
        + this.getErrno()
        + '}';
  }
}

package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicResponse {
  private final String code;
  private final String message;
  private final int errno;

  @JsonCreator
  public BasicResponse(
      @JsonProperty("code") String code,
      @JsonProperty("message") String message,
      @JsonProperty("errno") int errno) {
    this.code = code;
    this.message = message;
    this.errno = errno;
  }

  public BasicResponse() {
    this("", "", 0);
  }

  public String getCode() {
    return Objects.requireNonNullElse(this.code, "");
  }

  public String getMessage() {
    return Objects.requireNonNullElse(this.message, "");
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
        + ", message='"
        + this.getMessage()
        + '\''
        + ", errno="
        + this.getErrno()
        + '}';
  }
}

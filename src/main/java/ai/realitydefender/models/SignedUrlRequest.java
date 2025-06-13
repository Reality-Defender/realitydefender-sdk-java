package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignedUrlRequest {
  @JsonProperty("fileName")
  private final String fileName;

  public SignedUrlRequest(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }
}

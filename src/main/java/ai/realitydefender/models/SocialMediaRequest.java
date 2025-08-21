package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SocialMediaRequest {
  private final String socialLink;

  @JsonCreator
  public SocialMediaRequest(@JsonProperty("socialLink") String socialLink) {
    this.socialLink = socialLink;
  }

  public String getSocialLink() {
    return this.socialLink;
  }
}

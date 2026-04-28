package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Response body for successful user feedback creation (201). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFeedbackResponse {

  private String id;

  @JsonProperty("userId")
  private String userId;

  @JsonProperty("requestId")
  private String requestId;

  @JsonProperty("institutionId")
  private String institutionId;

  private String text;
  private String category;

  @JsonProperty("userName")
  private String userName;

  @JsonProperty("userEmail")
  private String userEmail;

  @JsonProperty("orgName")
  private String orgName;

  @JsonProperty("mediaType")
  private String mediaType;

  @JsonProperty("mediaViewUrl")
  private String mediaViewUrl;

  @JsonProperty("mediaSource")
  private String mediaSource;

  private String label;

  @JsonProperty("createdAt")
  private String createdAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getInstitutionId() {
    return institutionId;
  }

  public void setInstitutionId(String institutionId) {
    this.institutionId = institutionId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public String getMediaViewUrl() {
    return mediaViewUrl;
  }

  public void setMediaViewUrl(String mediaViewUrl) {
    this.mediaViewUrl = mediaViewUrl;
  }

  public String getMediaSource() {
    return mediaSource;
  }

  public void setMediaSource(String mediaSource) {
    this.mediaSource = mediaSource;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
}

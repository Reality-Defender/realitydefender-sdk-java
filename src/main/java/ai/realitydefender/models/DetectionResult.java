package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Represents the result of a deepfake detection analysis. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectionResult {

  private String name;
  private String filename;
  private String aggregationResultUrl;
  private String originalFileName;
  private String storageLocation;
  private String convertedFileName;
  private String convertedFileLocation;
  private String socialLink;
  private boolean socialLinkDownloaded;
  private boolean socialLinkDownloadFailed;
  private String requestId;
  private LocalDateTime uploadedDate;
  private String mediaType;
  private UserInfo userInfo;
  private String audioExtractionFileName;
  private boolean showAudioResult;
  private String audioRequestId;
  private String thumbnail;
  private String contentPreview;
  private String userId;
  private String institutionId;
  private String releaseVersion;
  private List<String> webhookUrls;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean audioExtractionProcessed;
  private ResultsSummary resultsSummary;
  private List<ModelResult> models;
  private List<Object> rdModels;
  private MediaMetadataInfo mediaMetadataInfo;
  private String modelMetadataUrl;
  private String explainabilityUrl;
  private Map<String, String> heatmaps;

  private Double score;

  @JsonCreator
  public DetectionResult(
      @JsonProperty("name") String name,
      @JsonProperty("filename") String filename,
      @JsonProperty("aggregationResultUrl") String aggregationResultUrl,
      @JsonProperty("originalFileName") String originalFileName,
      @JsonProperty("storageLocation") String storageLocation,
      @JsonProperty("convertedFileName") String convertedFileName,
      @JsonProperty("convertedFileLocation") String convertedFileLocation,
      @JsonProperty("socialLink") String socialLink,
      @JsonProperty("socialLinkDownloaded") boolean socialLinkDownloaded,
      @JsonProperty("socialLinkDownloadFailed") boolean socialLinkDownloadFailed,
      @JsonProperty("requestId") String requestId,
      @JsonProperty("uploadedDate") LocalDateTime uploadedDate,
      @JsonProperty("mediaType") String mediaType,
      @JsonProperty("userInfo") UserInfo userInfo,
      @JsonProperty("audioExtractionFileName") String audioExtractionFileName,
      @JsonProperty("showAudioResult") boolean showAudioResult,
      @JsonProperty("audioRequestId") String audioRequestId,
      @JsonProperty("thumbnail") String thumbnail,
      @JsonProperty("contentPreview") String contentPreview,
      @JsonProperty("userId") String userId,
      @JsonProperty("institutionId") String institutionId,
      @JsonProperty("releaseVersion") String releaseVersion,
      @JsonProperty("webhookUrls") List<String> webhookUrls,
      @JsonProperty("createdAt") LocalDateTime createdAt,
      @JsonProperty("updatedAt") LocalDateTime updatedAt,
      @JsonProperty("audioExtractionProcessed") boolean audioExtractionProcessed,
      @JsonProperty("resultsSummary") ResultsSummary resultsSummary,
      @JsonProperty("models") List<ModelResult> models,
      @JsonProperty("rdModels") List<Object> rdModels,
      @JsonProperty("media_metadata_info") MediaMetadataInfo mediaMetadataInfo,
      @JsonProperty("modelMetadataUrl") String modelMetadataUrl,
      @JsonProperty("explainabilityUrl") String explainabilityUrl,
      @JsonProperty("heatmaps") Map<String, String> heatmaps) {
    this.name = name;
    this.filename = filename;
    this.aggregationResultUrl = aggregationResultUrl;
    this.originalFileName = originalFileName;
    this.storageLocation = storageLocation;
    this.convertedFileName = convertedFileName;
    this.convertedFileLocation = convertedFileLocation;
    this.socialLink = socialLink;
    this.socialLinkDownloaded = socialLinkDownloaded;
    this.socialLinkDownloadFailed = socialLinkDownloadFailed;
    this.requestId = requestId;
    this.uploadedDate = uploadedDate;
    this.mediaType = mediaType;
    this.userInfo = userInfo;
    this.audioExtractionFileName = audioExtractionFileName;
    this.showAudioResult = showAudioResult;
    this.audioRequestId = audioRequestId;
    this.thumbnail = thumbnail;
    this.contentPreview = contentPreview;
    this.userId = userId;
    this.institutionId = institutionId;
    this.releaseVersion = releaseVersion;
    this.webhookUrls = webhookUrls;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.audioExtractionProcessed = audioExtractionProcessed;
    this.resultsSummary = resultsSummary;
    this.models = models;
    this.rdModels = rdModels;
    this.mediaMetadataInfo = mediaMetadataInfo;
    this.modelMetadataUrl = modelMetadataUrl;
    this.explainabilityUrl = explainabilityUrl;
    this.heatmaps = heatmaps;
  }

  public DetectionResult(
      String requestId, ResultsSummary resultsSummary, Double score, List<ModelResult> models) {
    this.requestId = requestId;
    this.resultsSummary = resultsSummary;
    this.score = score;
    this.models = models;
  }

  // Getters for all fields
  public String getName() {
    return name;
  }

  public String getFilename() {
    return filename;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public String getStorageLocation() {
    return storageLocation;
  }

  public String getConvertedFileName() {
    return convertedFileName;
  }

  public String getConvertedFileLocation() {
    return convertedFileLocation;
  }

  public String getSocialLink() {
    return socialLink;
  }

  public boolean isSocialLinkDownloaded() {
    return socialLinkDownloaded;
  }

  public boolean isSocialLinkDownloadFailed() {
    return socialLinkDownloadFailed;
  }

  public String getRequestId() {
    return requestId;
  }

  public LocalDateTime getUploadedDate() {
    return uploadedDate;
  }

  public String getMediaType() {
    return mediaType;
  }

  public UserInfo getUserInfo() {
    return userInfo;
  }

  public String getAudioExtractionFileName() {
    return audioExtractionFileName;
  }

  public boolean isShowAudioResult() {
    return showAudioResult;
  }

  public String getAudioRequestId() {
    return audioRequestId;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public String getContentPreview() {
    return contentPreview;
  }

  public String getUserId() {
    return userId;
  }

  public String getInstitutionId() {
    return institutionId;
  }

  public String getReleaseVersion() {
    return releaseVersion;
  }

  public List<String> getWebhookUrls() {
    return webhookUrls;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public boolean isAudioExtractionProcessed() {
    return audioExtractionProcessed;
  }

  public String getStatus() {
    if (this.resultsSummary == null) {
      return null;
    }
    return this.resultsSummary.status;
  }

  /**
   * Gets the normalized detection score (0-1 range) from the results summary.
   *
   * @return the normalized score, or null if not available
   */
  @JsonIgnore
  public Double getScore() {
    if (this.score != null) {
      return score;
    }
    if (resultsSummary != null && resultsSummary.getMetadata() != null) {
      Object finalScore = resultsSummary.getMetadata().get("finalScore");
      if (finalScore instanceof Number) {
        this.score = ((Number) finalScore).doubleValue() / 100.0; // Normalize to 0-1 range
        return this.score;
      }
    }
    return null;
  }

  public ResultsSummary getResultsSummary() {
    return resultsSummary;
  }

  public List<ModelResult> getModels() {
    if (models == null) {
      return null;
    }
    return models.stream()
        .filter(model -> !"NOT_APPLICABLE".equals(model.getStatus()))
        .collect(Collectors.toList());
  }

  public List<Object> getRdModels() {
    return rdModels;
  }

  public MediaMetadataInfo getMediaMetadataInfo() {
    return mediaMetadataInfo;
  }

  public String getModelMetadataUrl() {
    return modelMetadataUrl;
  }

  public String getExplainabilityUrl() {
    return explainabilityUrl;
  }

  public Map<String, String> getHeatmaps() {
    return heatmaps;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DetectionResult that = (DetectionResult) o;
    return Objects.equals(requestId, that.requestId)
        && Objects.equals(this.getStatus(), that.getStatus());
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, this.getStatus());
  }

  @Override
  public String toString() {
    return "DetectionResult{"
        + "requestId='"
        + requestId
        + '\''
        + ", status='"
        + this.getStatus()
        + '\''
        + ", mediaType='"
        + mediaType
        + '\''
        + ", originalFileName='"
        + originalFileName
        + '\''
        + '}';
  }

  public DetectionResult summarize() {
    return new DetectionResult(
        this.requestId, this.resultsSummary, this.getScore(), this.getModels());
  }

  public String getAggregationResultUrl() {
    return this.aggregationResultUrl;
  }

  /** User information associated with the detection request. */
  public static class UserInfo {
    private final String email;
    private final boolean isApi;
    private final String lastName;
    private final String firstName;
    private final List<String> planNames;
    private final String trackingId;
    private final String institutionId;
    private final String institutionName;
    private final String institutionUUID;
    private final List<String> institutionRoles;

    @JsonCreator
    public UserInfo(
        @JsonProperty("email") String email,
        @JsonProperty("isApi") boolean isApi,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("firstName") String firstName,
        @JsonProperty("planNames") List<String> planNames,
        @JsonProperty("trackingId") String trackingId,
        @JsonProperty("institutionId") String institutionId,
        @JsonProperty("institutionName") String institutionName,
        @JsonProperty("institutionUUID") String institutionUUID,
        @JsonProperty("institutionRoles") List<String> institutionRoles) {
      this.email = email;
      this.isApi = isApi;
      this.lastName = lastName;
      this.firstName = firstName;
      this.planNames = planNames;
      this.trackingId = trackingId;
      this.institutionId = institutionId;
      this.institutionName = institutionName;
      this.institutionUUID = institutionUUID;
      this.institutionRoles = institutionRoles;
    }

    // Getters
    public String getEmail() {
      return email;
    }

    public boolean getIsApi() {
      return isApi;
    }

    public String getLastName() {
      return lastName;
    }

    public String getFirstName() {
      return firstName;
    }

    public List<String> getPlanNames() {
      return planNames;
    }

    public String getTrackingId() {
      return trackingId;
    }

    public String getInstitutionId() {
      return institutionId;
    }

    public String getInstitutionName() {
      return institutionName;
    }

    public String getInstitutionUUID() {
      return institutionUUID;
    }

    public List<String> getInstitutionRoles() {
      return institutionRoles;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      UserInfo userInfo = (UserInfo) o;
      return Objects.equals(email, userInfo.email)
          && Objects.equals(institutionId, userInfo.institutionId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(email, institutionId);
    }

    @Override
    public String toString() {
      return "UserInfo{"
          + "email='"
          + email
          + '\''
          + ", institutionName='"
          + institutionName
          + '\''
          + '}';
    }
  }

  /** Summary of detection results. */
  public static class ResultsSummary {

    @JsonDeserialize(using = StatusDeserializer.class)
    private final String status;

    private final Map<String, Object> metadata;

    @JsonCreator
    public ResultsSummary(
        @JsonProperty("status") String status,
        @JsonProperty("metadata") Map<String, Object> metadata) {
      this.status = status;
      this.metadata = metadata;
    }

    public String getStatus() {
      return status;
    }

    public Map<String, Object> getMetadata() {
      return metadata;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ResultsSummary that = (ResultsSummary) o;
      return Objects.equals(status, that.status) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
      return Objects.hash(status, metadata);
    }

    @Override
    public String toString() {
      return "ResultsSummary{" + "status='" + status + '\'' + ", metadata=" + metadata + '}';
    }
  }

  /** Represents a result from an individual detection model. */
  public static class ModelResult {
    private final String name;
    private final Object data;
    private final String error;
    private final String code;

    @JsonDeserialize(using = StatusDeserializer.class)
    private final String status;

    @JsonDeserialize(using = PredictionNumberDeserializer.class)
    private final Double predictionNumber;

    private final Double normalizedPredictionNumber;
    private final Double rollingAvgNumber;
    private final Double finalScore;

    @JsonCreator
    public ModelResult(
        @JsonProperty("name") String name,
        @JsonProperty("data") Object data,
        @JsonProperty("error") String error,
        @JsonProperty("code") String code,
        @JsonProperty("status") String status,
        @JsonProperty("predictionNumber") Double predictionNumber,
        @JsonProperty("normalizedPredictionNumber") Double normalizedPredictionNumber,
        @JsonProperty("rollingAvgNumber") Double rollingAvgNumber,
        @JsonProperty("finalScore") Double finalScore) {
      this.name = name;
      this.data = data;
      this.error = error;
      this.code = code;
      this.status = status;
      this.predictionNumber = predictionNumber;
      this.normalizedPredictionNumber = normalizedPredictionNumber;
      this.rollingAvgNumber = rollingAvgNumber;
      this.finalScore = finalScore;
    }

    // Getters
    public String getName() {
      return name;
    }

    public Object getData() {
      return data;
    }

    public String getError() {
      return error;
    }

    public String getCode() {
      return code;
    }

    public String getStatus() {
      return this.status;
    }

    public Double getPredictionNumber() {
      return predictionNumber;
    }

    public Double getNormalizedPredictionNumber() {
      return normalizedPredictionNumber;
    }

    public Double getRollingAvgNumber() {
      return rollingAvgNumber;
    }

    public Double getFinalScore() {
      return finalScore;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ModelResult that = (ModelResult) o;
      return Objects.equals(name, that.name)
          && Objects.equals(status, that.status)
          && Objects.equals(finalScore, that.finalScore);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, status, finalScore);
    }

    @Override
    public String toString() {
      return "ModelResult{"
          + "name='"
          + name
          + '\''
          + ", status='"
          + status
          + '\''
          + ", finalScore="
          + finalScore
          + '}';
    }
  }

  /** Media metadata information. */
  public static class MediaMetadataInfo {
    private final Integer fileSize;
    private final Map<String, Object> gpsInformation;
    private final Double audioLength;

    @JsonCreator
    public MediaMetadataInfo(
        @JsonProperty("file_size") Integer fileSize,
        @JsonProperty("gps_information") Map<String, Object> gpsInformation,
        @JsonProperty("audio_length") Double audioLength) {
      this.fileSize = fileSize;
      this.gpsInformation = gpsInformation;
      this.audioLength = audioLength;
    }

    public Integer getFileSize() {
      return fileSize;
    }

    public Map<String, Object> getGpsInformation() {
      return gpsInformation;
    }

    public Double getAudioLength() {
      return audioLength;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MediaMetadataInfo that = (MediaMetadataInfo) o;
      return Objects.equals(fileSize, that.fileSize)
          && Objects.equals(gpsInformation, that.gpsInformation)
          && Objects.equals(audioLength, that.audioLength);
    }

    @Override
    public int hashCode() {
      return Objects.hash(fileSize, gpsInformation, audioLength);
    }

    @Override
    public String toString() {
      return "MediaMetadataInfo{"
          + "fileSize="
          + fileSize
          + ", gpsInformation="
          + gpsInformation
          + ", audioLength="
          + audioLength
          + '}';
    }
  }

  public static class StatusDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(
        com.fasterxml.jackson.core.JsonParser p,
        com.fasterxml.jackson.databind.DeserializationContext ctxt)
        throws java.io.IOException {
      String node = p.getValueAsString();
      if (node == null) {
        return "UNKNOWN";
      }
      if (node.equals("FAKE")) {
        return "MANIPULATED";
      }
      return node;
    }
  }

  public static class PredictionNumberDeserializer extends JsonDeserializer<Double> {
    @Override
    public Double deserialize(
        com.fasterxml.jackson.core.JsonParser p,
        com.fasterxml.jackson.databind.DeserializationContext ctxt)
        throws java.io.IOException {
      JsonNode node = p.readValueAsTree();

      if (node.isNull()) {
        return null;
      }

      if (node.isNumber()) {
        return node.asDouble();
      }

      // If it's an object or any other type, return null
      return null;
    }
  }
}

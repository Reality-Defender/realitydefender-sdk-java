package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonCreator;
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
public class DetectionResult {

  private final String name;
  private final String filename;
  private final String aggregationResultUrl;
  private final String originalFileName;
  private final String storageLocation;
  private final String convertedFileName;
  private final String convertedFileLocation;
  private final String socialLink;
  private final boolean socialLinkDownloaded;
  private final boolean socialLinkDownloadFailed;
  private final String requestId;
  private final LocalDateTime uploadedDate;
  private final String mediaType;
  private final UserInfo userInfo;
  private final String audioExtractionFileName;
  private final boolean showAudioResult;
  private final String audioRequestId;
  private final String thumbnail;
  private final String contentPreview;
  private final String userId;
  private final String institutionId;
  private final String releaseVersion;
  private final List<String> webhookUrls;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;
  private final boolean audioExtractionProcessed;

  @JsonDeserialize(using = StatusDeserializer.class)
  private final String overallStatus;

  private final ResultsSummary resultsSummary;
  private final List<ModelResult> models;
  private final List<Object> rdModels;
  private final MediaMetadataInfo mediaMetadataInfo;
  private final String modelMetadataUrl;
  private final String explainabilityUrl;
  private final Map<String, String> heatmaps;

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
      @JsonProperty("overallStatus") String overallStatus,
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
    this.overallStatus = overallStatus;
    this.resultsSummary = resultsSummary;
    this.models = models;
    this.rdModels = rdModels;
    this.mediaMetadataInfo = mediaMetadataInfo;
    this.modelMetadataUrl = modelMetadataUrl;
    this.explainabilityUrl = explainabilityUrl;
    this.heatmaps = heatmaps;
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

  public String getOverallStatus() {
    if (this.overallStatus == null) {
      return null;
    } else if (this.overallStatus.equals("FAKE")) {
      return "MANIPULATED";
    } else {
      return this.overallStatus;
    }
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
        && Objects.equals(overallStatus, that.overallStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, overallStatus);
  }

  @Override
  public String toString() {
    return "DetectionResult{"
        + "requestId='"
        + requestId
        + '\''
        + ", overallStatus='"
        + overallStatus
        + '\''
        + ", mediaType='"
        + mediaType
        + '\''
        + ", originalFileName='"
        + originalFileName
        + '\''
        + '}';
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
      return status;
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

    @JsonCreator
    public MediaMetadataInfo(
        @JsonProperty("file_size") Integer fileSize,
        @JsonProperty("gps_information") Map<String, Object> gpsInformation) {
      this.fileSize = fileSize;
      this.gpsInformation = gpsInformation;
    }

    public Map<String, Object> getGpsInformation() {
      return gpsInformation;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MediaMetadataInfo that = (MediaMetadataInfo) o;
      return Objects.equals(fileSize, that.fileSize)
          && Objects.equals(gpsInformation, that.gpsInformation);
    }

    @Override
    public int hashCode() {
      return Objects.hash(gpsInformation);
    }

    @Override
    public String toString() {
      return "MediaMetadataInfo{" + "gpsInformation=" + gpsInformation + '}';
    }
  }

  public static class StatusDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(
        com.fasterxml.jackson.core.JsonParser p,
        com.fasterxml.jackson.databind.DeserializationContext ctxt)
        throws java.io.IOException {
      JsonNode node = p.readValueAsTree();

      if (node.asText().equals("FAKE")) {
        return "MANIPULATED";
      }

      return node.asText();
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

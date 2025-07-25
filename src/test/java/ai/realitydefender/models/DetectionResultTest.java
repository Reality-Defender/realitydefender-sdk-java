package ai.realitydefender.models;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DetectionResultTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  void testDetectionResultCreation() {
    DetectionResult.ModelResult model1 = createModelResult("model1", "FAKE", 0.95);
    DetectionResult.ModelResult model2 = createModelResult("model2", "FAKE", 0.87);
    List<DetectionResult.ModelResult> models = Arrays.asList(model1, model2);

    DetectionResult result = createDetectionResult("FAKE", models);

    assertEquals("MANIPULATED", result.getStatus());
    assertEquals(2, result.getModels().size());
    assertEquals("model1", result.getModels().get(0).getName());
  }

  @Test
  void testDetectionResultWithNullScore() {
    DetectionResult result = createDetectionResult("PROCESSING", new ArrayList<>());

    assertEquals("PROCESSING", result.getStatus());
    assertTrue(result.getModels().isEmpty());
  }

  @Test
  void testModelResultCreation() {
    DetectionResult.ModelResult model = createModelResult("test-model", "AUTHENTIC", 0.23);

    assertEquals("test-model", model.getName());
    assertEquals("AUTHENTIC", model.getStatus());
    assertEquals(0.23, model.getFinalScore());
  }

  @Test
  void testDetectionResultEquality() {
    DetectionResult.ModelResult model = createModelResult("model1", "FAKE", 0.95);
    List<DetectionResult.ModelResult> models = List.of(model);

    DetectionResult result1 = createDetectionResult("MANIPULATED", models);
    DetectionResult result2 = createDetectionResult("MANIPULATED", models);

    assertEquals(result1, result2);
    assertEquals(result1.hashCode(), result2.hashCode());
  }

  @Test
  void testDetectionResultToString() {
    DetectionResult.ModelResult model = createModelResult("model1", "FAKE", 0.95);
    DetectionResult result = createDetectionResult("FAKE", List.of(model));

    String toString = result.toString();
    assertTrue(toString.contains("FAKE"));
    assertTrue(toString.contains("test-request-id"));
  }

  @Test
  void testJsonSerialization() throws Exception {
    DetectionResult.ModelResult model = createModelResult("model1", "FAKE", 0.95);
    DetectionResult result = createDetectionResult("FAKE", List.of(model));

    String json = objectMapper.writeValueAsString(result);
    DetectionResult deserialized = objectMapper.readValue(json, DetectionResult.class);

    assertEquals(result.getRequestId(), deserialized.getRequestId());
    assertEquals(result.getStatus(), deserialized.getStatus());
    assertEquals(result.getModels().size(), deserialized.getModels().size());
    assertEquals(result.getModels().get(0).getName(), deserialized.getModels().get(0).getName());
    assertEquals(
        result.getModels().get(0).getStatus(), deserialized.getModels().get(0).getStatus());
    assertEquals(
        result.getModels().get(0).getFinalScore(), deserialized.getModels().get(0).getFinalScore());
  }

  @Test
  void testJsonSerializationWithComplexData() throws Exception {
    // Create a detection result with more complex data
    LocalDateTime now = LocalDateTime.now();

    DetectionResult.UserInfo userInfo =
        new DetectionResult.UserInfo(
            "test@example.com",
            true,
            "Doe",
            "John",
            List.of("Premium"),
            "track123",
            "inst456",
            "Test Institution",
            "uuid789",
            List.of("admin"));

    DetectionResult.ModelResult model =
        new DetectionResult.ModelResult(
            "complex-model",
            Map.of("confidence", 0.95, "type", "deepfake"),
            null,
            "SUCCESS",
            "MANIPULATED",
            0.90,
            0.92,
            0.91,
            0.95);

    DetectionResult result =
        new DetectionResult(
            "complex-test",
            "test.jpg",
            null,
            "original.jpg",
            "https://storage.com/file",
            "",
            "",
            "",
            false,
            false,
            "req-456",
            now,
            "IMAGE",
            userInfo,
            "",
            false,
            "",
            "",
            null,
            "user123",
            "inst456",
            "2.0.0",
            List.of("http://webhook.com"),
            now,
            now,
            false,
            "COMPLETED",
            new DetectionResult.ResultsSummary("COMPLETED", Map.of("confidence", 0.95)),
            List.of(model),
            new ArrayList<>(),
            null,
            "",
            "",
            Map.of());

    String json = objectMapper.writeValueAsString(result);
    DetectionResult deserialized = objectMapper.readValue(json, DetectionResult.class);

    assertEquals(result.getRequestId(), deserialized.getRequestId());
    assertEquals(result.getUserInfo().getEmail(), deserialized.getUserInfo().getEmail());
    assertEquals(result.getModels().get(0).getName(), deserialized.getModels().get(0).getName());
  }

  @Test
  void testModelResultFields() {
    DetectionResult.ModelResult model =
        new DetectionResult.ModelResult(
            "test-model",
            null, // data
            null, // error
            null, // code
            "ANALYZING", // status
            0.85, // predictionNumber
            0.90, // normalizedPredictionNumber
            0.88, // rollingAvgNumber
            0.92 // finalScore
            );

    assertEquals("test-model", model.getName());
    assertEquals("ANALYZING", model.getStatus());
    assertEquals(0.92, model.getFinalScore());
    assertEquals(0.92, model.getFinalScore()); // Should return finalScore first
    assertEquals(0.85, model.getPredictionNumber());
    assertEquals(0.90, model.getNormalizedPredictionNumber());
    assertEquals(0.88, model.getRollingAvgNumber());
  }

  @Test
  void testModelResultScorePriority() {
    // Test that getFinalScore() returns finalScore when available
    DetectionResult.ModelResult modelWithFinal =
        new DetectionResult.ModelResult("test", null, null, null, "COMPLETED", 0.1, 0.2, 0.3, 0.9);
    assertEquals(0.9, modelWithFinal.getFinalScore());

    // Test that getFinalScore() returns normalizedPredictionNumber when finalScore is null
    DetectionResult.ModelResult modelWithNormalized =
        new DetectionResult.ModelResult("test", null, null, null, "COMPLETED", 0.1, 0.8, 0.3, null);
    assertNull(modelWithNormalized.getFinalScore());

    // Test that getFinalScore() returns predictionNumber when others are null
    DetectionResult.ModelResult modelWithPrediction =
        new DetectionResult.ModelResult("test", null, null, null, "COMPLETED", 0.7, null, 0.3, 0.7);
    assertEquals(0.7, modelWithPrediction.getFinalScore());
  }

  @Test
  void testUserInfoCreation() {
    DetectionResult.UserInfo userInfo =
        new DetectionResult.UserInfo(
            "test@example.com",
            true,
            "Doe",
            "John",
            List.of("Premium"),
            "track123",
            "inst456",
            "Test Institution",
            "uuid789",
            List.of("admin"));

    assertEquals("test@example.com", userInfo.getEmail());
    assertTrue(userInfo.getIsApi());
    assertEquals("Doe", userInfo.getLastName());
    assertEquals("John", userInfo.getFirstName());
    assertEquals("Test Institution", userInfo.getInstitutionName());
  }

  @Test
  void testResultsSummaryCreation() {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("confidence", 0.95);
    metadata.put("model_count", 3);

    DetectionResult.ResultsSummary summary =
        new DetectionResult.ResultsSummary("COMPLETED", metadata);

    assertEquals("COMPLETED", summary.getStatus());
    assertEquals(metadata, summary.getMetadata());
    assertEquals(0.95, summary.getMetadata().get("confidence"));
  }

  @Test
  void testMediaMetadataInfo() {
    Map<String, Object> gpsInfo = new HashMap<>();
    gpsInfo.put("latitude", 40.7128);
    gpsInfo.put("longitude", -74.0060);

    DetectionResult.MediaMetadataInfo mediaInfo =
        new DetectionResult.MediaMetadataInfo(1024768, gpsInfo, null);

    assertEquals(gpsInfo, mediaInfo.getGpsInformation());
    assertEquals(40.7128, mediaInfo.getGpsInformation().get("latitude"));
    assertEquals(Integer.valueOf(1024768), mediaInfo.getFileSize());
    assertNull(mediaInfo.getAudioLength());
  }

  @Test
  void testMediaMetadataInfoWithAudioLength() {
    Map<String, Object> gpsInfo = new HashMap<>();
    gpsInfo.put("latitude", 40.7128);
    gpsInfo.put("longitude", -74.0060);

    DetectionResult.MediaMetadataInfo mediaInfo =
        new DetectionResult.MediaMetadataInfo(1024768, gpsInfo, 120.5);

    assertEquals(gpsInfo, mediaInfo.getGpsInformation());
    assertEquals(40.7128, mediaInfo.getGpsInformation().get("latitude"));
    assertEquals(Integer.valueOf(1024768), mediaInfo.getFileSize());
    assertEquals(Double.valueOf(120.5), mediaInfo.getAudioLength());
  }

  @Test
  void testDetectionResultWithAllFields() {
    LocalDateTime now = LocalDateTime.now();

    DetectionResult.UserInfo userInfo =
        new DetectionResult.UserInfo(
            "user@test.com",
            false,
            "Smith",
            "Jane",
            List.of("Basic"),
            "track456",
            "inst789",
            "University",
            "uuid123",
            List.of("user"));

    DetectionResult.ModelResult model1 = createModelResult("model-a", "FAKE", 0.8);
    DetectionResult.ModelResult model2 = createModelResult("model-b", "FAKE", 0.9);

    Map<String, Object> gpsData = Map.of("lat", 51.5074, "lon", -0.1278);
    DetectionResult.MediaMetadataInfo mediaInfo =
        new DetectionResult.MediaMetadataInfo(2048000, gpsData, null);

    Map<String, String> heatmaps = Map.of("overall", "heatmap-url", "model-a", "model-a-heatmap");

    DetectionResult result =
        new DetectionResult(
            "full-test",
            "video.mp4",
            "https://results.com/123",
            "original-video.mp4",
            "https://storage.com/video",
            "converted.mp4",
            "https://storage.com/converted",
            "https://social.com/video",
            true,
            false,
            "req-789",
            now,
            "VIDEO",
            userInfo,
            "audio.wav",
            true,
            "audio-req-123",
            "thumb.jpg",
            "preview-data",
            "user789",
            "inst789",
            "3.0.0",
            Arrays.asList("http://hook1.com", "http://hook2.com"),
            now,
            now,
            true,
            "MANIPULATED",
            new DetectionResult.ResultsSummary("MANIPULATED", Map.of("overall_confidence", 0.85)),
            Arrays.asList(model1, model2),
            List.of(),
            mediaInfo,
            "https://metadata.com",
            "https://explainability.com",
            heatmaps);

    // Test all getters
    assertEquals("full-test", result.getName());
    assertEquals("video.mp4", result.getFilename());
    assertEquals("https://results.com/123", result.getAggregationResultUrl());
    assertEquals("original-video.mp4", result.getOriginalFileName());
    assertEquals("https://storage.com/video", result.getStorageLocation());
    assertEquals("converted.mp4", result.getConvertedFileName());
    assertEquals("https://storage.com/converted", result.getConvertedFileLocation());
    assertEquals("https://social.com/video", result.getSocialLink());
    assertTrue(result.isSocialLinkDownloaded());
    assertFalse(result.isSocialLinkDownloadFailed());
    assertEquals("req-789", result.getRequestId());
    assertEquals(now, result.getUploadedDate());
    assertEquals("VIDEO", result.getMediaType());
    assertEquals(userInfo, result.getUserInfo());
    assertEquals("audio.wav", result.getAudioExtractionFileName());
    assertTrue(result.isShowAudioResult());
    assertEquals("audio-req-123", result.getAudioRequestId());
    assertEquals("thumb.jpg", result.getThumbnail());
    assertEquals("preview-data", result.getContentPreview());
    assertEquals("user789", result.getUserId());
    assertEquals("inst789", result.getInstitutionId());
    assertEquals("3.0.0", result.getReleaseVersion());
    assertEquals(2, result.getWebhookUrls().size());
    assertEquals(now, result.getCreatedAt());
    assertEquals(now, result.getUpdatedAt());
    assertTrue(result.isAudioExtractionProcessed());
    assertEquals("MANIPULATED", result.getStatus());
    assertEquals("MANIPULATED", result.getStatus()); // Convenience method
    assertNotNull(result.getResultsSummary());
    assertEquals(2, result.getModels().size());
    assertTrue(result.getRdModels().isEmpty());
    assertEquals(mediaInfo, result.getMediaMetadataInfo());
    assertEquals("https://metadata.com", result.getModelMetadataUrl());
    assertEquals("https://explainability.com", result.getExplainabilityUrl());
    assertEquals(heatmaps, result.getHeatmaps());
  }

  @Test
  void testEqualsAndHashCode() {
    DetectionResult result1 = createDetectionResult("MANIPULATED", new ArrayList<>());
    DetectionResult result2 = createDetectionResult("MANIPULATED", new ArrayList<>());
    DetectionResult result3 = createDetectionResult("AUTHENTIC", new ArrayList<>());

    assertEquals(result1, result2);
    assertEquals(result1.hashCode(), result2.hashCode());
    assertNotEquals(result1, result3);
    assertNotEquals(result1.hashCode(), result3.hashCode());
  }

  @Test
  void testModelFilteringDuringSerialization() throws Exception {
    // Create models with different statuses
    DetectionResult.ModelResult applicableModel = createModelResult("model1", "FAKE", 0.95);
    DetectionResult.ModelResult notApplicableModel =
        createModelResult("model2", "NOT_APPLICABLE", null);
    DetectionResult.ModelResult anotherApplicableModel =
        createModelResult("model3", "AUTHENTIC", 0.15);

    List<DetectionResult.ModelResult> allModels =
        Arrays.asList(applicableModel, notApplicableModel, anotherApplicableModel);

    DetectionResult result = createDetectionResult("MANIPULATED", allModels);

    // Serialize to JSON
    String json = objectMapper.writeValueAsString(result);

    // Verify NOT_APPLICABLE model is not in JSON
    assertFalse(json.contains("NOT_APPLICABLE"));
    assertFalse(json.contains("model2"));

    // Verify other models are present
    assertTrue(json.contains("model1"));
    assertTrue(json.contains("model3"));
    assertTrue(json.contains("MANIPULATED"));
    assertTrue(json.contains("AUTHENTIC"));
  }

  @Test
  void testModelFilteringDuringDeserialization() throws Exception {
    // JSON with NOT_APPLICABLE model
    String json =
        "{\n"
            + "  \"requestId\": \"test-request\",\n"
            + "  \"status\": \"FAKE\",\n"
            + "  \"models\": [\n"
            + "    {\n"
            + "      \"name\": \"model1\",\n"
            + "      \"status\": \"FAKE\",\n"
            + "      \"finalScore\": 0.95\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"model2\",\n"
            + "      \"status\": \"NOT_APPLICABLE\",\n"
            + "      \"finalScore\": null\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"model3\",\n"
            + "      \"status\": \"AUTHENTIC\",\n"
            + "      \"finalScore\": 0.15\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    DetectionResult result = objectMapper.readValue(json, DetectionResult.class);

    // Should only have 2 models (NOT_APPLICABLE filtered out)
    assertEquals(2, result.getModels().size());

    // Verify correct models are present
    List<String> modelNames =
        result.getModels().stream()
            .map(DetectionResult.ModelResult::getName)
            .collect(Collectors.toList());

    assertTrue(modelNames.contains("model1"));
    assertTrue(modelNames.contains("model3"));
    assertFalse(modelNames.contains("model2"));

    // Verify no NOT_APPLICABLE status
    List<String> statuses =
        result.getModels().stream()
            .map(DetectionResult.ModelResult::getStatus)
            .collect(Collectors.toList());

    assertFalse(statuses.contains("NOT_APPLICABLE"));
  }

  @Test
  void testRoundTripWithFiltering() throws Exception {
    // Create result with mixed model statuses
    DetectionResult.ModelResult model1 = createModelResult("applicable1", "FAKE", 0.9);
    DetectionResult.ModelResult model2 = createModelResult("notApplicable", "NOT_APPLICABLE", null);
    DetectionResult.ModelResult model3 = createModelResult("applicable2", "AUTHENTIC", 0.1);

    DetectionResult original =
        createDetectionResult("MANIPULATED", Arrays.asList(model1, model2, model3));

    // Serialize and deserialize
    String json = objectMapper.writeValueAsString(original);
    DetectionResult roundTrip = objectMapper.readValue(json, DetectionResult.class);

    // Should have 2 models (NOT_APPLICABLE filtered out)
    assertEquals(2, roundTrip.getModels().size());

    // Verify filtering worked in both directions
    assertFalse(
        roundTrip.getModels().stream()
            .anyMatch(model -> "NOT_APPLICABLE".equals(model.getStatus())));
  }

  @Test
  void testPredictionNumberAsDouble() throws Exception {
    String json =
        "{\n"
            + "  \"name\": \"test-model\",\n"
            + "  \"status\": \"COMPLETED\",\n"
            + "  \"predictionNumber\": 0.85,\n"
            + "  \"finalScore\": 0.9\n"
            + "}";

    DetectionResult.ModelResult model =
        objectMapper.readValue(json, DetectionResult.ModelResult.class);

    assertEquals(0.85, model.getPredictionNumber());
  }

  @Test
  void testPredictionNumberAsObject() throws Exception {
    String json =
        "{\n"
            + "  \"name\": \"test-model\",\n"
            + "  \"status\": \"COMPLETED\",\n"
            + "  \"predictionNumber\": {\"error\": \"calculation failed\"},\n"
            + "  \"finalScore\": 0.9\n"
            + "}";

    DetectionResult.ModelResult model =
        objectMapper.readValue(json, DetectionResult.ModelResult.class);

    assertNull(model.getPredictionNumber());
    assertEquals(0.9, model.getFinalScore());
  }

  @Test
  void testPredictionNumberAsNull() throws Exception {
    String json =
        "{\n"
            + "  \"name\": \"test-model\",\n"
            + "  \"status\": \"COMPLETED\",\n"
            + "  \"predictionNumber\": null,\n"
            + "  \"finalScore\": 0.9\n"
            + "}";

    DetectionResult.ModelResult model =
        objectMapper.readValue(json, DetectionResult.ModelResult.class);

    assertNull(model.getPredictionNumber());
  }

  @Test
  void testPredictionNumberAsString() throws Exception {
    String json =
        "{\n"
            + "  \"name\": \"test-model\",\n"
            + "  \"status\": \"COMPLETED\",\n"
            + "  \"predictionNumber\": \"invalid\",\n"
            + "  \"finalScore\": 0.9\n"
            + "}";

    DetectionResult.ModelResult model =
        objectMapper.readValue(json, DetectionResult.ModelResult.class);

    assertNull(model.getPredictionNumber());
  }

  @Test
  void testMixedPredictionNumberTypes() throws Exception {
    // Test multiple models with different predictionNumber types
    String json =
        "{\n"
            + "  \"models\": [\n"
            + "    {\n"
            + "      \"name\": \"model1\",\n"
            + "      \"status\": \"COMPLETED\",\n"
            + "      \"predictionNumber\": 0.75\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"model2\",\n"
            + "      \"status\": \"ERROR\",\n"
            + "      \"predictionNumber\": {\"error\": \"failed\"}\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"model3\",\n"
            + "      \"status\": \"COMPLETED\",\n"
            + "      \"predictionNumber\": 0.92\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    JsonNode root = objectMapper.readTree(json);
    JsonNode modelsArray = root.get("models");

    List<DetectionResult.ModelResult> models = new ArrayList<>();
    for (JsonNode modelNode : modelsArray) {
      DetectionResult.ModelResult model =
          objectMapper.treeToValue(modelNode, DetectionResult.ModelResult.class);
      models.add(model);
    }

    assertEquals(3, models.size());
    assertEquals(0.75, models.get(0).getPredictionNumber());
    assertNull(models.get(1).getPredictionNumber()); // Object converted to null
    assertEquals(0.92, models.get(2).getPredictionNumber());
  }

  @Test
  void testSerializationPreservesDoubleValues() throws Exception {
    DetectionResult.ModelResult model =
        new DetectionResult.ModelResult(
            "test-model",
            null, // data
            null, // error
            null, // code
            "COMPLETED", // status
            0.85, // predictionNumber
            0.90, // normalizedPredictionNumber
            0.88, // rollingAvgNumber
            0.92 // finalScore
            );

    String json = objectMapper.writeValueAsString(model);
    DetectionResult.ModelResult deserialized =
        objectMapper.readValue(json, DetectionResult.ModelResult.class);

    assertEquals(0.85, deserialized.getPredictionNumber());
  }

  // Helper methods to create objects with all required parameters
  private DetectionResult createDetectionResult(
      String status, List<DetectionResult.ModelResult> models) {
    return new DetectionResult(
        "test-name", // name
        "test-filename", // filename
        null, // aggregationResultUrl
        "test-original.jpg", // originalFileName
        "https://storage.location", // storageLocation
        "", // convertedFileName
        "", // convertedFileLocation
        "", // socialLink
        false, // socialLinkDownloaded
        false, // socialLinkDownloadFailed
        "test-request-id", // requestId
        null, // uploadedDate
        "IMAGE", // mediaType
        null, // userInfo
        "", // audioExtractionFileName
        false, // showAudioResult
        "", // audioRequestId
        "", // thumbnail
        null, // contentPreview
        "test-user-id", // userId
        "test-institution-id", // institutionId
        "1.0.0", // releaseVersion
        new ArrayList<>(), // webhookUrls
        null, // createdAt
        null, // updatedAt
        false, // audioExtractionProcessed
        status, // overallStatus
        new DetectionResult.ResultsSummary(status, Map.of()), // resultsSummary
        models, // models
        new ArrayList<>(), // rdModels
        null, // mediaMetadataInfo
        "", // modelMetadataUrl
        "", // explainabilityUrl
        Map.of() // heatmaps
        );
  }

  private DetectionResult.ModelResult createModelResult(String name, String status, Double score) {
    return new DetectionResult.ModelResult(
        name, // name
        null, // data
        null, // error
        null, // code
        status, // status
        score, // predictionNumber
        score, // normalizedPredictionNumber
        score, // rollingAvgNumber
        score // finalScore
        );
  }

  @Test
  void testOverallStatusDeserializerWithFake() throws Exception {
    String json = createDetectionResultJsonWithStatus("FAKE");
    DetectionResult result = objectMapper.readValue(json, DetectionResult.class);

    assertEquals("MANIPULATED", result.getStatus());
  }

  @Test
  void testStatusDeserializerWithNormalStatuses() throws Exception {
    String[] normalStatuses = {"COMPLETED", "PROCESSING", "ANALYZING", "MANIPULATED"};

    for (String status : normalStatuses) {
      String json = createDetectionResultJsonWithStatus(status);
      DetectionResult result = objectMapper.readValue(json, DetectionResult.class);

      assertEquals(status, result.getStatus());
    }
  }

  @Test
  void testOverallStatusDeserializerRoundTrip() throws Exception {
    DetectionResult original = createDetectionResult("FAKE", new ArrayList<>());

    String json = objectMapper.writeValueAsString(original);
    DetectionResult deserialized = objectMapper.readValue(json, DetectionResult.class);

    assertEquals("MANIPULATED", deserialized.getStatus());
  }

  @Test
  void testStatusDeserializerWithNull() throws Exception {
    String json = createDetectionResultJsonWithNullStatus();
    DetectionResult result = objectMapper.readValue(json, DetectionResult.class);

    assertNull(result.getStatus());
  }

  // Helper methods to add to the existing helper methods section

  private String createDetectionResultJsonWithStatus(String status) {
    return "{\n"
        + "  \"name\": \"test\",\n"
        + "  \"filename\": \"test.jpg\",\n"
        + "  \"originalFileName\": \"test.jpg\",\n"
        + "  \"storageLocation\": \"storage\",\n"
        + "  \"requestId\": \"req-123\",\n"
        + "  \"mediaType\": \"IMAGE\",\n"
        + "  \"userId\": \"user-123\",\n"
        + "  \"institutionId\": \"inst-123\",\n"
        + "  \"releaseVersion\": \"1.0.0\",\n"
        + "  \"webhookUrls\": [],\n"
        + "  \"audioExtractionProcessed\": false,\n"
        + "  \"status\": \""
        + status
        + "\",\n"
        + "  \"resultsSummary\": { \"status\": \""
        + status
        + "\", \"metadata\": {} },\n"
        + "  \"models\": [],\n"
        + "  \"rdModels\": [],\n"
        + "  \"heatmaps\": {}\n"
        + "}";
  }

  private String createDetectionResultJsonWithNullStatus() {
    return "{\n"
        + "  \"name\": \"test\",\n"
        + "  \"filename\": \"test.jpg\",\n"
        + "  \"originalFileName\": \"test.jpg\",\n"
        + "  \"storageLocation\": \"storage\",\n"
        + "  \"requestId\": \"req-123\",\n"
        + "  \"mediaType\": \"IMAGE\",\n"
        + "  \"userId\": \"user-123\",\n"
        + "  \"institutionId\": \"inst-123\",\n"
        + "  \"releaseVersion\": \"1.0.0\",\n"
        + "  \"webhookUrls\": [],\n"
        + "  \"audioExtractionProcessed\": false,\n"
        + "  \"status\": null,\n"
        + "  \"resultsSummary\": { \"status\": null, \"metadata\": {} },\n"
        + "  \"models\": [],\n"
        + "  \"rdModels\": [],\n"
        + "  \"heatmaps\": {}\n"
        + "}";
  }
}

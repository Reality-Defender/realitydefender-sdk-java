package ai.realitydefender;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ai.realitydefender.models.DetectionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NullResultsSummaryStatusTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  void nullResultsSummaryUsesOverallStatusDownloading() throws Exception {
    DetectionResult result =
        objectMapper.readValue(
            "{"
                + "\"requestId\":\"req-social\","
                + "\"overallStatus\":\"DOWNLOADING\","
                + "\"resultsSummary\":null,"
                + "\"models\":[]"
                + "}",
            DetectionResult.class);

    assertEquals("DOWNLOADING", result.getStatus());
  }

  @Test
  void nullResultsSummaryUsesOverallStatusAnalyzing() throws Exception {
    DetectionResult result =
        objectMapper.readValue(
            "{"
                + "\"requestId\":\"req-social\","
                + "\"overallStatus\":\"ANALYZING\","
                + "\"resultsSummary\":null,"
                + "\"models\":[]"
                + "}",
            DetectionResult.class);

    assertEquals("ANALYZING", result.getStatus());
  }
}

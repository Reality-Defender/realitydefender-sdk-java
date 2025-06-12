package ai.realitydefender.models;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class DetectionResultTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDetectionResultCreation() {
        DetectionResult.ModelResult model1 = new DetectionResult.ModelResult("model1", "ARTIFICIAL", 0.95);
        DetectionResult.ModelResult model2 = new DetectionResult.ModelResult("model2", "ARTIFICIAL", 0.87);
        List<DetectionResult.ModelResult> models = Arrays.asList(model1, model2);

        DetectionResult result = new DetectionResult("ARTIFICIAL", 0.91, models);

        assertEquals("ARTIFICIAL", result.getStatus());
        assertEquals(0.91, result.getScore());
        assertEquals(2, result.getModels().size());
        assertEquals("model1", result.getModels().get(0).getName());
    }

    @Test
    void testDetectionResultWithNullScore() {
        DetectionResult result = new DetectionResult("PROCESSING", null, Arrays.asList());

        assertEquals("PROCESSING", result.getStatus());
        assertNull(result.getScore());
        assertTrue(result.getModels().isEmpty());
    }

    @Test
    void testModelResultCreation() {
        DetectionResult.ModelResult model = new DetectionResult.ModelResult("test-model", "AUTHENTIC", 0.23);

        assertEquals("test-model", model.getName());
        assertEquals("AUTHENTIC", model.getStatus());
        assertEquals(0.23, model.getScore());
    }

    @Test
    void testDetectionResultEquality() {
        DetectionResult.ModelResult model = new DetectionResult.ModelResult("model1", "ARTIFICIAL", 0.95);
        List<DetectionResult.ModelResult> models = Arrays.asList(model);

        DetectionResult result1 = new DetectionResult("ARTIFICIAL", 0.91, models);
        DetectionResult result2 = new DetectionResult("ARTIFICIAL", 0.91, models);

        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void testDetectionResultToString() {
        DetectionResult.ModelResult model = new DetectionResult.ModelResult("model1", "ARTIFICIAL", 0.95);
        DetectionResult result = new DetectionResult("ARTIFICIAL", 0.91, Arrays.asList(model));

        String toString = result.toString();
        assertTrue(toString.contains("ARTIFICIAL"));
        assertTrue(toString.contains("0.91"));
    }

    @Test
    void testJsonSerialization() throws Exception {
        DetectionResult.ModelResult model = new DetectionResult.ModelResult("model1", "ARTIFICIAL", 0.95);
        DetectionResult result = new DetectionResult("ARTIFICIAL", 0.91, Arrays.asList(model));

        String json = objectMapper.writeValueAsString(result);
        DetectionResult deserialized = objectMapper.readValue(json, DetectionResult.class);

        assertEquals(result, deserialized);
    }
}
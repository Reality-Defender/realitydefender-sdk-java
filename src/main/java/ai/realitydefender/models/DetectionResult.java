package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of a deepfake detection analysis.
 */
public class DetectionResult {

    private final String status;
    private final Double score;
    private final List<ModelResult> models;

    @JsonCreator
    public DetectionResult(
            @JsonProperty("status") String status,
            @JsonProperty("score") Double score,
            @JsonProperty("models") List<ModelResult> models) {
        this.status = status;
        this.score = score;
        this.models = models;
    }

    /**
     * Gets the overall detection status.
     *
     * @return the status (e.g., "ARTIFICIAL", "AUTHENTIC")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets the overall confidence score.
     *
     * @return the score between 0.0 and 1.0, or null if not available
     */
    public Double getScore() {
        return score;
    }

    /**
     * Gets the individual model results.
     *
     * @return list of model-specific results
     */
    public List<ModelResult> getModels() {
        return models;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectionResult that = (DetectionResult) o;
        return Objects.equals(status, that.status) &&
                Objects.equals(score, that.score) &&
                Objects.equals(models, that.models);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, score, models);
    }

    @Override
    public String toString() {
        return "DetectionResult{" +
                "status='" + status + '\'' +
                ", score=" + score +
                ", models=" + models +
                '}';
    }

    /**
     * Represents a result from an individual detection model.
     */
    public static class ModelResult {
        private final String name;
        private final String status;
        private final Double score;

        @JsonCreator
        public ModelResult(
                @JsonProperty("name") String name,
                @JsonProperty("status") String status,
                @JsonProperty("score") Double score) {
            this.name = name;
            this.status = status;
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public String getStatus() {
            return status;
        }

        public Double getScore() {
            return score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModelResult that = (ModelResult) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(status, that.status) &&
                    Objects.equals(score, that.score);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, status, score);
        }

        @Override
        public String toString() {
            return "ModelResult{" +
                    "name='" + name + '\'' +
                    ", status='" + status + '\'' +
                    ", score=" + score +
                    '}';
        }
    }
}
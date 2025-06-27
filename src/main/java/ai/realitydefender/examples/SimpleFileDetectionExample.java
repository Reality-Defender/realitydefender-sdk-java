package ai.realitydefender.examples;

import ai.realitydefender.RealityDefender;
import ai.realitydefender.exceptions.RealityDefenderException;
import ai.realitydefender.models.DetectionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;

public class SimpleFileDetectionExample {
  public static void main(String[] args) {
    // Initialize the Reality Defender client

    try (RealityDefender client =
        RealityDefender.builder().apiKey(System.getenv("REALITY_DEFENDER_API_KEY")).build()) {
      try {
        // Analyze a suspicious image file
        File imageFile = new File("test_image.jpg");

        System.out.println("Analyzing file: " + imageFile.getName());

        // Perform detection (uploads file and waits for results)
        DetectionResult result = client.detectFile(imageFile);

        // Display results
        System.out.println("Detection Status: " + result.getOverallStatus());

        if (result.getModels() != null) {
          System.out.println("\nIndividual Model Results:");
          for (DetectionResult.ModelResult model : result.getModels()) {
            System.out.printf("- %s: %s%n", model.getName(), model.getStatus());
          }
        }

        // Interpret results
        if ("FAKE".equals(result.getOverallStatus())) {
          System.out.println(
              "\n⚠️  WARNING: This media appears to be artificially generated or manipulated!");
        } else if ("AUTHENTIC".equals(result.getOverallStatus())) {
          System.out.println("\n✅ This media appears to be authentic.");
        } else {
          System.out.println("\n❓ Analysis inconclusive or still processing.");
        }

      } catch (RealityDefenderException | JsonProcessingException e) {
        System.err.println("Error during detection: " + e.getMessage());
      }
    } catch (Exception e) {
      System.err.println("Error closing client: " + e.getMessage());
    }
  }
}

package ai.realitydefender.examples;

import ai.realitydefender.RealityDefender;
import ai.realitydefender.exceptions.RealityDefenderException;
import ai.realitydefender.models.DetectionResult;
import ai.realitydefender.models.UploadResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Duration;

public class SocialMediaDetectionExample {
  public static void main(String[] args) {
    // Initialize the Reality Defender client
    try (RealityDefender client =
        RealityDefender.builder().apiKey(System.getenv("REALITY_DEFENDER_API_KEY")).build()) {

      // Social media URLs to analyze
      String[] socialMediaUrls = {
        "https://www.youtube.com/watch?v=6O0fySNw-Lw", "https://youtube.com/watch?v=ABC123",
      };

      for (String url : socialMediaUrls) {
        try {
          System.out.println("Analyzing social media content: " + url);

          // Upload the social media URL for analysis
          UploadResponse uploadResponse = client.uploadSocialMedia(url);
          System.out.println("Upload successful. Request ID: " + uploadResponse.getRequestId());

          // Wait for analysis results with custom polling settings
          DetectionResult result =
              client.getResult(
                  uploadResponse.getRequestId(),
                  Duration.ofSeconds(3), // Check every 3 seconds
                  20 // Maximum 20 attempts (1 minute total)
                  );

          // Display results
          System.out.println("Detection Status: " + result.getStatus());

          if (result.getModels() != null && !result.getModels().isEmpty()) {
            System.out.println("Individual Model Results:");
            for (DetectionResult.ModelResult model : result.getModels()) {
              System.out.printf("- %s: %s%n", model.getName(), model.getStatus());
            }
          }

          // Interpret results
          switch (result.getStatus()) {
            case "MANIPULATED":
              System.out.println(
                  "⚠️  WARNING: This content appears to be artificially generated or manipulated!");
              break;
            case "AUTHENTIC":
              System.out.println("✅ This content appears to be authentic.");
              break;
            case "PROCESSING":
            case "ANALYZING":
              System.out.println("🔄 Analysis is still in progress...");
              break;
            default:
              System.out.println("❓ Analysis inconclusive.");
          }

          System.out.println("---");

        } catch (RealityDefenderException e) {
          System.err.println("Error analyzing " + url + ": " + e.getMessage());
          if (e.getCode() != null) {
            System.err.println("Error code: " + e.getCode());
          }
        } catch (JsonProcessingException e) {
          System.err.println("Error processing response for " + url + ": " + e.getMessage());
        }
      }

    } catch (Exception e) {
      System.err.println("Error initializing Reality Defender client: " + e.getMessage());
    }
  }
}

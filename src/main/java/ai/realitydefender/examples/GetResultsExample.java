package ai.realitydefender.examples;

import ai.realitydefender.RealityDefender;
import ai.realitydefender.exceptions.RealityDefenderException;
import ai.realitydefender.models.DetectionResult;
import ai.realitydefender.models.DetectionResultList;
import ai.realitydefender.models.GetResultsOptions;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * Example demonstrating how to retrieve paginated detection results.
 */
public class GetResultsExample {

  public static void main(String[] args) {
    // Get API key from environment variable
    String apiKey = System.getenv("REALITY_DEFENDER_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Please set REALITY_DEFENDER_API_KEY environment variable");
      return;
    }

    // Create client
    RealityDefender client = RealityDefender.builder()
        .apiKey(apiKey)
        .build();

    try {
      System.out.println("Reality Defender SDK - Get Results Example");
      System.out.println("==========================================\n");

      // Example 1: Get first page of results with size=5
      System.out.println("1. Fetching first page of results (size=5):");
      try {
        GetResultsOptions options = GetResultsOptions.builder()
            .pageNumber(0)
            .size(5)
            .build();
        DetectionResultList results = client.getResults(options);
        printResults(results);
      } catch (RealityDefenderException e) {
        System.err.println("Error fetching results: " + e.getMessage());
      }

      // Example 2: Get results with pagination  
      System.out.println("\n2. Fetching results with pagination (page 0, size 5):");
      GetResultsOptions options = GetResultsOptions.builder()
          .pageNumber(0)
          .size(5)
          .build();
      
      try {
        DetectionResultList results = client.getResults(options);
        printResults(results);
      } catch (RealityDefenderException e) {
        System.err.println("Error fetching paginated results: " + e.getMessage());
      }

      // Example 3: Get results with date filter
      System.out.println("\n3. Fetching results with date filter (2024-2025):");
      GetResultsOptions dateOptions = GetResultsOptions.builder()
          .pageNumber(0)
          .size(5)
          .startDate(LocalDate.of(2024, 1, 1))
          .endDate(LocalDate.of(2025, 12, 31))
          .build();
      
      try {
        DetectionResultList results = client.getResults(dateOptions);
        printResults(results);
      } catch (RealityDefenderException e) {
        System.err.println("Error fetching date-filtered results: " + e.getMessage());
      }

      // Example 4: Get results with name filter
      System.out.println("\n4. Fetching results with name filter:");
      GetResultsOptions nameOptions = GetResultsOptions.builder()
          .pageNumber(0)
          .size(5)
          .name("test")
          .build();
      
      try {
        DetectionResultList results = client.getResults(nameOptions);
        printResults(results);
      } catch (RealityDefenderException e) {
        System.err.println("Error fetching name-filtered results: " + e.getMessage());
      }

      // Example 5: Get results with polling for completion
      System.out.println("\n5. Fetching results with polling for completion:");
      GetResultsOptions pollingOptions = GetResultsOptions.builder()
          .pageNumber(0)
          .size(5)
          .maxAttempts(30)
          .pollingInterval(Duration.ofSeconds(2))
          .build();
      
      try {
        DetectionResultList results = client.getResults(pollingOptions);
        printResults(results);
      } catch (RealityDefenderException e) {
        System.err.println("Error fetching results with polling: " + e.getMessage());
      }

      // Example 6: Asynchronous results fetching
      System.out.println("\n6. Fetching results asynchronously:");
      CompletableFuture<DetectionResultList> futureResults = client.getResultsAsync(
          GetResultsOptions.builder()
              .pageNumber(0)
              .size(5)
              .build());
      
      futureResults.thenAccept(results -> {
        System.out.println("Async results received:");
        printResults(results);
      }).exceptionally(throwable -> {
        System.err.println("Async error: " + throwable.getMessage());
        return null;
      });

      // Wait for async operation to complete
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

    } finally {
      client.close();
    }

    System.out.println("\nExample complete!");
  }

  private static void printResults(DetectionResultList results) {
    System.out.println("Total Results: " + results.getTotalItems());
    System.out.println("Current Page: " + (results.getCurrentPage() + 1) + " of " + results.getTotalPages());
    System.out.println("Results on this page: " + results.getCurrentPageItemsCount());

    if (results.getItems() != null && !results.getItems().isEmpty()) {
      System.out.println("\nDetection Results:");
      for (int i = 0; i < results.getItems().size(); i++) {
        DetectionResult result = results.getItems().get(i);
        System.out.println("\n" + (i + 1) + ". Request ID: " + result.getRequestId());
        System.out.println("   Status: " + result.getOverallStatus());
        
        Double score = result.getScore();
        if (score != null) {
          System.out.printf("   Score: %.4f (%.1f%%)\n", score, score * 100.0);
        } else {
          System.out.println("   Score: None");
        }

        if (result.getModels() != null && !result.getModels().isEmpty()) {
          System.out.println("   Models:");
          for (DetectionResult.ModelResult model : result.getModels()) {
            System.out.print("     - " + model.getName() + ": " + model.getStatus());
            if (model.getPredictionNumber() != null) {
              System.out.printf(" (Score: %.4f)", model.getPredictionNumber());
            } else {
              System.out.print(" (Score: None)");
            }
            System.out.println();
          }
        }
      }
    } else {
      System.out.println("\nNo results found.");
    }
  }
}
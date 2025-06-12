# Reality Defender SDK for Java

[![CI](https://github.com/Reality-Defender/realitydefender-sdk-java/actions/workflows/ci.yml/badge.svg)](https://github.com/Reality-Defender/realitydefender-sdk-java/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/Reality-Defender/realitydefender-sdk-java/branch/main/graph/badge.svg)](https://codecov.io/gh/Reality-Defender/realitydefender-sdk-java)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.realitydefender/realitydefender-sdk/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.realitydefender/realitydefender-sdk)

A Java SDK for the Reality Defender API to detect deepfakes and manipulated media.

## Features

- 🚀 **Async & Sync APIs** - Choose between synchronous and asynchronous operations
- 🛡️ **Type Safety** - Full type safety with comprehensive model classes
- 📊 **Built-in Polling** - Automatic result polling with configurable intervals
- 🔄 **Retry Logic** - Robust error handling and retry mechanisms
- 📝 **Comprehensive Logging** - SLF4J integration for flexible logging
- 🧪 **100% Test Coverage** - Thoroughly tested with unit and integration tests

## Requirements

- Java 11 or higher
- Maven 3.6+ or Gradle 6+

## Installation

### Maven

```xml
<dependency>
    <groupId>com.realitydefender</groupId>
    <artifactId>realitydefender-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.realitydefender:realitydefender-sdk:1.0.0'
```

## Quick Start

```java
import com.realitydefender.RealityDefender;
import com.realitydefender.models.DetectionResult;
import java.io.File;

// Initialize the client
RealityDefender client = RealityDefender.builder()
    .apiKey("your-api-key-here")
    .build();

try {
    // Analyze a file (one-step operation)
    File imageFile = new File("path/to/your/image.jpg");
    DetectionResult result = client.detectFile(imageFile);
    
    // Check the results
    System.out.println("Detection Status: " + result.getStatus());
    System.out.println("Confidence Score: " + result.getScore());
    
    // Examine individual model results
    result.getModels().forEach(model -> {
        System.out.printf("Model %s: %s (%.3f)%n", 
            model.getName(), model.getStatus(), model.getScore());
    });
    
} finally {
    // Always close the client to free resources
    client.close();
}
```

## Usage Examples

### Synchronous Detection

```java
RealityDefender client = RealityDefender.builder()
    .apiKey("your-api-key")
    .build();

try {
    // Upload file
    File file = new File("image.jpg");
    UploadResponse upload = client.upload(file);
    System.out.println("Uploaded with request ID: " + upload.getRequestId());
    
    // Get results (polls until complete)
    DetectionResult result = client.getResult(upload.getRequestId());
    System.out.println("Detection complete: " + result.getStatus());
    
} finally {
    client.close();
}
```

### Asynchronous Detection

```java
RealityDefender client = RealityDefender.builder()
    .apiKey("your-api-key")
    .build();

try {
    File file = new File("image.jpg");
    
    // Async upload and detection
    CompletableFuture<DetectionResult> future = client.detectFileAsync(file);
    
    // Do other work while detection runs...
    
    // Get the result when ready
    DetectionResult result = future.get();
    System.out.println("Async detection complete: " + result.getStatus());
    
} finally {
    client.close();
}
```

### Polling with Callbacks

```java
RealityDefender client = RealityDefender.builder()
    .apiKey("your-api-key")
    .build();

try {
    File file = new File("image.jpg");
    UploadResponse upload = client.upload(file);
    
    // Poll with custom callbacks
    client.pollForResults(
        upload.getRequestId(),
        Duration.ofSeconds(2),  // Poll every 2 seconds
        Duration.ofMinutes(5),  // 5 minute timeout
        result -> {
            // Success callback
            System.out.println("Detection complete: " + result.getStatus());
            System.out.println("Score: " + result.getScore());
        },
        error -> {
            // Error callback
            System.err.println("Detection failed: " + error.getMessage());
        }
    );
    
} finally {
    client.close();
}
```

### Custom Configuration

```java
RealityDefender client = RealityDefender.builder()
    .apiKey("your-api-key")
    .baseUrl("https://custom-api.realitydefender.com")
    .timeout(Duration.ofMinutes(2))
    .build();
```

## Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `apiKey` | String | Required | Your Reality Defender API key |
| `baseUrl` | String | `https://api.realitydefender.com` | API base URL |
| `timeout` | Duration | 30 seconds | Request timeout |

## Error Handling

The SDK throws `RealityDefenderException` for all API-related errors:

```java
try {
    DetectionResult result = client.detectFile(file);
} catch (RealityDefenderException e) {
    System.err.println("Error: " + e.getMessage());
    System.err.println("Code: " + e.getCode());
    System.err.println("Status Code: " + e.getStatusCode());
}
```

Common error codes:
- `UNAUTHORIZED` - Invalid API key
- `INVALID_FILE` - Unsupported file format
- `UPLOAD_FAILED` - File upload failed
- `TIMEOUT` - Request timed out
- `SERVER_ERROR` - Internal server error

## Supported File Types

- **Images**: JPEG, PNG, GIF, BMP, TIFF
- **Videos**: MP4, AVI, MOV, WMV, FLV

## API Reference

### DetectionResult

```java
public class DetectionResult {
    public String getStatus();        // Overall status: "ARTIFICIAL", "AUTHENTIC", "PROCESSING"
    public Double getScore();         // Confidence score 0.0-1.0 (null if processing)
    public List<ModelResult> getModels(); // Individual model results
}
```

### ModelResult

```java
public class ModelResult {
    public String getName();          // Model name
    public String getStatus();        // Model-specific status
    public Double getScore();         // Model-specific score
}
```

### UploadResponse

```java
public class UploadResponse {
    public String getRequestId();     // ID for tracking analysis
    public String getMediaId();       // Uploaded media identifier
}
```

## Development

### Building

```bash
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Running Integration Tests

```bash
mvn verify
```

### Code Coverage

```bash
mvn jacoco:report
open target/site/jacoco/index.html
```

### Code Formatting

```bash
mvn spotless:apply
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass and coverage is maintained
6. Submit a pull request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

- 📧 Email: support@realitydefender.com
- 📖 Documentation: [API Docs](https://docs.realitydefender.com)
- 🐛 Issues: [GitHub Issues](https://github.com/Reality-Defender/realitydefender-sdk-java/issues)
```
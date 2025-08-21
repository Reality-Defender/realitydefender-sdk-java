# Reality Defender SDK for Java

[![codecov](https://codecov.io/github/Reality-Defender/realitydefender-sdk-java/graph/badge.svg?token=ARMPPU3HQM)](https://codecov.io/github/Reality-Defender/realitydefender-sdk-java)

A Java SDK for the Reality Defender API to detect deepfakes and manipulated media.

## Features

- 🚀 **Async & Sync APIs** - Choose between synchronous and asynchronous operations
- 🛡️ **Type Safety** - Full type safety with comprehensive model classes
- 📊 **Built-in Polling** - Automatic result polling with configurable intervals
- 🔄 **Retry Logic** - Robust error handling and retry mechanisms
- 📝 **Comprehensive Logging** - SLF4J integration for flexible logging

## Requirements

- Java 11 or higher
- Maven 3.6+ or Gradle 6+

## Installation

### Maven

```xml
<dependency>
    <groupId>ai.realitydefender</groupId>
    <artifactId>realitydefender-sdk</artifactId>
    <version>[0.1.0,0.2)</version>
</dependency>
```

### Gradle

```gradle
implementation 'ai.realitydefender:realitydefender-sdk:0.1.0'
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


## Supported file types and size limits

There is a size limit for each of the supported file types.

| File Type | Extensions                                 | Size Limit (bytes) | Size Limit (MB) |
|-----------|--------------------------------------------|--------------------|-----------------|
| Video     | .mp4, .mov                                 | 262,144,000        | 250 MB          |
| Image     | .jpg, .png, .jpeg, .gif, .webp             | 52,428,800         | 50 MB           |
| Audio     | .flac, .wav, .mp3, .m4a, .aac, .alac, .ogg | 20,971,520         | 20 MB           |
| Text      | .txt                                       | 5,242,880          | 5 MB            |


## Supported social media platforms

The Reality Defender API supports analysis of media from the following social media platforms:
* Facebook
* Instagram
* Twitter
* YouTube
* TikTok

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

### Running Examples

Set your API key:
```bash
export REALITY_DEFENDER_API_KEY="your_api_key_here"
```

Run specific examples:
```bash
# Simple file detection example
mvn exec:java@simple-example

# Get results example (pagination, filtering, async)
mvn exec:java@get-results-example

# Social media detection example
mvn exec:java@social-media-example
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

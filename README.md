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
    <version>0.1.0</version>
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

## Supported File Types

- **Images**: JPEG, PNG, GIF, BMP, TIFF
- **Videos**: MP4, AVI, MOV, WMV, FLV

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
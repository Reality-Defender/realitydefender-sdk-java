package ai.realitydefender.core;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RealityDefenderConfigTest {

  @Test
  void testConfigCreation() {
    String apiKey = "test-api-key";
    String baseUrl = "https://api.realitydefender.com";
    Duration timeout = Duration.ofSeconds(30);

    RealityDefenderConfig config = new RealityDefenderConfig(apiKey, baseUrl, timeout);

    assertEquals(apiKey, config.getApiKey());
    assertEquals(baseUrl, config.getBaseUrl());
    assertEquals(timeout, config.getTimeout());
  }

  @Test
  void testConfigWithNullApiKey() {
    assertThrows(
        NullPointerException.class,
        () -> {
          new RealityDefenderConfig(
              null, "https://api.realitydefender.com", Duration.ofSeconds(30));
        });
  }

  @Test
  void testConfigWithNullBaseUrl() {
    assertThrows(
        NullPointerException.class,
        () -> {
          new RealityDefenderConfig("api-key", null, Duration.ofSeconds(30));
        });
  }

  @Test
  void testConfigWithNullTimeout() {
    assertThrows(
        NullPointerException.class,
        () -> {
          new RealityDefenderConfig("api-key", "https://api.realitydefender.com", null);
        });
  }

  @Test
  void testConfigEquality() {
    RealityDefenderConfig config1 =
        new RealityDefenderConfig(
            "api-key", "https://api.realitydefender.com", Duration.ofSeconds(30));
    RealityDefenderConfig config2 =
        new RealityDefenderConfig(
            "api-key", "https://api.realitydefender.com", Duration.ofSeconds(30));

    assertEquals(config1, config2);
    assertEquals(config1.hashCode(), config2.hashCode());
  }

  @Test
  void testConfigToString() {
    RealityDefenderConfig config =
        new RealityDefenderConfig(
            "api-key", "https://api.realitydefender.com", Duration.ofSeconds(30));

    String toString = config.toString();
    assertTrue(toString.contains("https://api.realitydefender.com"));
    assertTrue(toString.contains("PT30S"));
    assertFalse(toString.contains("api-key")); // API key should not be in toString for security
  }
}

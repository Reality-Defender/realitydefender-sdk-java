package ai.realitydefender.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UrlTest {

  @Test
  void testValidHttpsUrls() {
    assertTrue(Url.isValidHttpUrl("https://example.com"));
    assertTrue(Url.isValidHttpUrl("https://www.example.com"));
    assertTrue(Url.isValidHttpUrl("https://api.example.com/v1/endpoint"));
    assertTrue(Url.isValidHttpUrl("https://example.com:8080"));
    assertTrue(Url.isValidHttpUrl("https://example.com/path?query=value"));
    assertTrue(Url.isValidHttpUrl("https://example.com/path#fragment"));
    assertTrue(Url.isValidHttpUrl("https://subdomain.example.com/path?query=value&other=123"));
    assertTrue(Url.isValidHttpUrl("https://example.com:443/secure"));
    assertTrue(Url.isValidHttpUrl("https://192.168.1.1"));
    assertTrue(Url.isValidHttpUrl("https://localhost:3000"));
  }

  @Test
  void testValidHttpUrls() {
    assertTrue(Url.isValidHttpUrl("http://example.com"));
    assertTrue(Url.isValidHttpUrl("http://www.example.com"));
    assertTrue(Url.isValidHttpUrl("http://api.example.com/v1/endpoint"));
    assertTrue(Url.isValidHttpUrl("http://example.com:8080"));
    assertTrue(Url.isValidHttpUrl("http://example.com/path?query=value"));
    assertTrue(Url.isValidHttpUrl("http://example.com/path#fragment"));
    assertTrue(Url.isValidHttpUrl("http://subdomain.example.com/path?query=value&other=123"));
    assertTrue(Url.isValidHttpUrl("http://example.com:80/default"));
    assertTrue(Url.isValidHttpUrl("http://192.168.1.1:8080"));
    assertTrue(Url.isValidHttpUrl("http://localhost"));
  }

  @Test
  void testCaseInsensitiveSchemes() {
    assertTrue(Url.isValidHttpUrl("HTTP://example.com"));
    assertTrue(Url.isValidHttpUrl("HTTPS://example.com"));
    assertTrue(Url.isValidHttpUrl("Http://example.com"));
    assertTrue(Url.isValidHttpUrl("Https://example.com"));
    assertTrue(Url.isValidHttpUrl("hTtPs://example.com"));
  }

  @Test
  void testInvalidSchemes() {
    assertFalse(Url.isValidHttpUrl("ftp://example.com"));
    assertFalse(Url.isValidHttpUrl("file://example.com"));
    assertFalse(Url.isValidHttpUrl("mailto:user@example.com"));
    assertFalse(Url.isValidHttpUrl("ssh://example.com"));
    assertFalse(Url.isValidHttpUrl("ws://example.com"));
    assertFalse(Url.isValidHttpUrl("wss://example.com"));
    assertFalse(Url.isValidHttpUrl("ldap://example.com"));
  }

  @Test
  void testNullAndEmptyInputs() {
    assertFalse(Url.isValidHttpUrl(null));
    assertFalse(Url.isValidHttpUrl(""));
    assertFalse(Url.isValidHttpUrl("   "));
    assertFalse(Url.isValidHttpUrl("\t"));
    assertFalse(Url.isValidHttpUrl("\n"));
    assertFalse(Url.isValidHttpUrl("  \t  \n  "));
  }

  @Test
  void testMalformedUrls() {
    assertFalse(Url.isValidHttpUrl("not-a-url"));
    assertFalse(Url.isValidHttpUrl("example.com"));
    assertFalse(Url.isValidHttpUrl("www.example.com"));
    assertFalse(Url.isValidHttpUrl("http://"));
    assertFalse(Url.isValidHttpUrl("https://"));
    assertFalse(Url.isValidHttpUrl("http:///path"));
    assertFalse(Url.isValidHttpUrl("https:///path"));
    assertFalse(Url.isValidHttpUrl("http:/example.com"));
    assertFalse(Url.isValidHttpUrl("https:/example.com"));
  }

  @Test
  void testInvalidCharacters() {
    assertFalse(Url.isValidHttpUrl("http://exam ple.com"));
    assertFalse(Url.isValidHttpUrl("https://example .com"));
    assertFalse(Url.isValidHttpUrl("http://[invalid"));
    assertFalse(Url.isValidHttpUrl("https://example.com:abc"));
  }

  @Test
  void testEdgeCases() {
    // URLs with special characters that are valid
    assertTrue(Url.isValidHttpUrl("https://user:pass@example.com"));
    assertTrue(Url.isValidHttpUrl("https://example-site.com"));
    assertTrue(Url.isValidHttpUrl("https://example_site.com"));

    // Very long but valid URL
    String longPath = "a".repeat(100);
    assertTrue(Url.isValidHttpUrl("https://example.com/" + longPath));

    // URL with many query parameters
    assertTrue(Url.isValidHttpUrl("https://example.com?a=1&b=2&c=3&d=4&e=5"));
  }
}

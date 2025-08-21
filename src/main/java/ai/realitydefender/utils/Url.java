package ai.realitydefender.utils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class Url {
  /**
   * Validates if a string is a valid URL with HTTP or HTTPS scheme.
   *
   * @param url the string to validate
   * @return true if the string is a valid HTTP or HTTPS URL, false otherwise
   */
  public static boolean isValidHttpUrl(String url) {
    if (url == null || url.trim().isEmpty()) {
      return false;
    }

    try {
      java.net.URL parsedUrl = new java.net.URL(url);
      parsedUrl.toURI();

      String scheme = parsedUrl.getProtocol().toLowerCase();
      return ("http".equals(scheme) || "https".equals(scheme))
          && (parsedUrl.getHost() != null && !parsedUrl.getHost().isBlank());
    } catch (MalformedURLException | URISyntaxException e) {
      return false;
    }
  }
}

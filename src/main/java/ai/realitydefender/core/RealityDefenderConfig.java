package ai.realitydefender.core;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration class for Reality Defender SDK.
 */
public class RealityDefenderConfig {

    private final String apiKey;
    private final String baseUrl;
    private final Duration timeout;

    public RealityDefenderConfig(String apiKey, String baseUrl, Duration timeout) {
        this.apiKey = Objects.requireNonNull(apiKey, "API key cannot be null");
        this.baseUrl = Objects.requireNonNull(baseUrl, "Base URL cannot be null");
        this.timeout = Objects.requireNonNull(timeout, "Timeout cannot be null");
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RealityDefenderConfig that = (RealityDefenderConfig) o;
        return Objects.equals(apiKey, that.apiKey) &&
                Objects.equals(baseUrl, that.baseUrl) &&
                Objects.equals(timeout, that.timeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiKey, baseUrl, timeout);
    }

    @Override
    public String toString() {
        return "RealityDefenderConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", timeout=" + timeout +
                '}';
    }
}
package ai.realitydefender.models;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

/** Options for getting paginated results with filtering and polling. */
public class GetResultsOptions {
  private final Integer pageNumber;
  private final Integer size;
  private final String name;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final Integer maxAttempts;
  private final Duration pollingInterval;

  private GetResultsOptions(Builder builder) {
    this.pageNumber = builder.pageNumber;
    this.size = builder.size;
    this.name = builder.name;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.maxAttempts = builder.maxAttempts;
    this.pollingInterval = builder.pollingInterval;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Integer getPageNumber() {
    return pageNumber;
  }

  public Integer getSize() {
    return size;
  }

  public String getName() {
    return name;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public Integer getMaxAttempts() {
    return maxAttempts;
  }

  public Duration getPollingInterval() {
    return pollingInterval;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GetResultsOptions that = (GetResultsOptions) o;
    return Objects.equals(pageNumber, that.pageNumber)
        && Objects.equals(size, that.size)
        && Objects.equals(name, that.name)
        && Objects.equals(startDate, that.startDate)
        && Objects.equals(endDate, that.endDate)
        && Objects.equals(maxAttempts, that.maxAttempts)
        && Objects.equals(pollingInterval, that.pollingInterval);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pageNumber, size, name, startDate, endDate, maxAttempts, pollingInterval);
  }

  @Override
  public String toString() {
    return "GetResultsOptions{"
        + "pageNumber="
        + pageNumber
        + ", size="
        + size
        + ", name='"
        + name
        + '\''
        + ", startDate="
        + startDate
        + ", endDate="
        + endDate
        + ", maxAttempts="
        + maxAttempts
        + ", pollingInterval="
        + pollingInterval
        + '}';
  }

  public static class Builder {
    private Integer pageNumber;
    private Integer size;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxAttempts;
    private Duration pollingInterval;

    public Builder pageNumber(int pageNumber) {
      this.pageNumber = pageNumber;
      return this;
    }

    public Builder size(int size) {
      this.size = size;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder startDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    public Builder endDate(LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    public Builder maxAttempts(int maxAttempts) {
      this.maxAttempts = maxAttempts;
      return this;
    }

    public Builder pollingInterval(Duration pollingInterval) {
      this.pollingInterval = pollingInterval;
      return this;
    }

    public GetResultsOptions build() {
      return new GetResultsOptions(this);
    }
  }
}

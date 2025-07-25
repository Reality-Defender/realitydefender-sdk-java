package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/** Represents a paginated list of detection results. */
public class DetectionResultList {
  private final int totalItems;
  private final int totalPages;
  private final int currentPage;
  private final int currentPageItemsCount;
  private final List<DetectionResult> items;

  @JsonCreator
  public DetectionResultList(
      @JsonProperty("totalItems") int totalItems,
      @JsonProperty("totalPages") int totalPages,
      @JsonProperty("currentPage") int currentPage,
      @JsonProperty("currentPageItemsCount") int currentPageItemsCount,
      @JsonProperty("mediaList") List<DetectionResult> items) {
    this.totalItems = totalItems;
    this.totalPages = totalPages;
    this.currentPage = currentPage;
    this.currentPageItemsCount = currentPageItemsCount;
    this.items = items;
  }

  public int getTotalItems() {
    return totalItems;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public int getCurrentPage() {
    return currentPage;
  }

  public int getCurrentPageItemsCount() {
    return currentPageItemsCount;
  }

  public List<DetectionResult> getItems() {
    return items;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DetectionResultList that = (DetectionResultList) o;
    return totalItems == that.totalItems
        && totalPages == that.totalPages
        && currentPage == that.currentPage
        && currentPageItemsCount == that.currentPageItemsCount
        && Objects.equals(items, that.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalItems, totalPages, currentPage, currentPageItemsCount, items);
  }

  @Override
  public String toString() {
    return "DetectionResultList{"
        + "totalItems="
        + totalItems
        + ", totalPages="
        + totalPages
        + ", currentPage="
        + currentPage
        + ", currentPageItemsCount="
        + currentPageItemsCount
        + ", items="
        + (items != null ? items.size() : 0)
        + " items"
        + '}';
  }

  public DetectionResultList summarize() {
    return new DetectionResultList(
        this.totalItems,
        this.totalPages,
        this.currentPage,
        this.currentPageItemsCount,
        this.items.stream()
            .map(DetectionResult::summarize)
            .collect(java.util.stream.Collectors.toList()));
  }
}

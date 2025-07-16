package ai.realitydefender.models;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class GetResultsOptionsTest {

  @Test
  void testBuilderWithAllOptions() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 12, 31);
    Duration pollingInterval = Duration.ofSeconds(5);

    GetResultsOptions options =
        GetResultsOptions.builder()
            .pageNumber(2)
            .size(20)
            .name("test-filter")
            .startDate(startDate)
            .endDate(endDate)
            .maxAttempts(10)
            .pollingInterval(pollingInterval)
            .build();

    assertEquals(2, options.getPageNumber());
    assertEquals(20, options.getSize());
    assertEquals("test-filter", options.getName());
    assertEquals(startDate, options.getStartDate());
    assertEquals(endDate, options.getEndDate());
    assertEquals(10, options.getMaxAttempts());
    assertEquals(pollingInterval, options.getPollingInterval());
  }

  @Test
  void testBuilderWithMinimalOptions() {
    GetResultsOptions options = GetResultsOptions.builder().pageNumber(0).build();

    assertEquals(0, options.getPageNumber());
    assertNull(options.getSize());
    assertNull(options.getName());
    assertNull(options.getStartDate());
    assertNull(options.getEndDate());
    assertNull(options.getMaxAttempts());
    assertNull(options.getPollingInterval());
  }

  @Test
  void testBuilderWithEmptyBuild() {
    GetResultsOptions options = GetResultsOptions.builder().build();

    assertNull(options.getPageNumber());
    assertNull(options.getSize());
    assertNull(options.getName());
    assertNull(options.getStartDate());
    assertNull(options.getEndDate());
    assertNull(options.getMaxAttempts());
    assertNull(options.getPollingInterval());
  }

  @Test
  void testEqualsAndHashCode() {
    GetResultsOptions options1 =
        GetResultsOptions.builder().pageNumber(1).size(10).name("test").build();

    GetResultsOptions options2 =
        GetResultsOptions.builder().pageNumber(1).size(10).name("test").build();

    GetResultsOptions options3 =
        GetResultsOptions.builder().pageNumber(2).size(10).name("test").build();

    assertEquals(options1, options2);
    assertNotEquals(options1, options3);

    assertEquals(options1.hashCode(), options2.hashCode());
    assertNotEquals(options1.hashCode(), options3.hashCode());
  }

  @Test
  void testToString() {
    GetResultsOptions options =
        GetResultsOptions.builder().pageNumber(1).size(10).name("test").build();

    String toString = options.toString();
    assertTrue(toString.contains("pageNumber=1"));
    assertTrue(toString.contains("size=10"));
    assertTrue(toString.contains("name='test'"));
  }
}

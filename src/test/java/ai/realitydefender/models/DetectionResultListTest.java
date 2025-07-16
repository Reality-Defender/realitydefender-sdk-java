package ai.realitydefender.models;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DetectionResultListTest {

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Test
  void testConstructorAndGetters() {
    List<DetectionResult> items = new ArrayList<>();
    DetectionResultList resultList = new DetectionResultList(100, 10, 0, 10, items);

    assertEquals(100, resultList.getTotalItems());
    assertEquals(10, resultList.getTotalPages());
    assertEquals(0, resultList.getCurrentPage());
    assertEquals(10, resultList.getCurrentPageItemsCount());
    assertEquals(items, resultList.getItems());
  }

  @Test
  void testEqualsAndHashCode() {
    List<DetectionResult> items1 = new ArrayList<>();
    List<DetectionResult> items2 = new ArrayList<>();
    
    DetectionResultList resultList1 = new DetectionResultList(100, 10, 0, 10, items1);
    DetectionResultList resultList2 = new DetectionResultList(100, 10, 0, 10, items2);
    DetectionResultList resultList3 = new DetectionResultList(200, 20, 1, 20, items1);

    assertEquals(resultList1, resultList2);
    assertNotEquals(resultList1, resultList3);
    
    assertEquals(resultList1.hashCode(), resultList2.hashCode());
    assertNotEquals(resultList1.hashCode(), resultList3.hashCode());
  }

  @Test
  void testToString() {
    List<DetectionResult> items = new ArrayList<>();
    DetectionResultList resultList = new DetectionResultList(100, 10, 0, 10, items);

    String toString = resultList.toString();
    assertTrue(toString.contains("totalItems=100"));
    assertTrue(toString.contains("totalPages=10"));
    assertTrue(toString.contains("currentPage=0"));
    assertTrue(toString.contains("0 items"));
  }

  @Test
  void testJsonDeserialization() throws Exception {
    String json = "{\n" +
        "  \"totalItems\": 50,\n" +
        "  \"totalPages\": 5,\n" +
        "  \"currentPage\": 2,\n" +
        "  \"currentPageItemsCount\": 10,\n" +
        "  \"mediaList\": []\n" +
        "}";

    DetectionResultList resultList = objectMapper.readValue(json, DetectionResultList.class);

    assertEquals(50, resultList.getTotalItems());
    assertEquals(5, resultList.getTotalPages());
    assertEquals(2, resultList.getCurrentPage());
    assertEquals(10, resultList.getCurrentPageItemsCount());
    assertNotNull(resultList.getItems());
    assertTrue(resultList.getItems().isEmpty());
  }
}
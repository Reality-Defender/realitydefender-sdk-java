package ai.realitydefender.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RealityDefenderExceptionTest {

    @Test
    void testExceptionWithMessageAndCode() {
        RealityDefenderException exception = new RealityDefenderException("Test message", "TEST_CODE");

        assertEquals("Test message", exception.getMessage());
        assertEquals("TEST_CODE", exception.getCode());
        assertEquals(0, exception.getStatusCode());
    }

    @Test
    void testExceptionWithMessageCodeAndStatusCode() {
        RealityDefenderException exception = new RealityDefenderException("Test message", "TEST_CODE", 404);

        assertEquals("Test message", exception.getMessage());
        assertEquals("TEST_CODE", exception.getCode());
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void testExceptionWithCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        RealityDefenderException exception = new RealityDefenderException("Test message", "TEST_CODE", cause);

        assertEquals("Test message", exception.getMessage());
        assertEquals("TEST_CODE", exception.getCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionWithAllParameters() {
        RuntimeException cause = new RuntimeException("Root cause");
        RealityDefenderException exception = new RealityDefenderException("Test message", "TEST_CODE", 500, cause);

        assertEquals("Test message", exception.getMessage());
        assertEquals("TEST_CODE", exception.getCode());
        assertEquals(500, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }
}
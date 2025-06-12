package ai.realitydefender.exceptions;

/**
 * Base exception for Reality Defender SDK errors.
 */
public class RealityDefenderException extends Exception {

    private final String code;
    private final int statusCode;

    public RealityDefenderException(String message, String code) {
        this(message, code, 0);
    }

    public RealityDefenderException(String message, String code, int statusCode) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public RealityDefenderException(String message, String code, Throwable cause) {
        this(message, code, 0, cause);
    }

    public RealityDefenderException(String message, String code, int statusCode, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
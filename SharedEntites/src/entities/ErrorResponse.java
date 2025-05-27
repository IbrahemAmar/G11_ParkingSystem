package entities;

import java.io.Serializable;

/**
 * Used by the server to notify the client of unexpected errors or unsupported operations.
 */
public class ErrorResponse implements Serializable {
    private String errorMessage;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() { return errorMessage; }
}

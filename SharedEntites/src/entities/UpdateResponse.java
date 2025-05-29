package entities;

import java.io.Serializable;

/**
 * Response sent by the server after login attempt.
 */
public class UpdateResponse implements Serializable {
    private boolean success;
    private String message;

    public UpdateResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}

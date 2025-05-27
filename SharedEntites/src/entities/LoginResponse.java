package entities;

import java.io.Serializable;

/**
 * Response sent by the server after login attempt.
 */
public class LoginResponse implements Serializable {
    private boolean success;
    private String message;

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}

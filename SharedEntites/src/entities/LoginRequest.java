package entities;

import java.io.Serializable;

/**
 * Represents a login request from the client containing username and password.
 */
public class LoginRequest implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}

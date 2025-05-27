package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DBController handles database connectivity and operations
 * related to user authentication for the parking system.
 */
public class DBController {

    // Update these to match your MySQL setup if needed
	private static final String URL = "jdbc:mysql://localhost:3306/bpark?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";
	private static final String USER = "root";
	private static final String PASSWORD = "Aa123456";

    /**
     * Opens a connection to the MySQL database.
     * 
     * @return a Connection object
     * @throws SQLException if the connection fails
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Validates a user's credentials by querying the users table.
     * 
     * @param username the username to check
     * @param password the password to check
     * @return true if the user exists and credentials match, false otherwise
     */
    public boolean validateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if at least one row found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validates a user's credentials against the database and retrieves their role.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @return The user's role ("admin", "supervisor", or "subscriber") if credentials are valid;
     *         {@code null} if authentication fails or an error occurs.
     */
    public String checkUserCredentials(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role"); // ✅ returns admin/supervisor/subscriber
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}

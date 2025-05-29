package server;

import entities.ParkingSpace;
import entities.Subscriber;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DBController handles database connectivity and operations
 * related to user authentication, subscribers, and parking space management.
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

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all parking spaces that are currently marked as available.
     *
     * @return List of available parking spaces.
     */
    public List<ParkingSpace> getAvailableParkingSpaces() {
        List<ParkingSpace> spots = new ArrayList<>();
        String sql = "SELECT * FROM parking_space WHERE is_available = TRUE";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("parking_space_id");
                boolean isAvailable = rs.getBoolean("is_available");
                spots.add(new ParkingSpace(id, isAvailable));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return spots;
    }

    /**
     * Retrieves full subscriber details by their username.
     *
     * @param username the subscriber's username
     * @return a populated Subscriber object or null if not found
     */
    public Subscriber getSubscriberByUsername(String username) {
        String sql = """
            SELECT u.id, u.first_name, u.last_name, u.username, s.email, s.phone_number, s.subscriber_code
            FROM users u
            JOIN subscriber s ON s.subscriber_id = u.id
            WHERE u.username = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone_number");
                String subscriberCode = rs.getString("subscriber_code");

                return new Subscriber(id, fullName, username, email, phone, subscriberCode);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}

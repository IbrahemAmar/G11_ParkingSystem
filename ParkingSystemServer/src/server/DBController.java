package server;

import entities.ParkingHistory;
import entities.ParkingSpace;
import entities.Subscriber;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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

    /**
     * Updates the email and phone number of a subscriber in the database.
     *
     * @param subscriber The Subscriber object containing updated information.
     * @return true if the update was successful (at least one row affected), false otherwise.
     */
    public boolean updateSubscriberInfo(Subscriber subscriber) {
        String sql = "UPDATE subscriber SET email = ?, phone_number = ? WHERE subscriber_code = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subscriber.getEmail());
            stmt.setString(2, subscriber.getPhone());
            stmt.setString(3, subscriber.getSubscriberCode());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves the parking history records for a given subscriber (by code), ordered from latest to oldest.
     *
     * @param subscriberCode The code of the subscriber (e.g., SUB0005)
     * @return A list of ParkingHistory objects representing their parking history.
     */
    public List<ParkingHistory> getParkingHistoryForSubscriber(String subscriberCode) {
        List<ParkingHistory> history = new ArrayList<>();
        String sql = """
            SELECT history_id, subscriber_code, parking_space_id, entry_time, exit_time, extended, was_late
            FROM parking_history
            WHERE subscriber_code = ?
            ORDER BY entry_time DESC
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subscriberCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int historyId = rs.getInt("history_id");
                String subCode = rs.getString("subscriber_code");
                int parkingSpaceId = rs.getInt("parking_space_id");
                LocalDateTime entryTime = rs.getTimestamp("entry_time").toLocalDateTime();
                LocalDateTime exitTime = rs.getTimestamp("exit_time").toLocalDateTime();
                boolean extended = rs.getBoolean("extended");
                boolean wasLate = rs.getBoolean("was_late");

                // Constructor: ParkingHistory(int historyId, String subscriberCode, int parkingSpaceId, LocalDateTime entryTime, LocalDateTime exitTime, boolean extended, boolean wasLate)
                history.add(new ParkingHistory(historyId, subCode, parkingSpaceId, entryTime, exitTime, extended, wasLate));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }
    /**
     * Returns a random available parking spot (numeric ID only) that:
     * - Is marked as available in the `parking_space` table.
     * - Is NOT reserved in the `parking_history` table for the next 4 hours.
     *
     * @return a valid parking_space_id or -1 if none are available
     */
    public int getRandomAvailableSpotWithoutA() {
        String sql = """
            SELECT ps.parking_space_id
            FROM parking_space ps
            WHERE ps.is_available = TRUE
              AND ps.parking_space_id NOT IN (
                  SELECT parking_space_id
                  FROM parking_history
                  WHERE NOW() < exit_time
                    AND entry_time < DATE_ADD(NOW(), INTERVAL 4 HOUR)
              )
            ORDER BY RAND()
            LIMIT 1
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("parking_space_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // No suitable spot found
    }
    /**
     * Updates the availability status of a parking space.
     *
     * @param parkingSpaceId The ID of the parking space to update.
     * @param isAvailable    True to mark the space as available; false to mark as unavailable.
     */
    public void setSpotAvailability(int parkingSpaceId, boolean isAvailable) {
        String sql = "UPDATE parking_space SET is_available = ? WHERE parking_space_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, isAvailable);
            stmt.setInt(2, parkingSpaceId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Failed to update spot availability.");
            e.printStackTrace();
        }
    }
    /**
     * Inserts a system log entry for a user action, such as a vehicle deposit.
     *
     * @param action         The action performed (e.g., "Deposit").
     * @param target         The target of the action (e.g., "Spot 6").
     * @param subscriberCode The subscriber's code who performed the action.
     */
    public void insertSystemLog(String action, String target, String subscriberCode) {
        String sql = """
            INSERT INTO system_log (action, target, by_user, log_time)
            SELECT ?, ?, u.id, NOW()
            FROM users u
            JOIN subscriber s ON s.subscriber_id = u.id
            WHERE s.subscriber_code = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, action);
            stmt.setString(2, target);
            stmt.setString(3, subscriberCode);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Failed to insert system log.");
            e.printStackTrace();
        }
    }
    /**
     * Inserts a new parking deposit into the parking_history table.
     *
     * @param history A fully populated ParkingHistory object representing the current deposit.
     */
    public void insertParkingHistory(ParkingHistory history) {
        String sql = """
            INSERT INTO parking_history (
                subscriber_code,
                parking_space_id,
                entry_time,
                exit_time,
                extended,
                was_late
            ) VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, history.getSubscriberCode());
            stmt.setInt(2, history.getParkingSpaceId());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(history.getEntryTime()));
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(history.getExitTime()));
            stmt.setBoolean(5, history.isExtended());
            stmt.setBoolean(6, history.isWasLate());

            stmt.executeUpdate();
            System.out.println("✅ Parking deposit saved for " + history.getSubscriberCode());

        } catch (SQLException e) {
            System.err.println("❌ Failed to insert parking deposit");
            e.printStackTrace();
        }
    }
    /**
     * Checks whether the subscriber currently has an active parking reservation.
     *
     * A reservation is considered active if:
     * - entry_time <= NOW()
     * - AND exit_time > NOW()
     *
     * @param subscriberCode The subscriber's code (e.g., "SUB0005")
     * @return true if such a reservation exists, false otherwise
     */
    public boolean hasActiveReservation(String subscriberCode) {
        String sql = """
            SELECT 1 FROM parking_history
            WHERE subscriber_code = ?
              AND entry_time <= NOW()
              AND exit_time > NOW()
            LIMIT 1
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subscriberCode);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // ✅ true = active reservation exists

        } catch (SQLException e) {
            System.err.println("❌ Error checking active reservation:");
            e.printStackTrace();
            return false; // fail safe: assume no reservation
        }
    }

    /**
     * Retrieves the active parking history for the given subscriber.
     * Active means exit_time > NOW().
     *
     * @param subscriberCode the subscriber's unique code
     * @return the active ParkingHistory if exists, otherwise null
     */
    public ParkingHistory getActiveParkingBySubscriber(String subscriberCode) {
        String query = "SELECT * FROM parking_history " +
                       "WHERE subscriber_code = ? AND exit_time > NOW() " +
                       "ORDER BY exit_time DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, subscriberCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int historyId = rs.getInt("history_id");
                String subCode = rs.getString("subscriber_code");
                int parkingSpaceId = rs.getInt("parking_space_id");
                LocalDateTime entryTime = rs.getTimestamp("entry_time").toLocalDateTime();
                LocalDateTime exitTime = rs.getTimestamp("exit_time").toLocalDateTime();
                boolean extended = rs.getBoolean("extended");
                boolean wasLate = rs.getBoolean("was_late");

                return new ParkingHistory(historyId, subCode, parkingSpaceId, entryTime, exitTime, extended, wasLate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks for conflicting reservations in the reservation table during the proposed extension period.
     * Assumes each reservation lasts 4 hours from reservation_date.
     *
     * @return true if a conflict exists, false otherwise
     */
    public boolean isReservationConflict(int parkingSpaceId, LocalDateTime currentExit, LocalDateTime newExit) {
        String query = "SELECT COUNT(*) FROM reservation " +
                       "WHERE parking_space_id = ? " +
                       "AND status = 'active' " + // Only active reservations matter!
                       "AND (reservation_date BETWEEN ? AND ? " +
                       "OR DATE_ADD(reservation_date, INTERVAL 4 HOUR) BETWEEN ? AND ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, parkingSpaceId);
            stmt.setTimestamp(2, Timestamp.valueOf(currentExit));
            stmt.setTimestamp(3, Timestamp.valueOf(newExit));
            stmt.setTimestamp(4, Timestamp.valueOf(currentExit));
            stmt.setTimestamp(5, Timestamp.valueOf(newExit));

            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Conflict exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    
    /**
     * Updates the exit time of the active parking for the subscriber.
     *
     * @param subscriberCode the subscriber's unique code
     * @param newExitTime    the new exit time to set
     * @return number of rows updated (should be 1 if successful)
     */
    public int updateExitTime(String subscriberCode, LocalDateTime newExitTime) {
        String query = "UPDATE parking_history SET exit_time = ? " +
                       "WHERE subscriber_code = ? AND exit_time > NOW() " +
                       "ORDER BY exit_time DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, Timestamp.valueOf(newExitTime));
            stmt.setString(2, subscriberCode);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }



}

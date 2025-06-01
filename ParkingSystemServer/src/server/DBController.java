package server;

import entities.ParkingHistory;
import entities.ParkingSpace;
import entities.Subscriber;

import java.sql.*;
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

    // --------- Universal Mappers ---------

    /**
     * Maps a ResultSet row to a ParkingHistory object.
     */
    public ParkingHistory mapParkingHistory(ResultSet rs) throws SQLException {
        return new ParkingHistory(
            rs.getInt("history_id"),
            rs.getString("subscriber_code"),
            rs.getInt("parking_space_id"),
            rs.getTimestamp("entry_time").toLocalDateTime(),
            rs.getTimestamp("exit_time").toLocalDateTime(),
            rs.getBoolean("extended"),
            rs.getBoolean("was_late"),
            rs.getBoolean("picked_up")
        );
    }

    /**
     * Maps a ResultSet row to a ParkingSpace object.
     */
    public ParkingSpace mapParkingSpace(ResultSet rs) throws SQLException {
        return new ParkingSpace(
            rs.getInt("parking_space_id"),
            rs.getBoolean("is_available")
        );
    }

    /**
     * Maps a ResultSet row to a Subscriber object.
     */
    public Subscriber mapSubscriber(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String phone = rs.getString("phone_number");
        String subscriberCode = rs.getString("subscriber_code");
        return new Subscriber(id, fullName, username, email, phone, subscriberCode);
    }

    // -------------------------------------

    public boolean validateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String checkUserCredentials(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ParkingSpace> getAvailableParkingSpaces() {
        List<ParkingSpace> spots = new ArrayList<>();
        String sql = "SELECT * FROM parking_space WHERE is_available = TRUE";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                spots.add(mapParkingSpace(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spots;
    }

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
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapSubscriber(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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

    public List<ParkingHistory> getParkingHistoryForSubscriber(String subscriberCode) {
        List<ParkingHistory> history = new ArrayList<>();
        String sql = """
            SELECT history_id, subscriber_code, parking_space_id, entry_time, exit_time, extended, was_late, picked_up
            FROM parking_history
            WHERE subscriber_code = ?
            ORDER BY entry_time DESC
        """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, subscriberCode);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(mapParkingHistory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

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

    public void insertSystemLog(String action, String target, String subscriberCode) {
        String sql = """
            INSERT INTO system_log (action, target, by_user, log_time, note)
            SELECT ?, ?, u.id, NOW(), 'Car Deposit'
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

    public void insertParkingHistory(ParkingHistory history) {
        String sql = """
            INSERT INTO parking_history (
                subscriber_code,
                parking_space_id,
                entry_time,
                exit_time,
                extended,
                was_late,
                picked_up
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, history.getSubscriberCode());
            stmt.setInt(2, history.getParkingSpaceId());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(history.getEntryTime()));
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(history.getExitTime()));
            stmt.setBoolean(5, history.isExtended());
            stmt.setBoolean(6, history.isWasLate());
            stmt.setBoolean(7, history.isPickedUp());
            stmt.executeUpdate();
            System.out.println("✅ Parking deposit saved for " + history.getSubscriberCode());
        } catch (SQLException e) {
            System.err.println("❌ Failed to insert parking deposit");
            e.printStackTrace();
        }
    }

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
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking active reservation:");
            e.printStackTrace();
            return false;
        }
    }

    public ParkingHistory getActiveParkingBySubscriber(String subscriberCode) {
        String query = "SELECT * FROM parking_history " +
                       "WHERE subscriber_code = ? AND exit_time > NOW() " +
                       "ORDER BY exit_time DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, subscriberCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapParkingHistory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true; // Conflict exists
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int updateExitTime(String subscriberCode, LocalDateTime newExitTime) {
        String query = "UPDATE parking_history SET exit_time = ?, extended = 1 " +
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

    public ParkingHistory getPendingParkingBySubscriberAndSpot(String subscriberCode, int parkingSpaceId) {
        String query = "SELECT * FROM parking_history WHERE subscriber_code = ? AND parking_space_id = ? AND picked_up = 0 ORDER BY entry_time DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, subscriberCode);
            stmt.setInt(2, parkingSpaceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapParkingHistory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Marks the parking session as picked up, updates exit time, and (if late) sets extended/was_late flags.
     *
     * @param subscriberCode The subscriber's code
     * @param parkingSpaceId The parking spot ID
     * @param wasLate        True if pickup is late
     * @param pickupTime     The actual pickup time (now)
     * @return number of rows updated (should be 1 if successful)
     */
    public int completePickup(String subscriberCode, int parkingSpaceId, boolean wasLate, LocalDateTime pickupTime) {
        String query;
        if (wasLate) {
            // Set extended and was_late flags
            query = "UPDATE parking_history SET exit_time = ?, picked_up = 1, extended = 1, was_late = 1 " +
                    "WHERE subscriber_code = ? AND parking_space_id = ? AND picked_up = 0 " +
                    "ORDER BY entry_time DESC LIMIT 1";
        } else {
            // Only update exit_time and picked_up
            query = "UPDATE parking_history SET exit_time = ?, picked_up = 1 " +
                    "WHERE subscriber_code = ? AND parking_space_id = ? AND picked_up = 0 " +
                    "ORDER BY entry_time DESC LIMIT 1";
        }
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(pickupTime));
            stmt.setString(2, subscriberCode);
            stmt.setInt(3, parkingSpaceId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}

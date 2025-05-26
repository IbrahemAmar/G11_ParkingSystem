package server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import common.Order;
import common.ParkingSession;

public class DBController {
//TESTIN G FIRST
	// JDBC connection parameters
	private static final String URL = "jdbc:mysql://localhost:3306/bpark?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";
	private static final String USER = "root";
	private static final String PASSWORD = "Aa123456";

	// Establishes and returns a new connection to the MySQL database
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	public List<ParkingSession> getActiveParkingSessions() {
	    List<ParkingSession> sessions = new ArrayList<>();

	    String sql = "SELECT subscriber_id, spot_id, entry_time, expected_exit_time, parking_code, extension_requested, is_late " +
	                 "FROM parking_history WHERE actual_exit_time IS NULL";

	    try (Connection conn = getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            sessions.add(new ParkingSession(
	                rs.getInt("subscriber_id"),
	                rs.getInt("spot_id"),
	                rs.getTimestamp("entry_time").toLocalDateTime(),
	                rs.getTimestamp("expected_exit_time").toLocalDateTime(),
	                rs.getString("parking_code"),
	                rs.getBoolean("extension_requested"),
	                rs.getBoolean("is_late")
	            ));
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return sessions;
	}


	public List<Order> getOrdersBySubscriberId(int subscriberId) {
		List<Order> orders = new ArrayList<>();
		String sql = "SELECT * FROM `Order` WHERE subscriber_id = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, subscriberId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				orders.add(new Order(rs.getInt("order_number"), rs.getInt("parking_space"), rs.getDate("order_date"),
						rs.getInt("confirmation_code"), rs.getInt("subscriber_id"),
						rs.getDate("date_of_placing_an_order")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return orders;
	}

	public String checkUserCredentials(String username, String password) {
		String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, username);
			stmt.setString(2, password);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getString("role"); // return role if login is correct
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null; // login failed
	}

}

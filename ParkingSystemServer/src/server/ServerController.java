package server;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import servergui.ServerMainController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import common.ClientRequest;
import common.Order;
import common.ParkingSession;

public class ServerController extends AbstractServer {

	// Map to keep track of connected clients and their IP/Host info
	private Map<ConnectionToClient, String[]> clientInfoMap = new ConcurrentHashMap<>();

	// Database access object
	private DBController db = new DBController(); 

	// Reference to the server GUI controller
	private ServerMainController guiController;

	/**
	 * Constructs the server controller with a specific port and GUI controller
	 * reference.
	 */
	public ServerController(int port, ServerMainController guiController) {
		super(port);
		this.guiController = guiController;
	}

	/**
	 * Handles incoming messages from clients. Processes ClientRequest objects and
	 * performs appropriate DB operations.
	 */
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		if (msg instanceof ClientRequest request) {
			try {
				String command = request.getCommand();
				Object[] params = request.getParams();

				switch (command) {
				case "LOGIN":
				    String username = params[0].toString();
				    String password = params[1].toString();

				    String role = db.checkUserCredentials(username, password);
				    if (role != null) {
				        client.sendToClient("ROLE:" + role); // example: ROLE:supervisor
				    } else {
				        client.sendToClient("LOGIN_FAILED");
				    }
				    break;

				case "SEARCH_ORDERS_BY_SUBSCRIBER_ID":
					int subscriberId = Integer.parseInt(params[0].toString());
					List<Order> results = db.getOrdersBySubscriberId(subscriberId);
					client.sendToClient(results);
					break;
				case "GET_ACTIVE_PARKING":
				    List<ParkingSession> sessions = db.getActiveParkingSessions(); // ✅ should return real objects
				    client.sendToClient(sessions);
				    break;

				case "DISCONNECT":
					client.close(); // Client explicitly asked to disconnect
					break;

				default:
					client.sendToClient("❌ Unknown command: " + command);
				}

			} catch (Exception e) {
				System.err.println("❌ Error handling client request: " + e.getMessage());
				e.printStackTrace();
				try {
					client.sendToClient("❌ Error processing command: " + e.getMessage());
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		} else {
			System.err.println("⚠️ Received unsupported message type: " + msg.getClass().getName());
		}
	}

	/**
	 * Triggered when a new client connects to the server. Extracts IP and hostname
	 * info and updates the GUI.
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		String ip = client.getInetAddress().getHostAddress();
		String host = client.getInetAddress().getCanonicalHostName();
		int id = client.hashCode();

		clientInfoMap.put(client, new String[] { ip, host });

		if (guiController != null) {
			guiController.addClient(ip, host, id);
		}

		System.out.println("✅ Client connected: " + ip + " / " + host);
	}

	/**
	 * Triggered when a client disconnects (either intentionally or unexpectedly).
	 * Updates the GUI and removes the client from the internal map.
	 */
	@Override
	protected synchronized void clientDisconnected(ConnectionToClient client) {
		String[] info = clientInfoMap.getOrDefault(client, new String[] { "unknown", "unknown" });
		String ip = info[0];
		String host = info[1];
		int id = client.hashCode();

		if (guiController != null) {
			guiController.updateClientStatus(id, "Disconnected");
		}

		System.out.println("❌ Client disconnected: " + ip + " / " + host);
		// clientInfoMap.remove(client);
	}
}

package entities;

import java.io.Serializable;

/**
 * Represents a parking spot in the BPARK system.
 * This class is used for transferring parking spot availability data
 * between the server and the client.
 * 
 * Implements {@link Serializable} to support object transfer over the network.
 * 
 * @author BPARK
 */
public class ParkingSpot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The number identifying the parking spot.
     */
    private String spotNumber;

    /**
     * The current status of the parking spot (e.g., "Available", "Occupied").
     */
    private String status;

    /**
     * Constructs a new ParkingSpot instance.
     *
     * @param spotNumber the spot number
     * @param status     the current status of the parking spot
     */
    public ParkingSpot(String spotNumber, String status) {
        this.spotNumber = spotNumber;
        this.status = status;
    }

    /**
     * Returns the spot number.
     *
     * @return the spot number
     */
    public String getSpotNumber() {
        return spotNumber;
    }

    /**
     * Sets the spot number.
     *
     * @param spotNumber the new spot number
     */
    public void setSpotNumber(String spotNumber) {
        this.spotNumber = spotNumber;
    }

    /**
     * Returns the current status of the spot.
     *
     * @return the status (e.g., "Available", "Occupied")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the spot.
     *
     * @param status the new status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns a string representation of the ParkingSpot.
     *
     * @return string in the format "[spotNumber: status]"
     */
    @Override
    public String toString() {
        return "[" + spotNumber + ": " + status + "]";
    }
}

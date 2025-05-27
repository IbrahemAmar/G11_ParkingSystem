package entities;

import java.io.Serializable;

/**
 * Represents a parking space.
 */
public class ParkingSpace implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID of the parking space */
    private int parkingSpaceId;

    /** Availability status of the parking space */
    private boolean isAvailable;

	public int getParkingSpaceId() {
		return parkingSpaceId;
	}

	public void setParkingSpaceId(int parkingSpaceId) {
		this.parkingSpaceId = parkingSpaceId;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

    // Getters and setters can be added here
}


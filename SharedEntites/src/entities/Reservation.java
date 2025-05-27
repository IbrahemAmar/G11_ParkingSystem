package entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a parking reservation made by a subscriber.
 */
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique reservation ID */
    private int reservationId;

    /** Subscriber's code associated with this reservation */
    private String subscriberCode;

    /** ID of the reserved parking space */
    private int parkingSpaceId;

    /** Date and time of the reservation */
    private LocalDateTime reservationDate;

    /** Optional confirmation code */
    private Integer confirmationCode;

    /** Reservation status: active, cancelled, expired */
    private String status;

	public int getReservationId() {
		return reservationId;
	}

	public void setReservationId(int reservationId) {
		this.reservationId = reservationId;
	}

	public String getSubscriberCode() {
		return subscriberCode;
	}

	public void setSubscriberCode(String subscriberCode) {
		this.subscriberCode = subscriberCode;
	}

	public int getParkingSpaceId() {
		return parkingSpaceId;
	}

	public void setParkingSpaceId(int parkingSpaceId) {
		this.parkingSpaceId = parkingSpaceId;
	}

	public LocalDateTime getReservationDate() {
		return reservationDate;
	}

	public void setReservationDate(LocalDateTime reservationDate) {
		this.reservationDate = reservationDate;
	}

	public Integer getConfirmationCode() {
		return confirmationCode;
	}

	public void setConfirmationCode(Integer confirmationCode) {
		this.confirmationCode = confirmationCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

    // Getters and setters can be added here
}

package entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents the history of a parking session.
 */
public class ParkingHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique ID for the parking session history record */
    private int historyId;

    /** Subscriber's code for this session */
    private String subscriberCode;

    /** ID of the parking space used */
    private int parkingSpaceId;

    /** Entry timestamp */
    private LocalDateTime entryTime;

    /** Exit timestamp, if available */
    private LocalDateTime exitTime;

    /** Indicates whether the session was extended */
    private boolean extended;

    /** Indicates whether the exit was late */
    private boolean wasLate;

    // Getters and setters
    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
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

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isWasLate() {
        return wasLate;
    }

    public void setWasLate(boolean wasLate) {
        this.wasLate = wasLate;
    }
}

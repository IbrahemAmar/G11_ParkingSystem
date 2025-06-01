package entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a record in a subscriber's parking history.
 */
public class ParkingHistory implements Serializable {
    private int historyId;
    private String subscriberCode;
    private int parkingSpaceId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private boolean extended;
    private boolean wasLate;
    private boolean pickedUp;


    public ParkingHistory(int historyId, String subscriberCode, int parkingSpaceId,
                         LocalDateTime entryTime, LocalDateTime exitTime,
                         boolean extended, boolean wasLate,boolean pickedUp) {
        this.historyId = historyId;
        this.subscriberCode = subscriberCode;
        this.parkingSpaceId = parkingSpaceId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.extended = extended;
        this.wasLate = wasLate;
        this.pickedUp = pickedUp;

    }

    // Getters

    public int getHistoryId() { return historyId; }
    public String getSubscriberCode() { return subscriberCode; }
    public int getParkingSpaceId() { return parkingSpaceId; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public boolean isExtended() { return extended; }
    public boolean isWasLate() { return wasLate; }
    public boolean isPickedUp() { return pickedUp; }
    public void setPickedUp(boolean pickedUp) { this.pickedUp = pickedUp; }
}

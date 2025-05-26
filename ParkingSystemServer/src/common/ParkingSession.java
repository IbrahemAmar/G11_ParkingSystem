package common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ParkingSession implements Serializable {
    private int subscriberId;
    private int spotId;
    private LocalDateTime entryTime;
    private LocalDateTime expectedExitTime;
    private String parkingCode;
    private boolean extensionRequested;
    private boolean isLate;

    public ParkingSession(int subscriberId, int spotId, LocalDateTime entryTime,
                          LocalDateTime expectedExitTime, String parkingCode,
                          boolean extensionRequested, boolean isLate) {
        this.subscriberId = subscriberId;
        this.spotId = spotId;
        this.entryTime = entryTime;
        this.expectedExitTime = expectedExitTime;
        this.parkingCode = parkingCode;
        this.extensionRequested = extensionRequested;
        this.isLate = isLate;
    }

    public int getSubscriberId() { return subscriberId; }
    public int getSpotId() { return spotId; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExpectedExitTime() { return expectedExitTime; }
    public String getParkingCode() { return parkingCode; }
    public boolean isExtensionRequested() { return extensionRequested; }
    public boolean isLate() { return isLate; }
}

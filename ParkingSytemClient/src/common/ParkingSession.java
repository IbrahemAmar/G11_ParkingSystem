package common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ParkingSession implements Serializable {

    private int subscriberId;
    private String carPlate;
    private int spotId;
    private LocalDateTime entryTime;
    private LocalDateTime expectedExitTime;
    private String parkingCode;

    public ParkingSession(int subscriberId, String carPlate, int spotId,
                          LocalDateTime entryTime, LocalDateTime expectedExitTime,
                          String parkingCode) {
        this.subscriberId = subscriberId;
        this.carPlate = carPlate;
        this.spotId = spotId;
        this.entryTime = entryTime;
        this.expectedExitTime = expectedExitTime;
        this.parkingCode = parkingCode;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public String getCarPlate() {
        return carPlate;
    }

    public int getSpotId() {
        return spotId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExpectedExitTime() {
        return expectedExitTime;
    }

    public String getParkingCode() {
        return parkingCode;
    }
}

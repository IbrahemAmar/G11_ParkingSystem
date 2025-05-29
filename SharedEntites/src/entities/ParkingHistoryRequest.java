package entities;

import java.io.Serializable;

/**
 * Request to retrieve parking history for a specific subscriber.
 */
public class ParkingHistoryRequest implements Serializable {
    private final String subscriberCode;

    public ParkingHistoryRequest(String subscriberCode) {
        this.subscriberCode = subscriberCode;
    }

    public String getSubscriberCode() {
        return subscriberCode;
    }
}
package entities;

import java.io.Serializable;

/**
 * Request from client to server to extend parking time by 4 hours.
 * Contains the subscriber code as the request data.
 */
public class ExtendParkingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String subscriberCode;

    /**
     * Constructs an ExtendParkingRequest.
     *
     * @param subscriberCode the subscriber code requesting extension
     */
    public ExtendParkingRequest(String subscriberCode) {
        this.subscriberCode = subscriberCode;
    }

    /**
     * Gets the subscriber code.
     *
     * @return the subscriber code
     */
    public String getSubscriberCode() {
        return subscriberCode;
    }

    /**
     * Sets the subscriber code.
     *
     * @param subscriberCode the subscriber code to set
     */
    public void setSubscriberCode(String subscriberCode) {
        this.subscriberCode = subscriberCode;
    }
}

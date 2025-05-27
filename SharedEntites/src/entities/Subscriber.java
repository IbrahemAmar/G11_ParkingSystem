package entities;

import java.io.Serializable;

/**
 * Represents a parking system subscriber.
 */
public class Subscriber implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique code identifying the subscriber */
    private String subscriberCode;

    /** User ID that this subscriber is associated with */
    private int subscriberId;

    /** Email address of the subscriber */
    private String email;

    /** Phone number of the subscriber */
    private String phoneNumber;

	public String getSubscriberCode() {
		return subscriberCode;
	}

	public void setSubscriberCode(String subscriberCode) {
		this.subscriberCode = subscriberCode;
	}

	public int getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(int subscriberId) {
		this.subscriberId = subscriberId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

    // Getters and setters can be added here
}

package common;

import java.io.Serializable;
import java.sql.Date;

public class Order implements Serializable {
    private int orderNumber;
    private int parkingSpace;
    private Date orderDate;
    private int confirmationCode;
    private int subscriberId;
    private Date dateOfPlacingOrder;

    public Order(int orderNumber, int parkingSpace, Date orderDate, int confirmationCode, int subscriberId,
                 Date dateOfPlacingOrder) {
        this.orderNumber = orderNumber;
        this.parkingSpace = parkingSpace;
        this.orderDate = orderDate;
        this.confirmationCode = confirmationCode;
        this.subscriberId = subscriberId;
        this.dateOfPlacingOrder = dateOfPlacingOrder;
    }

    public int getOrderNumber() { return orderNumber; }
    public void setOrderNumber(int orderNumber) { this.orderNumber = orderNumber; }

    public int getParkingSpace() { return parkingSpace; }
    public void setParkingSpace(int parkingSpace) { this.parkingSpace = parkingSpace; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public int getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(int confirmationCode) { this.confirmationCode = confirmationCode; }

    public int getSubscriberId() { return subscriberId; }
    public void setSubscriberId(int subscriberId) { this.subscriberId = subscriberId; }

    public Date getDateOfPlacingOrder() { return dateOfPlacingOrder; }
    public void setDateOfPlacingOrder(Date dateOfPlacingOrder) { this.dateOfPlacingOrder = dateOfPlacingOrder; }

    // ✅ Helper string getters for TableView
    public String getOrderDateString() { return orderDate != null ? orderDate.toString() : ""; }
    public String getDateOfPlacingOrderString() { return dateOfPlacingOrder != null ? dateOfPlacingOrder.toString() : ""; }

    @Override
    public String toString() {
        return "Order {" +
                "orderNumber=" + orderNumber +
                ", parkingSpace=" + parkingSpace +
                ", orderDate=" + orderDate +
                ", confirmationCode=" + confirmationCode +
                ", subscriberId=" + subscriberId +
                ", dateOfPlacingOrder=" + dateOfPlacingOrder +
                '}';
    }
}

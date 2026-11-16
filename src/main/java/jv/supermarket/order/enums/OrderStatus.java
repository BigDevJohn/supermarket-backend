package jv.supermarket.order.enums;

public enum OrderStatus {
    WAITING_PAYMENT("Waiting for payment"),
    PREPARING("Preparing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

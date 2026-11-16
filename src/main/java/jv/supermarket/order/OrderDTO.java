package jv.supermarket.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import jv.supermarket.order.enums.OrderStatus;

public class OrderDTO {

    private Set<OrderItemDTO> items;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime date;

    public OrderDTO() {
    }

    public OrderDTO(Set<OrderItemDTO> items, BigDecimal totalPrice, OrderStatus status, LocalDateTime date) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.date = date;
    }

    public Set<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(Set<OrderItemDTO> items) {
        this.items = items;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

}

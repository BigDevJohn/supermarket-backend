package jv.supermarket.cart;

import java.math.BigDecimal;
import java.util.Set;

public class CartDTO {

    private Set<CartItemDTO> items;
    private BigDecimal totalPrice;

    public Set<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(Set<CartItemDTO> items) {
        this.items = items;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

}

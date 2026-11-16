package jv.supermarket.order;

import java.math.BigDecimal;

import jv.supermarket.product.ProductDTO;

public class OrderItemDTO {

    private ProductDTO product;
    private int quantity;
    private BigDecimal totalPrice;

    public OrderItemDTO() {
    }

    public OrderItemDTO(ProductDTO product, int quantity, BigDecimal totalPrice) {
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public ProductDTO getProduct() {
        return product;
    }

    public void setProduct(ProductDTO product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

}

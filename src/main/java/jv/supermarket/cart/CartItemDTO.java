package jv.supermarket.cart;

import java.math.BigDecimal;

import jv.supermarket.product.ProductDTO;

public class CartItemDTO {

    private ProductDTO product;
    private int quantity;
    private BigDecimal subTotal;

    public CartItemDTO() {
    }

    public CartItemDTO(ProductDTO product, int quantity, BigDecimal subTotal) {
        this.product = product;
        this.quantity = quantity;
        this.subTotal = subTotal;
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

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

}

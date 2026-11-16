package jv.supermarket.product;

import java.math.BigDecimal;
import java.util.List;

public class ProductRequestDTO {

    private String name;
    private String brand;
    private BigDecimal price;
    private String description;
    private int stock;
    private List<Long> categories;

    public ProductRequestDTO(String name, String brand, BigDecimal price, int stock, String description,
            List<Long> categories) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.categories = categories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getCategories() {
        return categories;
    }

    public void setCategories(List<Long> categories) {
        this.categories = categories;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

}

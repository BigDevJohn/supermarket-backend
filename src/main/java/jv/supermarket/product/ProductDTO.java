package jv.supermarket.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jv.supermarket.image.ImageDTO;

public class ProductDTO {

    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private String description;

    private Set<String> categories = new HashSet<>();
    private List<ImageDTO> images = new ArrayList<>();

    public ProductDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public List<ImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ImageDTO> images) {
        this.images = images;
    }

    public void addImageDTO(ImageDTO dto) {
        if (this.images == null) {
            images = new ArrayList<>();
        }
        images.add(dto);
    }

    

}

package jv.supermarket.product;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jv.supermarket.category.Category;
import jv.supermarket.category.CategoryDTO;
import jv.supermarket.category.CategoryRepository;
import jv.supermarket.image.ImageDTO;
import jv.supermarket.shared.customexception.AlreadyExistException;
import jv.supermarket.shared.customexception.ResourceNotFoundException;
import jv.supermarket.stock.Stock;
import jv.supermarket.stock.StockService;
import jv.supermarket.user.UserService;

@Service
public class ProductService {

    final ProductRepository productRepository;

    final CategoryRepository categoryRepository;

    final UserService userService;

    final StockService stockService;

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
            UserService userService, StockService stockService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
        this.stockService = stockService;
    }

    private boolean productExists(String name, String brand) {
        return productRepository.existsByNameAndBrand(name, brand);
    }

    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> products;
        if (userService.isClient()) {
            log.info("[getAllProducts] User is a client, viewing available products.");
            products = productRepository.findAllByAvailable(true, pageable);
        } else {
            log.info("[getAllProducts] User is not a client, viewing all products.");
            products = productRepository.findAll(pageable);
        }
        return products.map(this::convertToDTO);
    }

    @Transactional
    public ProductDTO saveProduct(ProductRequestDTO dto) {
        if (productExists(dto.getName(), dto.getBrand())) {
            log.warn("[saveProduct] Attempt to create a duplicate product with name: {} and brand: {}", dto.getName(), dto.getBrand());
            throw new AlreadyExistException("A product with this name and brand already exists.");
        }
        log.info("[saveProduct] Initializing product creation");

        Product product = new Product();
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());

        log.info("[saveProduct] Adding {} categories to the product", dto.getCategories().size());
        addCategoriesToProduct(product, dto.getCategories());

        // Persist product first so it gets an ID before Stock (which uses @MapsId) is
        // created
        Product savedProduct = productRepository.save(product);
        log.info("[saveProduct] Product saved with ID: {}", savedProduct.getId());

        Stock stock = stockService.buildStock(dto.getStock(), savedProduct);
        savedProduct.setStock(stock);
        log.info("[saveProduct] Stock initialized with quantity: {}", stock.getQuantity());
        log.info("[saveProduct] Product creation completed successfully for ID: {}", savedProduct.getId());
        return convertToDTO(productRepository.save(savedProduct));
    }

    private void addCategoriesToProduct(Product product, List<Long> categoryIds) {
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> {
                        log.warn("[addCategoriesToProduct] Category with id: {} not found", categoryId);
                        return new ResourceNotFoundException("Category with id: " + categoryId + " not found");
                    });
            product.getCategories().add(category);
        }
        log.info("[addCategoriesToProduct] {} categories added to the product", categoryIds.size());
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[getProductById] Product with id: {} not found", id);
                    return new ResourceNotFoundException("Product with id: " + id + " not found");
                });
    }

    public ProductDTO getProductDTOById(Long id) {
        log.info("[getProductDTOById] Fetching product DTO with ID: {}", id);
        return convertToDTO(getProductById(id));
    }

    public ProductDTO updateProduct(Product product, Long id) {
        log.info("[updateProduct] Updating product with ID: {}", id);

        Product savedProduct = getProductById(id);
        log.info("[updateProduct] Product found with ID: {}", savedProduct.getId());
        
        savedProduct.setName(product.getName());
        savedProduct.setBrand(product.getBrand());
        savedProduct.setPrice(product.getPrice());
        savedProduct.setDescription(product.getDescription());

        Product updated = productRepository.save(savedProduct);
        log.info("[updateProduct] Product with ID: {} updated successfully", updated.getId());
        return convertToDTO(updated);

    }

    public void deleteProductById(Long id) {
        if (productRepository.existsById(id)) {
            log.info("[deleteProductById] Attempting to delete product with ID: {}", id);
            try {
                productRepository.deleteById(id);
                log.info("[deleteProductById] Product with ID: {} deleted successfully", id);
            } catch (DataIntegrityViolationException e) {
                log.warn("[deleteProductById] Product with ID: {} cannot be deleted due to data integrity constraints. Falling back to making it unavailable.", id);
                makeUnavailable(id);
            } catch (Exception e) {
                log.error("[deleteProductById] An unexpected error occurred while deleting product with ID: {}", id, e);
                throw new RuntimeException("An unexpected error occurred while deleting the product.");
            }
        } else {
            log.warn("[deleteProductById] Product with id: {} not found", id);
            throw new ResourceNotFoundException("Product with id: " + id + " not found");
        }
    }

    public void makeUnavailable(Long id) {
        log.info("[makeUnavailable] Attempting to make product with ID: {} unavailable", id);
        if (productRepository.existsById(id)) {
            Product product = productRepository.findById(id).get();
            if (!product.isAvailable()) {
                log.warn("[makeUnavailable] Product with ID: {} was already unavailable", id);
                throw new IllegalStateException("The product was already unavailable.");
            }
            product.setAvailable(false);
            productRepository.save(product);
            log.info("[makeUnavailable] Product with ID: {} is now unavailable", id);
        } else {
            log.warn("[makeUnavailable] Product with id: {} not found", id);
            throw new ResourceNotFoundException("Product with id: " + id + " not found");
        }
    }

    public void makeAvailable(Long id) {
        log.info("[makeAvailable] Attempting to make product with ID: {} available", id);
        if (productRepository.existsById(id)) {
            Product product = productRepository.findById(id).get();
            if (product.isAvailable()) {
                log.warn("[makeAvailable] Product with ID: {} was already available", id);
                throw new IllegalStateException("The product was already available.");
            }
            product.setAvailable(true);
            productRepository.save(product);
            log.info("[makeAvailable] Product with ID: {} is now available", id);
        } else {
            log.warn("[makeAvailable] Product with id: {} not found", id);
            throw new ResourceNotFoundException("Product with id: " + id + " not found");
        }
    }

    public Page<ProductDTO> getProductsByName(String name, Pageable pageable) {
        Page<Product> products;
        if (userService.isClient()) {
            products = productRepository.findByNameContainingIgnoreCaseAndAvailable(name, true, pageable);
        } else {
            products = productRepository.findByNameContainingIgnoreCase(name, pageable);
        }

        log.info("[getProductsByName] Found {} products with name: {}", products.getTotalElements(), name);
        if (products.getTotalElements() == 0) {
            log.warn("[getProductsByName] No products found with name: {}", name);
            throw new ResourceNotFoundException("No products found with name: " + name);
        }
        return products.map(this::convertToDTO);
    }

    public Page<ProductDTO> getProductsByBrand(String brand, Pageable pageable) {
        Page<Product> products;
        if (userService.isClient()) {
            products = productRepository.findByBrandContainingIgnoreCaseAndAvailable(brand, true, pageable);
        } else {
            products = productRepository.findByBrandContainingIgnoreCase(brand, pageable);
        }

        log.info("[getProductsByBrand] Found {} products with brand: {}", products.getTotalElements(), brand);
        if (products.getTotalElements() == 0) {
            log.warn("[getProductsByBrand] No products found with brand: {}", brand);
            throw new ResourceNotFoundException("No products found with brand: " + brand);
        }
        return products.map(this::convertToDTO);
    }

    public ProductDTO getProductByBrandAndName(String brand, String name) {
        Product product;
        if (userService.isClient()) {
            product = productRepository.findByBrandAndNameAndAvailable(brand, name, true);
        } else {
            product = productRepository.findByBrandAndName(brand, name);
        }
        if (product == null) {
            log.warn("[getProductByBrandAndName] No product found with brand: {} and name: {}", brand, name);
            throw new ResourceNotFoundException("Product with brand: " + brand + " and name: " + name + " not found");
        }
        log.info("[getProductByBrandAndName] Found product with brand: {} and name: {}", brand, name);
        return convertToDTO(product);
    }

    public Page<ProductDTO> getProductsByCategoryName(String name, Pageable pageable) {
        Page<Product> products;
        if (userService.isClient()) {
            products = productRepository.findByCategoryNameAndAvailable(name, true, pageable);
        } else {
            products = productRepository.findByCategoryName(name, pageable);
        }
        log.info("[getProductsByCategoryName] Found {} products with category name: {}", products.getTotalElements(),
                name);
        if (products.getTotalElements() == 0) {
            log.warn("[getProductsByCategoryName] No products found with category name: {}", name);
            throw new ResourceNotFoundException("Products with category name: " + name + " not found");
        }
        return products.map(this::convertToDTO);
    }

    public ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());

        // Convert Set<Category> → Set<CategoryDTO>
        Set<CategoryDTO> categoryDTOs = product.getCategories().stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName()))
                .collect(Collectors.toSet());
        dto.setCategories(categoryDTOs);

        // Convert List<Image> → List<ImageDTO>
        if (product.getImages() != null) {
            List<ImageDTO> imageDTOs = product.getImages().stream()
                    .map(img -> new ImageDTO(img.getFileName(), img.getFileType(), img.getDownloadUrl()))
                    .collect(Collectors.toList());
            dto.setImages(imageDTOs);
        }

        return dto;
    }

    public boolean existById(Long productId) {
        return productRepository.existsById(productId);
    }

}

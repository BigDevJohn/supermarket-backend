package jv.supermarket.product;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jv.supermarket.category.Category;
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
            products = productRepository.findAllByAvailable(true, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        return products.map(this::convertToDTO);
    }

    @Transactional
    public ProductDTO saveProduct(ProductRequestDTO dto) {
        if (productExists(dto.getName(), dto.getBrand())) {
            throw new AlreadyExistException("A product with this name and brand already exists.");
        }
        Product product = new Product();
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());

        addCategoriesToProduct(product, dto.getCategories());

        // Persist product first so it gets an ID before Stock (which uses @MapsId) is created
        Product savedProduct = productRepository.save(product);

        Stock stock = stockService.buildStock(dto.getStock(), savedProduct);
        savedProduct.setStock(stock);

        return convertToDTO(productRepository.save(savedProduct));
    }

    private void addCategoriesToProduct(Product product, List<Long> categoryIds) {
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category with id: " + categoryId + " not found"));
            product.getCategories().add(category);
        }
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id: " + id + " not found"));
    }

    public ProductDTO getProductDTOById(Long id) {
        return convertToDTO(getProductById(id));
    }

    public ProductDTO updateProduct(Product product, Long id) {
        if (productRepository.existsById(id)) {
            Product savedProduct = getProductById(id);

            savedProduct.setName(product.getName());
            savedProduct.setBrand(product.getBrand());
            savedProduct.setPrice(product.getPrice());
            savedProduct.setDescription(product.getDescription());

            return convertToDTO(productRepository.save(savedProduct));
        }
        throw new ResourceNotFoundException("Product with id: " + id + " not found");
    }

    public void deleteProductById(Long id) {
        if (productRepository.existsById(id)) {
            try {
                productRepository.deleteById(id);
            } catch (DataIntegrityViolationException e) {
                makeUnavailable(id);
            }
        } else {
            throw new ResourceNotFoundException("Product with id: " + id + " not found");
        }
    }

    public void makeUnavailable(Long id) {
        if (productRepository.existsById(id)) {
            Product product = productRepository.findById(id).get();
            if (!product.isAvailable()) {
                throw new IllegalStateException("The product was already unavailable.");
            }
            product.setAvailable(false);
            productRepository.save(product);
        } else {
            throw new ResourceNotFoundException("Product with id: " + id + " not found");
        }
    }

    public void makeAvailable(Long id) {
        if (productRepository.existsById(id)) {
            Product product = productRepository.findById(id).get();
            if (product.isAvailable()) {
                throw new IllegalStateException("The product was already available.");
            }
            product.setAvailable(true);
            productRepository.save(product);
        } else {
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

        if (products.getTotalElements() == 0) {
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

        if (products.getTotalElements() == 0) {
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
            throw new ResourceNotFoundException("Product with brand: " + brand + " and name: " + name + " not found");
        }
        return convertToDTO(product);
    }

    public Page<ProductDTO> getProductsByCategoryName(String name, Pageable pageable) {
        Page<Product> products;
        if (userService.isClient()) {
            products = productRepository.findByCategoryNameAndAvailable(name, true, pageable);
        } else {
            products = productRepository.findByCategoryName(name, pageable);
        }
        if (products.getTotalElements() == 0) {
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

        // Convert Set<Category> → Set<String>
        Set<String> categoryNames = product.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
        dto.setCategories(categoryNames);

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

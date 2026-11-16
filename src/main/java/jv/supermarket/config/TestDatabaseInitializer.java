package jv.supermarket.config;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import jv.supermarket.cart.Cart;
import jv.supermarket.cart.CartItem;
import jv.supermarket.cart.CartItemRepository;
import jv.supermarket.cart.CartRepository;
import jv.supermarket.category.Category;
import jv.supermarket.category.CategoryRepository;
import jv.supermarket.product.Product;
import jv.supermarket.product.ProductRepository;
import jv.supermarket.stock.Stock;
import jv.supermarket.stock.StockRepository;
import jv.supermarket.user.User;
import jv.supermarket.user.UserService;

@Configuration
@Profile("test")
@Order(2)
public class TestDatabaseInitializer implements CommandLineRunner {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final UserService userService;

    TestDatabaseInitializer(UserService userService, CartItemRepository cartItemRepository, CartRepository cartRepository,
            CategoryRepository categoryRepository, ProductRepository productRepository, StockRepository stockRepository) {
        this.userService = userService;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        User employee = new User("kleber", "employee@supermarket.local", "123456");
        User client = new User("joao", "customer@supermarket.local", "123456");

        employee = userService.saveEmployee(employee);
        client = userService.saveClient(client);

        // Create categories
        Category c1 = categoryRepository.save(new Category("Eletrônicos"));
        Category c2 = categoryRepository.save(new Category("Mobília"));
        Category c3 = categoryRepository.save(new Category("Smartphones"));
        Category c4 = categoryRepository.save(new Category("Cozinha"));

        // Create and associate products
        Product p1 = saveProduct("Smartphone", "Samsung", new BigDecimal(3000), 20,
                "O melhor da Samsung", c3, c1);

        saveProduct("Smartphone", "Xiaomi", new BigDecimal(3200), 32,
                "O mundo todo no seu bolso", c3, c1);

        saveProduct("Geladeira", "Samsung", new BigDecimal(4000), 10,
                "Gela que é uma beleza!", c4, c1);

        saveProduct("Cama de Casal", "Plumatex", new BigDecimal(1500),
                5, "O que há de conforto para você", c2);

        Cart cart = cartRepository.findById(client.getId())
                .orElseThrow(() -> new IllegalStateException("Client cart was not created"));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(p1);
        cartItem.setQuantity(3);
        cartItemRepository.save(cartItem);
    }

    private Product saveProduct(String name, String brand, BigDecimal price, int stockQuantity,
            String description, Category... categories) {
        Product product = new Product();
        product.setName(name);
        product.setBrand(brand);
        product.setPrice(price);
        product.setDescription(description);
        Arrays.stream(categories).forEach(product::addCategory);

        Product savedProduct = productRepository.save(product);
        Stock stock = stockRepository.save(new Stock(stockQuantity, savedProduct));
        savedProduct.setStock(stock);
        return productRepository.save(savedProduct);
    }

}

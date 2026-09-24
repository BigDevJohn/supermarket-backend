package jv.supermarket.config;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import jv.supermarket.cart.Cart;
import jv.supermarket.cart.CartItemService;
import jv.supermarket.cart.CartService;
import jv.supermarket.category.Category;
import jv.supermarket.category.CategoryService;
import jv.supermarket.order.OrderService;
import jv.supermarket.product.ProductDTO;
import jv.supermarket.product.ProductRequestDTO;
import jv.supermarket.product.ProductService;
import jv.supermarket.user.User;
import jv.supermarket.user.UserService;

@Configuration
@Profile("test")
@Order(2)
public class TestDatabaseInitializer implements CommandLineRunner {

    private final ProductService productService;

    private final CategoryService categoryService;

    private final CartItemService cartItemService;

    private final CartService cartService;

    private final UserService userService;

    @SuppressWarnings("unused")
    private final OrderService orderService;

    TestDatabaseInitializer(UserService userService, CategoryService categoryService, CartService cartService, OrderService orderService, CartItemService cartItemService, ProductService productService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) throws Exception {

        User employee = new User("kleber", "employee@supermarket.local", "123456");
        User client = new User("joao", "customer@supermarket.local", "123456");

        employee = userService.saveEmployee(employee);
        client = userService.saveClient(client);

        // Create categories
        Category c1 = categoryService.saveCategory(new Category("Eletrônicos"));
        Category c2 = categoryService.saveCategory(new Category("Mobília"));
        Category c3 = categoryService.saveCategory(new Category("Smartphones"));
        Category c4 = categoryService.saveCategory(new Category("Cozinha"));

        // Create and associate products
        ProductDTO p1 = productService.saveProduct(new ProductRequestDTO("Smartphone", "Samsung", new BigDecimal(3000), 20,
                "O melhor da Samsung", Arrays.asList(c3.getId(), c1.getId())));

        productService.saveProduct(new ProductRequestDTO("Smartphone", "Xiaomi", new BigDecimal(3200), 32,
                "O mundo todo no seu bolso", Arrays.asList(c3.getId(), c1.getId())));

        productService.saveProduct(new ProductRequestDTO("Geladeira", "Samsung", new BigDecimal(4000), 10,
                "Gela que é uma beleza!", Arrays.asList(c4.getId(), c1.getId())));

        ProductDTO p4 = productService.saveProduct(new ProductRequestDTO("Cama de Casal", "Plumatex", new BigDecimal(1500),
                5, "O que há de conforto para você", Arrays.asList(c2.getId())));

        Cart cart = cartService.getCartById(client.getId());

        cartItemService.addItemToCart(p1.getId(), 2, cart.getId());
        cartItemService.addItemToCart(p4.getId(), 1, cart.getId());
        cartItemService.removeItemFromCart(cart.getId(), p4.getId());
        cartItemService.updateItemQuantity(cart.getId(), p1.getId(), 3);
    }

}

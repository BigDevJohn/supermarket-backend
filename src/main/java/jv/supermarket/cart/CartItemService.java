package jv.supermarket.cart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jv.supermarket.product.Product;
import jv.supermarket.product.ProductService;
import jv.supermarket.shared.customexception.OutOfStockException;
import jv.supermarket.shared.customexception.ResourceNotFoundException;
import jv.supermarket.stock.StockService;

@Service
public class CartItemService {

    private static final Logger log = LoggerFactory.getLogger(CartItemService.class);

    final CartItemRepository cartItemRepository;

    final ProductService productService;

    final CartService cartService;

    final StockService stockService;

    CartItemService(CartService cartService, ProductService productService, CartItemRepository cartItemRepository, StockService stockService) {
        this.cartService = cartService;
        this.productService = productService;
        this.cartItemRepository = cartItemRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void addItemToCart(Long productId, int quantity, Long cartId) {
        log.info("[addItemToCart] Attempting to add product ID: {} with quantity: {} to cart ID: {}", productId, quantity, cartId);
        Product product = productService.getProductById(productId);

        if (!stockService.isStockAvailable(quantity, productId)) {
            log.warn("[addItemToCart] Insufficient stock for product ID: {} with requested quantity: {}", productId, quantity);
            throw new OutOfStockException("The product stock is insufficient for the requested quantity");
        }
        
        if (quantity < 1) {
            log.warn("[addItemToCart] Invalid quantity: {} for product ID: {}. Quantity must be greater than 0", quantity, productId);
            throw new IllegalArgumentException("Item quantity must be greater than 0");
        }

        Cart cart = cartService.getCartById(cartId);
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseGet(() -> createNewCartItem(cart, product, quantity));

        if (cartItem.getId() == null) {
            saveNewCartItem(cart, cartItem);
        } else {
            updateItemQuantity(cartId, productId, quantity);
        }
        log.info("[addItemToCart] Successfully added/updated product ID: {} in cart ID: {}", productId, cartId);
    }

    private CartItem createNewCartItem(Cart cart, Product product, int quantity) {
        log.info("[createNewCartItem] Creating new cart item for product ID: {} in cart ID: {}", product.getId(), cart.getId());
        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(product);
        newItem.setQuantity(quantity);
        return newItem;
    }

    private void saveNewCartItem(Cart cart, CartItem cartItem) {
        log.info("[saveNewCartItem] Saving new cart item for product ID: {} in cart ID: {}", cartItem.getProduct().getId(), cart.getId());
        cartItemRepository.save(cartItem);
        cart.addItem(cartItem);
        cartService.saveCart(cart);
    }

    @Transactional
    public void removeItemFromCart(Long cartId, Long productId) {
        log.info("[removeItemFromCart] Attempting to remove product ID: {} from cart ID: {}", productId, cartId);
        CartItem item = getCartItem(cartId, productId);
        Cart cart = cartService.getCartById(cartId);

        cart.removeItem(item);

        cartService.saveCart(cart);
        log.info("[removeItemFromCart] Product ID: {} successfully removed from cart ID: {}", productId, cartId);
    }

    @Transactional
    public void updateItemQuantity(Long cartId, Long productId, int quantity) {
        log.info("[updateItemQuantity] Updating product ID: {} quantity to {} in cart ID: {}", productId, quantity, cartId);
        CartItem item = getCartItem(cartId, productId);

        if (quantity < 1) {
            log.warn("[updateItemQuantity] Invalid quantity: {} for product ID: {}. Quantity must be greater than 0", quantity, productId);
            throw new IllegalArgumentException("Item quantity must be greater than 0");
        }

        if (!stockService.isStockAvailable(quantity, productId)) {
            log.warn("[updateItemQuantity] Insufficient stock for product ID: {} with requested quantity: {}", productId, quantity);
            throw new OutOfStockException("The product stock is insufficient for the requested quantity");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        log.info("[updateItemQuantity] Quantity for product ID: {} successfully updated to {} in cart ID: {}", productId, quantity, cartId);
    }

    private CartItem getCartItem(Long cartId, Long productId) {
        log.info("[getCartItem] Finding item for product ID: {} in cart ID: {}", productId, cartId);
        Cart cart = cartService.getCartById(cartId);

        return cart.getItems().stream()
                .filter(items -> items.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("[getCartItem] No product with id: {} found in cart with id: {}", productId, cartId);
                    return new ResourceNotFoundException(
                            "No product with id: " + productId + " found in cart with id: " + cartId);
                });
    }

}

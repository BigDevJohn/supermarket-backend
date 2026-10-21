package jv.supermarket.cart;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jv.supermarket.product.ProductService;
import jv.supermarket.shared.customexception.ResourceNotFoundException;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepo;

    private final CartItemRepository itemRepo;

    private final ProductService productService;

    CartService(CartRepository cartRepo, CartItemRepository itemRepo, ProductService productService) {
        this.cartRepo = cartRepo;
        this.itemRepo = itemRepo;
        this.productService = productService;
    }

    public Cart saveCart(Cart cart) {
        log.info("[saveCart] Saving cart with ID: {}", cart.getId());
        Cart saved = cartRepo.save(cart);
        log.info("[saveCart] Cart saved successfully with ID: {}", saved.getId());
        return saved;
    }

    public Cart getCartById(Long cartId) {
        log.info("[getCartById] Fetching cart with ID: {}", cartId);
        return cartRepo.findById(cartId)
                .orElseThrow(() -> {
                    log.warn("[getCartById] Cart not found with id: {}", cartId);
                    return new ResourceNotFoundException("Cart not found with id: " + cartId);
                });
    }

    public CartDTO getCart(Long cartId) {
        log.info("[getCart] Fetching cart DTO for ID: {}", cartId);
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> {
                    log.warn("[getCart] Cart not found with id: {}. The user must be a Client to have a cart", cartId);
                    return new ResourceNotFoundException(
                            "Cart not found with id: " + cartId + ". The user must be a Client to have a cart");
                });
        return convertToDTO(cart);
    }

    @Transactional
    public void clearCart(Long cartId) {
        log.info("[clearCart] Clearing cart with ID: {}", cartId);
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> {
                    log.warn("[clearCart] Cart not found with id: {}", cartId);
                    return new ResourceNotFoundException("Cart not found with id: " + cartId);
                });
        itemRepo.deleteAllByCartId(cartId);
        cart.clearCart();
        log.info("[clearCart] Cart with ID: {} cleared successfully", cartId);
    }

    public boolean existsById(Long cartId) {
        return cartRepo.existsById(cartId);
    }

    public CartItemDTO convertItemToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();

        dto.setProduct(productService.convertToDTO(item.getProduct()));
        dto.setSubTotal(item.getSubTotal());
        dto.setQuantity(item.getQuantity());

        return dto;
    }

    public CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setTotalPrice(cart.getTotalPrice());
        dto.setItems(cart.getItems().stream()
                .map(item -> convertItemToDTO(item))
                .collect(Collectors.toSet()));
        return dto;
    }
}

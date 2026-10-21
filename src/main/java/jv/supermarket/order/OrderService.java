package jv.supermarket.order;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jv.supermarket.cart.Cart;
import jv.supermarket.cart.CartItem;
import jv.supermarket.cart.CartService;
import jv.supermarket.order.enums.OrderStatus;
import jv.supermarket.product.Product;
import jv.supermarket.product.ProductRepository;
import jv.supermarket.product.ProductService;
import jv.supermarket.shared.customexception.OutOfStockException;
import jv.supermarket.shared.customexception.ResourceNotFoundException;
import jv.supermarket.stock.StockService;
import jv.supermarket.user.Role;
import jv.supermarket.user.User;
import jv.supermarket.user.UserService;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    final CartService cartService;

    final OrderItemRepository itemRepo;

    final OrderRepository orderRepo;

    final UserService userService;

    final ProductRepository productRepo;

    final ProductService productService;

    final StockService stockService;

    OrderService(CartService cartService, OrderItemRepository itemRepo, OrderRepository orderRepo,
            UserService userService, ProductRepository productRepo, ProductService productService,
            StockService stockService) {
        this.cartService = cartService;
        this.itemRepo = itemRepo;
        this.orderRepo = orderRepo;
        this.userService = userService;
        this.productRepo = productRepo;
        this.productService = productService;
        this.stockService = stockService;
    }

    @Transactional
    public OrderDTO createOrder() {
        log.info("[createOrder] Initializing order creation for logged user");
        User user = userService.getLoggedUser();
        Cart cart = cartService.getCartById(user.getId());
        if (cart.getItems().isEmpty()) {
            log.warn("[createOrder] Order creation failed: Cart is empty for user ID: {}", user.getId());
            throw new ResourceNotFoundException(
                    "Cart is empty. Add items to it first before placing an order");
        }

        checkCartStock(cart);
        Order order = new Order();

        Set<OrderItem> items = convertCartItemsToOrderItems(cart.getItems());

        for (OrderItem orderItem : items) {
            order.addItem(orderItem);
        }
        order.setUser(user);
        order.setStatus(OrderStatus.WAITING_PAYMENT);
        order.setDate(LocalDateTime.now());

        cartService.clearCart(cart.getId());

        order = orderRepo.save(order);
        log.info("[createOrder] Order created successfully with ID: {} for user ID: {}", order.getId(), user.getId());

        return convertOrderToDTO(order);
    }

    @Transactional
    public OrderDTO getOrder(Long id) {
        log.info("[getOrder] Fetching order with ID: {}", id);
        User user = userService.getLoggedUser();
        Order order = getOrderById(id);

        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                log.info("[getOrder] Order ID: {} accessed by admin user ID: {}", id, user.getId());
                return convertOrderToDTO(order);
            } else if (role.getName().equals("ROLE_CUSTOMER")) {
                if (order.getUser().getId().equals(user.getId())) {
                    log.info("[getOrder] Order ID: {} accessed by owner user ID: {}", id, user.getId());
                    return convertOrderToDTO(order);
                }
            }
        }
        log.warn("[getOrder] Access denied to order ID: {} for user ID: {}", id, user.getId());
        throw new AccessDeniedException("Order not found for this client");
    }

    public Page<OrderDTO> getOrdersByUser(Pageable pageable) {
        Long userId = userService.getLoggedUser().getId();
        log.info("[getOrdersByUser] Fetching orders for logged user ID: {}", userId);
        Page<Order> orders = orderRepo.findByUserId(userId, pageable);
        if (orders == null || orders.isEmpty()) {
            log.warn("[getOrdersByUser] No orders found for user ID: {}", userId);
            throw new ResourceNotFoundException("The user has no orders");
        }
        log.info("[getOrdersByUser] Found {} order(s) for user ID: {}", orders.getTotalElements(), userId);
        return orders.map(order -> convertOrderToDTO(order));
    }

    public Order getOrderById(Long id) {
        log.info("[getOrderById] Fetching order with ID: {}", id);
        return orderRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("[getOrderById] Order not found with id: {}", id);
                    return new ResourceNotFoundException("Order not found with id: " + id);
                });
    }

    public Order cancelOrder(Long orderId) {
        log.info("[cancelOrder] Attempting to cancel order with ID: {}", orderId);
        User user = userService.getLoggedUser();
        Order order = getOrderById(orderId);

        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                order.setStatus(OrderStatus.CANCELLED);
                Order saved = orderRepo.save(order);
                log.info("[cancelOrder] Order ID: {} cancelled by admin user ID: {}", orderId, user.getId());
                return saved;
            } else if (role.getName().equals("ROLE_CUSTOMER")) {
                if (order.getUser().getId().equals(user.getId())) {
                    order.setStatus(OrderStatus.CANCELLED);
                    Order saved = orderRepo.save(order);
                    log.info("[cancelOrder] Order ID: {} cancelled by owner user ID: {}", orderId, user.getId());
                    return saved;
                }
            }
        }
        log.warn("[cancelOrder] Access denied to cancel order ID: {} for user ID: {}", orderId, user.getId());
        throw new AccessDeniedException("Order not found for this client");
    }

    private void checkCartStock(Cart cart) {
        log.info("[checkCartStock] Checking stock availability for cart ID: {}", cart.getId());
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            int availableStock = stockService.getStockById(product.getId()).quantity();
            if (item.getQuantity() > availableStock) {
                int exceededAmount = item.getQuantity() - availableStock;
                log.warn("[checkCartStock] Insufficient stock for product '{}' (ID: {}). Requested: {}, Available: {}",
                        product.getName(), product.getId(), item.getQuantity(), availableStock);
                throw new OutOfStockException(
                        "The order for product " + product.getName() + " by brand: " + product.getBrand()
                                + " exceeds the available stock by " + exceededAmount
                                + ". Please reduce the quantity to complete the purchase");
            }
        }
    }

    private Set<OrderItem> convertCartItemsToOrderItems(Set<CartItem> cartItems) {
        log.info("[convertCartItemsToOrderItems] Converting {} cart item(s) to order item(s)", cartItems.size());
        Set<OrderItem> items = new HashSet<OrderItem>();

        for (CartItem cartItem : cartItems) {
            OrderItem item = new OrderItem();
            Product product = cartItem.getProduct();
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());

            stockService.stockExit(product.getId(), cartItem.getQuantity());

            items.add(item);
        }

        return items;
    }

    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();

        dto.setProduct(productService.convertToDTO(item.getProduct()));
        dto.setQuantity(item.getQuantity());
        dto.setTotalPrice(item.getTotalPrice());

        return dto;
    }

    private OrderDTO convertOrderToDTO(Order order) {
        OrderDTO dto = new OrderDTO();

        dto.setDate(order.getDate());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setItems(order.getItems().stream()
                .map(item -> convertItemToDTO(item))
                .collect(Collectors.toSet()));

        return dto;
    }
}

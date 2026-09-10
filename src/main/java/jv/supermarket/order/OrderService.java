package jv.supermarket.order;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Order createOrder() {
        User user = userService.getLoggedUser();
        Cart cart = cartService.getCartById(user.getId());
        if (cart.getItems().isEmpty()) {
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

        return orderRepo.save(order);
    }

    @Transactional
    public OrderDTO getOrder(Long id) {
        User user = userService.getLoggedUser();
        Order order = getOrderById(id);

        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                return convertOrderToDTO(order);
            } else if (role.getName().equals("ROLE_CLIENTE")) {
                if (order.getUser().getId().equals(user.getId())) {
                    return convertOrderToDTO(order);
                }
            }
        }
        throw new AccessDeniedException("Order not found for this client");
    }

    public Page<OrderDTO> getOrdersByUser(Pageable pageable) {
        Page<Order> orders = orderRepo.findByUserId(userService.getLoggedUser().getId(), pageable);
        if (orders == null || orders.isEmpty()) {
            throw new ResourceNotFoundException("The user has no orders");
        }
        return orders.map(order -> convertOrderToDTO(order));
    }

    public Order getOrderById(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order cancelOrder(Long orderId) {
        User user = userService.getLoggedUser();
        Order order = getOrderById(orderId);

        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                order.setStatus(OrderStatus.CANCELLED);
                return orderRepo.save(order);
            } else if (role.getName().equals("ROLE_CLIENTE")) {
                if (order.getUser().getId().equals(user.getId())) {
                    order.setStatus(OrderStatus.CANCELLED);
                    return orderRepo.save(order);
                }
            }
        }
        throw new AccessDeniedException("Order not found for this client");
    }

    private void checkCartStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            int availableStock = stockService.getStockById(product.getId()).quantity();
            if (item.getQuantity() > availableStock) {
                int exceededAmount = item.getQuantity() - availableStock;
                throw new OutOfStockException(
                        "The order for product " + product.getName() + " by brand: " + product.getBrand()
                                + " exceeds the available stock by " + exceededAmount
                                + ". Please reduce the quantity to complete the purchase");
            }
        }
    }

    private Set<OrderItem> convertCartItemsToOrderItems(Set<CartItem> cartItems) {
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

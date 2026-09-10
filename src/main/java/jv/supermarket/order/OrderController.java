package jv.supermarket.order;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jv.supermarket.shared.ApiError;
import jv.supermarket.shared.Response;

@RestController
@RequestMapping("/supermarket/order")
public class OrderController {

    final OrderService orderService;
    
    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Creates an order", description = "Creates an order based on the products in the user's cart")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Order created successfully.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Order.class))),
        @ApiResponse(responseCode = "404",
            description = "Cart is empty. Add items first before placing an order.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404",
            description = "Stock exceeded. One of the requested products does not have enough stock.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<Order> createOrder() {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder());
    }

    @Operation(summary = "Finds an order by id", description = "Finds an order by id. If the user is not an Admin or the owner of the order, the request will be denied.")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Order found successfully.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = OrderDTO.class))),
        @ApiResponse(responseCode = "404",
            description = "Order not found. Check the order id.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403",
            description = "Request not allowed. User is not an admin or the order owner.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @Parameter(description = "Order id") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrder(id));
    }

    @Operation(summary = "Returns all orders of the logged-in user")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "User orders found successfully",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = OrderDTO.class)))),
        @ApiResponse(responseCode = "404",
            description = "User has no orders.",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("by-user")
    public ResponseEntity<Page<OrderDTO>> getOrdersByUser(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrdersByUser(pageable));
    }

    @Operation(summary = "Cancels an order")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Order cancelled successfully.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class))),
        @ApiResponse(responseCode = "404",
            description = "Order not found. Check the order id.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403",
            description = "Request not allowed. User is not an admin or the order owner.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Response> cancelOrder(
            @Parameter(description = "Order id") @PathVariable(name = "id") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response(Instant.now(), "Order cancelled successfully."));
    }
}

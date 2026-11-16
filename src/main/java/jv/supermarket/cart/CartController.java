package jv.supermarket.cart;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jv.supermarket.shared.ApiError;
import jv.supermarket.shared.Response;
import jv.supermarket.user.User;
import jv.supermarket.user.UserService;

@RestController
@RequestMapping("/supermarket/cart")
public class CartController {

    final CartService cartService;

    final CartItemService cartItemService;

    final UserService userService;

    CartController(CartService cartService, CartItemService cartItemService, UserService userService) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.userService = userService;
    }

    @Operation(summary = "Returns the logged-in User's cart")
    @ApiResponses({
        @ApiResponse(description = "User's cart returned successfully",
            responseCode = "200",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Cart.class))),
        @ApiResponse(responseCode = "404",
            description = "Cart not found. User must be a client to have a cart",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/show")
    public ResponseEntity<CartDTO> showCart() {
        User user = userService.getLoggedUser();

        return ResponseEntity.status(HttpStatus.OK).body(cartService.getCart(user.getId()));
    }

    @Operation(summary = "Adds an item to the cart", description = "Adds an item to the user's cart by Product id. If the product already exists in the cart, it only adds to the quantity.")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Item added successfully.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "404",
            description = "Item not found. Check the product id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "400",
            description = "Invalid quantity. Quantity must be a positive integer",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404",
            description = "Cart not found. User must be a client to have a cart",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/addItem/{itemId}")
    public ResponseEntity<Response> addItem(
            @Parameter(description = "Id of the product to be added") @PathVariable Long itemId,
            @Parameter(description = "Quantity of the product to be added") @RequestParam int quantity) {
        User user = userService.getLoggedUser();
        cartItemService.addItemToCart(itemId, quantity, user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(new Response(Instant.now(),
                "Item with id: " + itemId + " added to cart with id: " + user.getId() + " successfully."));
    }

    @Operation(summary = "Removes an item from the cart", description = "Removes an item from the user's cart by Product id")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Item removed successfully.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "404",
            description = "Item not found. Check the product id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404",
            description = "Cart not found. User must be a client to have a cart",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/removeItem/{itemId}")
    public ResponseEntity<Response> removeItem(@PathVariable Long itemId) {
        User user = userService.getLoggedUser();
        cartItemService.removeItemFromCart(user.getId(), itemId);
        return ResponseEntity.status(HttpStatus.OK).body(new Response(Instant.now(),
                "Item with id: " + itemId + " removed from cart with id: " + user.getId() + " successfully."));
    }

    @Operation(summary = "Updates the quantity of a cart item", description = "Updates the quantity of an item in the user's cart by Product id")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Item updated successfully.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "404",
            description = "Item not found. Check the product id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "400",
            description = "Invalid quantity. Quantity must be a positive integer",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404",
            description = "Cart not found. User must be a client to have a cart",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/item/{itemId}/update")
    public ResponseEntity<Response> updateItemQuantity(@PathVariable Long itemId, @RequestParam int quantity) {
        User user = userService.getLoggedUser();
        System.out.println("User ID: " + user.getId());
        cartItemService.updateItemQuantity(user.getId(), itemId, quantity);
        return ResponseEntity.status(HttpStatus.OK).body(new Response(Instant.now(),
                "Item with id: " + itemId + " updated in cart with id: " + user.getId() + " successfully."));
    }

    @Operation(summary = "Clears the User's cart")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Cart cleared successfully.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "404",
            description = "Cart not found. User must be a client to have a cart",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/clear")
    public ResponseEntity<Response> clearCart() {
        User user = userService.getLoggedUser();
        cartService.clearCart(user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response(Instant.now(), "Cart with id: " + user.getId() + " cleared successfully."));
    }

}

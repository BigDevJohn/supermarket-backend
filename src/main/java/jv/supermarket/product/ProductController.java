package jv.supermarket.product;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jv.supermarket.shared.ApiError;
import jv.supermarket.shared.Response;

@RestController
@RequestMapping("/supermarket/product")
public class ProductController {

    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Saves a new product", description = "Receives a product DTO with its category name(s) (the category must already exist). A product with the same name and brand cannot exist twice.")
    @ApiResponses({
            @ApiResponse(responseCode = "409", description = "Product with same name and brand already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "No category found with the given ids", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class)))
    })
    @PostMapping("/save")
    public ResponseEntity<ProductDTO> saveProduct(@RequestBody @Valid ProductRequestDTO product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.saveProduct(product));
    }

    @Operation(summary = "Finds a product by id")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "No product found with the given id", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "200", description = "Product found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "Product id. Example: 1") @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDTOById(id));
    }

    @Operation(summary = "Returns all products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found successfully", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/all")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @Operation(summary = "Finds products by name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No products found with the given name", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/by-name")
    public ResponseEntity<Page<ProductDTO>> getProductsByName(
            @Parameter(description = "Product name to search for") @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ProductDTO> products = productService.getProductsByName(name, pageable);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Finds products by brand")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No products found with the given brand", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/by-brand")
    public ResponseEntity<Page<ProductDTO>> getProductsByBrand(
            @Parameter(description = "Brand to search for") @RequestParam String brand,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ProductDTO> products = productService.getProductsByBrand(brand, pageable);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Finds a product by brand and name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "404", description = "No product found with the given brand and name", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/by-brand-and-name")
    public ResponseEntity<ProductDTO> getProductByBrandAndName(
            @Parameter(description = "Brand name") @RequestParam String brand,
            @Parameter(description = "Product name") @RequestParam String name) {
        return ResponseEntity.ok(productService.getProductByBrandAndName(brand, name));
    }

    @Operation(summary = "Finds products by category name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products found successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No products found in the given category", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/by-category-name")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategoryName(
            @Parameter(description = "Category name") @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ProductDTO> products = productService.getProductsByCategoryName(name, pageable);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Updates a product", description = "Updates a product by id with new information from the request body")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "404", description = "No product found with the given id", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,
            @RequestBody @Valid Product product) {
        return ResponseEntity.ok(productService.updateProduct(product, id));
    }

    @Operation(summary = "Deletes a product by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "No product found with the given id", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response(Instant.now(), "Product deleted successfully"));
    }

    @Operation(summary = "Makes a product available")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product made available successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "No product found with the given id", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Product was already available", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/available")
    public ResponseEntity<Response> makeProductAvailable(@PathVariable Long id) {
        productService.makeAvailable(id);
        return ResponseEntity.ok(new Response(Instant.now(), "Product made available successfully."));
    }

    @Operation(summary = "Makes a product unavailable")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product made unavailable successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "No product found with the given id", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Product was already unavailable", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/unavailable")
    public ResponseEntity<Response> makeProductUnavailable(@PathVariable Long id) {
        productService.makeUnavailable(id);
        return ResponseEntity.ok(new Response(Instant.now(), "Product made unavailable successfully."));
    }
}

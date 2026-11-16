package jv.supermarket.category;

import java.time.Instant;
import java.util.List;

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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jv.supermarket.shared.ApiError;
import jv.supermarket.shared.Response;

@RestController
@RequestMapping("/supermarket/category")
public class CategoryController {

    final CategoryService categoryService;

    CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Creates a category")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Category created successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "409",
            description = "A category with this name already exists",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/save")
    public ResponseEntity<Category> saveCategory(@RequestBody @Valid Category category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.saveCategory(category));
    }

    @Operation(summary = "Finds a category by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Category found successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "404",
            description = "No category found with this id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Finds a category by name")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Category found successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "404",
            description = "No category found with this name",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/by-name")
    public ResponseEntity<Category> getCategoryByName(
            @Parameter(description = "Name of the category to search for") @RequestParam String name) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getCategoryByName(name));
    }

    @Operation(summary = "Returns all categories")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Categories found successfully",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Category.class))))
    })
    @GetMapping("/all")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getAllCategories());
    }

    @Operation(summary = "Deletes a category", description = "Deletes a category by id. If a product only has this category, it is also deleted")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Category deleted successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "404",
            description = "No category found with this id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response(Instant.now(), "Category deleted successfully."));
    }

    @Operation(summary = "Updates a category", description = "Updates a category by id with new information from the request body")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Category updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "404",
            description = "No category found with this id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
            @RequestBody @Valid Category updatedCategory) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.updateCategory(id, updatedCategory));
    }

}

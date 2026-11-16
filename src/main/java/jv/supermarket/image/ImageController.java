package jv.supermarket.image;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
@RequestMapping("/supermarket/image")
public class ImageController {

    final ImageService imageService;

    ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @Operation(summary = "Uploads an image", description = "Uploads an image file and associates it with a product by its id")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Image uploaded successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ImageDTO.class))),
        @ApiResponse(responseCode = "404",
            description = "No product found with the given id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500",
            description = "Internal error while saving the images",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/upload")
    public ResponseEntity<List<ImageDTO>> uploadImage(@RequestParam List<MultipartFile> files,
            @RequestParam Long productId) {
        List<ImageDTO> savedImages = imageService.saveImages(productId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedImages);
    }

    @Operation(summary = "Returns images of a product", description = "Returns all images of a product by its id")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ImageDTO.class)))),
        @ApiResponse(responseCode = "404",
            description = "No product found with the given id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/by-product-id/{productId}")
    public ResponseEntity<List<ImageDTO>> getImagesByProduct(
            @Parameter(description = "Id of the product to fetch images for") @PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.OK).body(imageService.getImagesByProductId(productId));
    }

    @Operation(summary = "Downloads an image file", description = "Downloads the image file by its id")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Image downloaded successfully",
            content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "404",
            description = "No image found with the given id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadImage(
            @Parameter(description = "Id of the image") @PathVariable Long id) throws SQLException {
        Image image = imageService.getImageById(id);
        ByteArrayResource imageBytes = new ByteArrayResource(
                image.getImage().getBytes(1, (int) image.getImage().length()));

        //@formatter:off
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                .body(imageBytes);
        //@formatter:on
    }

    @Operation(summary = "Updates an image")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Image updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Image.class))),
        @ApiResponse(responseCode = "404",
            description = "No image found with the given id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Image> updateImage(
            @Parameter(description = "Id of the image to update") @PathVariable Long id,
            @RequestParam MultipartFile file) {
        imageService.updateImage(file, id);
        return ResponseEntity.status(HttpStatus.OK).body(imageService.getImageById(id));
    }

    @Operation(summary = "Deletes an image")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Image deleted successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "404",
            description = "No image found with the given id",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response(Instant.now(), "Image deleted successfully"));
    }

}

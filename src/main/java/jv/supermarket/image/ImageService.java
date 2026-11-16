package jv.supermarket.image;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialBlob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jv.supermarket.product.Product;
import jv.supermarket.product.ProductService;
import jv.supermarket.shared.customexception.ImageSavingException;
import jv.supermarket.shared.customexception.ResourceNotFoundException;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;

    private final ProductService productService;

    ImageService(ImageRepository imageRepository, ProductService productService) {
        this.imageRepository = imageRepository;
        this.productService = productService;
    }

    public Image getImageById(Long id) {
        log.info("[getImageById] Fetching image with ID: {}", id);
        return imageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[getImageById] No image found with id: {}", id);
                    return new ResourceNotFoundException("No image found with id: " + id);
                });
    }

    public List<ImageDTO> getImagesByProductId(Long productId) {
        log.info("[getImagesByProductId] Fetching images for product ID: {}", productId);
        List<Image> images = imageRepository.findByProductId(productId);
        if (images == null) {
            log.warn("[getImagesByProductId] No images found for product with id: {}", productId);
            throw new ResourceNotFoundException("No images found for product with id: " + productId);
        }
        log.info("[getImagesByProductId] Retrieved {} images for product ID: {}", images.size(), productId);
        return images.stream().map(image -> convertToDTO(image)).collect(Collectors.toList());
    }

    public List<ImageDTO> saveImages(Long productId, List<MultipartFile> files) {
        log.info("[saveImages] Saving {} image(s) for product ID: {}", files.size(), productId);
        Product product = productService.getProductById(productId);

        List<ImageDTO> savedImages = new ArrayList<ImageDTO>();
        for (MultipartFile file : files) {
            try {
                Image image = new Image();

                image.setFileName(file.getOriginalFilename());
                image.setFileType(file.getContentType());
                image.setImage(new SerialBlob(file.getBytes()));
                image.setProduct(product);

                Image savedImage = imageRepository.save(image);

                savedImage.setDownloadUrl("/supermarket/image/" + savedImage.getId() + "/download");
                imageRepository.save(savedImage);

                savedImages.add(convertToDTO(savedImage));
                log.info("[saveImages] Image '{}' saved successfully with ID: {}", file.getOriginalFilename(), savedImage.getId());
            } catch (IOException | SQLException e) {
                log.error("[saveImages] Error saving image '{}' for product ID: {}", file.getOriginalFilename(), productId, e);
                throw new ImageSavingException("Error saving the provided images");
            }
        }
        log.info("[saveImages] Successfully saved all {} image(s) for product ID: {}", savedImages.size(), productId);
        return savedImages;
    }

    public void updateImage(MultipartFile file, Long imageId) {
        log.info("[updateImage] Updating image with ID: {}", imageId);
        Image image = getImageById(imageId);
        try {
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setImage(new SerialBlob(file.getBytes()));
            imageRepository.save(image);
            log.info("[updateImage] Image with ID: {} updated successfully", imageId);
        } catch (IOException | SQLException e) {
            log.error("[updateImage] Error updating image with ID: {}", imageId, e);
            throw new ImageSavingException("Error updating the requested images");
        }
    }

    public void deleteImage(Long imageId) {
        log.info("[deleteImage] Attempting to delete image with ID: {}", imageId);
        if (imageRepository.existsById(imageId)) {
            imageRepository.deleteById(imageId);
            log.info("[deleteImage] Image with ID: {} deleted successfully", imageId);
        } else {
            log.warn("[deleteImage] Image with id: {} not found", imageId);
            throw new ResourceNotFoundException("Image with id: " + imageId + " not found");
        }
    }

    public ImageDTO convertToDTO(Image image) {
        ImageDTO dto = new ImageDTO();
        BeanUtils.copyProperties(image, dto);
        return dto;
    }
}

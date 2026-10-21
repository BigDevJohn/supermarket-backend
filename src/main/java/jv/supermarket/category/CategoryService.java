package jv.supermarket.category;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jv.supermarket.shared.customexception.AlreadyExistException;
import jv.supermarket.shared.customexception.CategoryNotEmptyException;
import jv.supermarket.shared.customexception.ResourceNotFoundException;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    final CategoryRepository categoryRepository;

    CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category saveCategory(Category category) {
        log.info("[saveCategory] Attempting to save category with name: {}", category.getName());
        if (categoryRepository.existsByName(category.getName())) {
            log.warn("[saveCategory] Category with name: {} already exists", category.getName());
            throw new AlreadyExistException("A category with the name: " + category.getName() + " already exists");
        }
        Category savedCategory = categoryRepository.save(category);
        log.info("[saveCategory] Category saved successfully with ID: {}", savedCategory.getId());
        return savedCategory;
    }

    public Category getCategoryById(Long id) {
        log.info("[getCategoryById] Fetching category with ID: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[getCategoryById] Category with id: {} not found", id);
                    return new ResourceNotFoundException("Category with id: " + id + " not found");
                });
    }

    public Category getCategoryByName(String name) {
        log.info("[getCategoryByName] Fetching category with name: {}", name);
        if (categoryRepository.existsByName(name)) {
            Category category = categoryRepository.findByName(name);
            log.info("[getCategoryByName] Found category with name: {}", name);
            return category;
        }
        log.warn("[getCategoryByName] Category with name: {} not found", name);
        throw new ResourceNotFoundException("Category with name: " + name + " not found");
    }

    public List<Category> getAllCategories() {
        log.info("[getAllCategories] Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        log.info("[getAllCategories] Retrieved {} categories", categories.size());
        return categories;
    }

    public void deleteCategoryById(Long id) {
        log.info("[deleteCategoryById] Attempting to delete category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[deleteCategoryById] Category with id: {} not found", id);
                    return new ResourceNotFoundException("Category with id: " + id + " not found");
                });

        if (!category.getProducts().isEmpty()) {
            log.warn("[deleteCategoryById] Category with ID: {} cannot be deleted because it still has associated products", id);
            throw new CategoryNotEmptyException(
                    "Category with id: " + id + " cannot be deleted because it still has associated products");
        }

        categoryRepository.deleteById(id);
        log.info("[deleteCategoryById] Category with ID: {} deleted successfully", id);
    }

    public Category updateCategory(Long id, Category updatedCategory) {
        log.info("[updateCategory] Updating category with ID: {}", id);
        Category category = getCategoryById(id);
        category.setName(updatedCategory.getName());

        if (updatedCategory.getProducts() != null) {
            log.info("[updateCategory] Adding products to category with ID: {}", id);
            category.getProducts().addAll(updatedCategory.getProducts());
        }

        Category saved = categoryRepository.save(category);
        log.info("[updateCategory] Category with ID: {} updated successfully", saved.getId());
        return saved;
    }

}

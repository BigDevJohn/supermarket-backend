package jv.supermarket.category;

import java.util.List;

import org.springframework.stereotype.Service;

import jv.supermarket.shared.customexception.AlreadyExistException;
import jv.supermarket.shared.customexception.CategoryNotEmptyException;
import jv.supermarket.shared.customexception.ResourceNotFoundException;

@Service
public class CategoryService {

    final CategoryRepository categoryRepository;

    CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category saveCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new AlreadyExistException("A category with the name: " + category.getName() + " already exists");
        }
        return categoryRepository.save(category);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id: " + id + " not found"));
    }

    public Category getCategoryByName(String name) {
        if (categoryRepository.existsByName(name)) {
            return categoryRepository.findByName(name);
        }
        throw new ResourceNotFoundException("Category with name: " + name + " not found");
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public void deleteCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id: " + id + " not found"));

        if (!category.getProducts().isEmpty()) {
            throw new CategoryNotEmptyException(
                    "Category with id: " + id + " cannot be deleted because it still has associated products");
        }

        categoryRepository.deleteById(id);
    }

    public Category updateCategory(Long id, Category updatedCategory) {
        Category category = getCategoryById(id);
        category.setName(updatedCategory.getName());

        if (updatedCategory.getProducts() != null) {
            category.getProducts().addAll(updatedCategory.getProducts());
        }

        return categoryRepository.save(category);
    }

}

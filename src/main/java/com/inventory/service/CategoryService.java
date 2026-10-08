package com.inventory.service;

import com.inventory.dto.CategoryRequest;
import com.inventory.dto.CategoryResponse;
import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ActivityLogService activityLogService;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository, ActivityLogService activityLogService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.activityLogService = activityLogService;
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category with name '" + name + "' already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(request.getDescription());
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            category.setStatus(request.getStatus().trim().toUpperCase());
        }

        Category saved = categoryRepository.save(category);
        activityLogService.log("CATEGORY_CREATE", "CATEGORIES", "Created category: " + saved.getName());
        return mapToResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String name = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(name) && categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category with name '" + name + "' already exists");
        }

        category.setName(name);
        category.setDescription(request.getDescription());
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            category.setStatus(request.getStatus().trim().toUpperCase());
        }

        Category updated = categoryRepository.save(category);
        activityLogService.log("CATEGORY_UPDATE", "CATEGORIES", "Updated category: " + updated.getName());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check foreign key dependencies
        List<Product> products = productRepository.findByCategoryId(id);
        if (!products.isEmpty()) {
            throw new DuplicateResourceException("Cannot delete category '" + category.getName() + "' because " + products.size() + " product(s) are associated with it");
        }

        categoryRepository.delete(category);
        activityLogService.log("CATEGORY_DELETE", "CATEGORIES", "Deleted category: " + category.getName());
    }

    public CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getStatus(),
                category.getCreatedAt()
        );
    }
}

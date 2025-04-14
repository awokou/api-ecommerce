package com.server.api.ecommerce.controller;

import com.server.api.ecommerce.config.AppConstants;
import com.server.api.ecommerce.dto.CategoryDto;
import com.server.api.ecommerce.dto.reponse.CategoryResponse;
import com.server.api.ecommerce.entity.Category;
import com.server.api.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/admin/category")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody Category category) {
        CategoryDto savedCategoryDTO = categoryService.createCategory(category);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
    }

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {

        CategoryResponse categoryResponse = categoryService.getCategories(pageNumber, pageSize, sortBy, sortOrder);

        return new ResponseEntity<>(categoryResponse, HttpStatus.FOUND);
    }

    @PutMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDto> updateCategory(@RequestBody Category category,
                                                      @PathVariable Long categoryId) {
        CategoryDto categoryDto = categoryService.updateCategory(category, categoryId);
        return new ResponseEntity<>(categoryDto, HttpStatus.OK);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        String status = categoryService.deleteCategory(categoryId);

        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}

package com.server.api.ecommerce.service;

import com.server.api.ecommerce.dto.CategoryDto;
import com.server.api.ecommerce.dto.reponse.CategoryResponse;
import com.server.api.ecommerce.entity.Category;

public interface CategoryService {
    CategoryDto createCategory(Category category);
    CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryDto updateCategory(Category category, Long categoryId);
    String deleteCategory(Long categoryId);
}

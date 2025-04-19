package com.server.api.ecommerce.service.impl;

import java.util.List;

import com.server.api.ecommerce.dto.CategoryDto;
import com.server.api.ecommerce.dto.reponse.CategoryResponse;
import com.server.api.ecommerce.entity.Category;
import com.server.api.ecommerce.entity.Product;
import com.server.api.ecommerce.exceptions.APIException;
import com.server.api.ecommerce.exceptions.ResourceNotFoundException;
import com.server.api.ecommerce.repository.CategoryRepository;
import com.server.api.ecommerce.service.CategoryService;
import com.server.api.ecommerce.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final ModelMapper modelMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductService productService, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
        this.modelMapper = modelMapper;
    }

    @Override
    public CategoryDto createCategory(Category category) {
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if (savedCategory != null) {
            throw new APIException("Category with the name '" + category.getCategoryName() + "' already exists !!!");
        }
        savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDto.class);
    }

    @Override
    public CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> pageCategories = categoryRepository.findAll(pageDetails);
        List<Category> categories = pageCategories.getContent();
        if (categories.isEmpty()) {
            throw new APIException("No category is created till now");
        }

        List<CategoryDto> categoryDTOs = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDto.class)).toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOs);
        categoryResponse.setPageNumber(pageCategories.getNumber());
        categoryResponse.setPageSize(pageCategories.getSize());
        categoryResponse.setTotalElements(pageCategories.getTotalElements());
        categoryResponse.setTotalPages(pageCategories.getTotalPages());
        categoryResponse.setLastPage(pageCategories.isLast());

        return categoryResponse;
    }

    @Override
    public CategoryDto updateCategory(Category category, Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        category.setId(categoryId);
        return modelMapper.map(categoryRepository.save(category), CategoryDto.class);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        List<Product> products = category.getProducts();
        products.forEach(product -> productService.deleteProduct(product.getId()));
        categoryRepository.delete(category);
        return "Category with categoryId: " + categoryId + " deleted successfully !!!";
    }
}

package com.server.api.ecommerce.service;

import com.server.api.ecommerce.dto.ProductDto;
import com.server.api.ecommerce.dto.reponse.ProductResponse;
import com.server.api.ecommerce.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductDto addProduct(Long categoryId, Product product);
    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ProductDto updateProduct(Long productId, Product product);
    ProductDto updateProductImage(Long productId, MultipartFile image) throws IOException;
    ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    String deleteProduct(Long productId);
}

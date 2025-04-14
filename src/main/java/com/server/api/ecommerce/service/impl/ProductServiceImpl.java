package com.server.api.ecommerce.service.impl;

import com.server.api.ecommerce.dto.CartDto;
import com.server.api.ecommerce.dto.ProductDto;
import com.server.api.ecommerce.dto.reponse.ProductResponse;
import com.server.api.ecommerce.entity.Cart;
import com.server.api.ecommerce.entity.Category;
import com.server.api.ecommerce.entity.Product;
import com.server.api.ecommerce.exceptions.APIException;
import com.server.api.ecommerce.exceptions.ResourceNotFoundException;
import com.server.api.ecommerce.repository.CartRepository;
import com.server.api.ecommerce.repository.CategoryRepository;
import com.server.api.ecommerce.repository.ProductRepository;
import com.server.api.ecommerce.service.CartService;
import com.server.api.ecommerce.service.FileService;
import com.server.api.ecommerce.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@Transactional
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final FileService fileService;
    private final ModelMapper modelMapper;

    @Value("${project.image}")
    private String path;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, CartRepository cartRepository, CartService cartService, FileService fileService, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.fileService = fileService;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductDto addProduct(Long categoryId, Product product) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        boolean isProductNotPresent = true;
        List<Product> products = category.getProducts();
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductName().equals(product.getProductName())
                    && products.get(i).getDescription().equals(product.getDescription())) {
                isProductNotPresent = false;
                break;
            }
        }

        if (isProductNotPresent) {
            product.setImage("default.png");
            product.setCategory(category);
            double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);

            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDto.class);
        } else {
            throw new APIException("Product already exists !!!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> pageProducts = productRepository.findAll(pageDetails);
        List<Product> products = pageProducts.getContent();
        List<ProductDto> productDtos = products.stream().map(product -> modelMapper.map(product, ProductDto.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDtos);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> pageProducts = productRepository.findAll(pageDetails);
        List<Product> products = pageProducts.getContent();

        if (products.isEmpty()) {
            throw new APIException(category.getCategoryName() + " category doesn't contain any products !!!");
        }

        List<ProductDto> productDTOs = products.stream().map(p -> modelMapper.map(p, ProductDto.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOs);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    @Override
    public ProductDto updateProduct(Long productId, Product product) {
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (productFromDB == null) {
            throw new APIException("Product not found with productId: " + productId);
        }
        product.setImage(productFromDB.getImage());
        product.setProductId(productId);
        product.setCategory(productFromDB.getCategory());
        double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
        product.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(product);
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        List<CartDto> cartDTOs = carts.stream().map(cart -> {
            CartDto cartDTO = modelMapper.map(cart, CartDto.class);
            List<ProductDto> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDto.class)).toList();
            cartDTO.setProducts(products);
            return cartDTO;

        }).toList();
        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getId(), productId));

        return modelMapper.map(savedProduct, ProductDto.class);
    }

    @Override
    public ProductDto updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (productFromDB == null) {
            throw new APIException("Product not found with productId: " + productId);
        }

        String fileName = fileService.uploadImage(path, image);
        productFromDB.setImage(fileName);
        Product updatedProduct = productRepository.save(productFromDB);
        return modelMapper.map(updatedProduct, ProductDto.class);
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByProductNameLike(keyword, pageDetails);
        List<Product> products = pageProducts.getContent();
        if (products.isEmpty()) {
            throw new APIException("Products not found with keyword: " + keyword);
        }

        List<ProductDto> productDtos = products.stream().map(p -> modelMapper.map(p, ProductDto.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDtos);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    @Override
    public String deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));
        productRepository.delete(product);
        return "Product with productId: " + productId + " deleted successfully !!!";
    }
}

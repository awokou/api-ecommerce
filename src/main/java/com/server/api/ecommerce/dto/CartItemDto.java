package com.server.api.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long id;
    private CartDto cart;
    private ProductDto product;
    private Integer quantity;
    private double discount;
    private double productPrice;
}

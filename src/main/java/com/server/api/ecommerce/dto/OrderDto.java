package com.server.api.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String email;
    private List<OrderItemDto> orderItems = new ArrayList<>();
    private LocalDate orderDate;
    private PaymentDto payment;
    private Double totalAmount;
    private String orderStatus;
}

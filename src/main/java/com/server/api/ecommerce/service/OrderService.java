package com.server.api.ecommerce.service;

import com.server.api.ecommerce.dto.OrderDto;
import com.server.api.ecommerce.dto.reponse.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderDto placeOrder(String emailId, Long cartId, String paymentMethod);
    OrderDto getOrder(String emailId, Long orderId);
    List<OrderDto> getOrdersByUser(String emailId);
    OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    OrderDto updateOrder(String emailId, Long orderId, String orderStatus);
}

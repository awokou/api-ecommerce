package com.server.api.ecommerce.controller;

import com.server.api.ecommerce.config.AppConstants;
import com.server.api.ecommerce.dto.OrderDto;
import com.server.api.ecommerce.dto.reponse.OrderResponse;
import com.server.api.ecommerce.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    public final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/public/users/{emailId}/carts/{cartId}/payments/{paymentMethod}/order")
    public ResponseEntity<OrderDto> orderProducts(@PathVariable String emailId, @PathVariable Long cartId, @PathVariable String paymentMethod) {
        OrderDto order = orderService.placeOrder(emailId, cartId, paymentMethod);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<OrderResponse> getAllOrders(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {

        OrderResponse orderResponse = orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(orderResponse, HttpStatus.FOUND);
    }

    @GetMapping("public/users/{emailId}/orders")
    public ResponseEntity<List<OrderDto>> getOrdersByUser(@PathVariable String emailId) {
        List<OrderDto> orders = orderService.getOrdersByUser(emailId);
        return new ResponseEntity<>(orders, HttpStatus.FOUND);
    }

    @GetMapping("public/users/{emailId}/orders/{orderId}")
    public ResponseEntity<OrderDto> getOrderByUser(@PathVariable String emailId, @PathVariable Long orderId) {
        OrderDto order = orderService.getOrder(emailId, orderId);
        return new ResponseEntity<>(order, HttpStatus.FOUND);
    }

    @PutMapping("admin/users/{emailId}/orders/{orderId}/orderStatus/{orderStatus}")
    public ResponseEntity<OrderDto> updateOrderByUser(@PathVariable String emailId, @PathVariable Long orderId, @PathVariable String orderStatus) {
        OrderDto order = orderService.updateOrder(emailId, orderId, orderStatus);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}

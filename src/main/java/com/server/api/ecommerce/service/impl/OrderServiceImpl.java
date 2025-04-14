package com.server.api.ecommerce.service.impl;

import com.server.api.ecommerce.dto.OrderDto;
import com.server.api.ecommerce.dto.OrderItemDto;
import com.server.api.ecommerce.dto.reponse.OrderResponse;
import com.server.api.ecommerce.entity.*;
import com.server.api.ecommerce.exceptions.APIException;
import com.server.api.ecommerce.exceptions.ResourceNotFoundException;
import com.server.api.ecommerce.repository.*;
import com.server.api.ecommerce.service.CartService;
import com.server.api.ecommerce.service.OrderService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;

    public OrderServiceImpl(CartRepository cartRepository, OrderRepository orderRepository, PaymentRepository paymentRepository, OrderItemRepository orderItemRepository, CartService cartService, ModelMapper modelMapper) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.modelMapper = modelMapper;
    }

    @Override
    public OrderDto placeOrder(String emailId, Long cartId, String paymentMethod) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);

        payment = paymentRepository.save(payment);

        order.setPayment(payment);
        Order savedOrder = orderRepository.save(order);

        List<CartItem> cartItems = cart.getCartItems();

        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);

        cart.getCartItems().forEach(item -> {
            int quantity = item.getQuantity();
            Product product = item.getProduct();
            cartService.deleteProductFromCart(cartId, item.getProduct().getProductId());
            product.setQuantity(product.getQuantity() - quantity);
        });

        OrderDto orderDTO = modelMapper.map(savedOrder, OrderDto.class);
        orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDto.class)));

        return orderDTO;
    }

    @Override
    public OrderDto getOrder(String emailId, Long orderId) {
        Order order = orderRepository.findOrderByEmailAndOrderId(emailId, orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order", "orderId", orderId);
        }
        return modelMapper.map(order, OrderDto.class);
    }

    @Override
    public List<OrderDto> getOrdersByUser(String emailId) {
        List<Order> orders = orderRepository.findAllByEmail(emailId);
        List<OrderDto> orderDtos = orders.stream().map(order -> modelMapper.map(order, OrderDto.class))
                .toList();
        if (orderDtos.isEmpty()) {
            throw new APIException("No orders placed yet by the user with email: " + emailId);
        }

        return orderDtos;
    }

    @Override
    public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Order> pageOrders = orderRepository.findAll(pageDetails);
        List<Order> orders = pageOrders.getContent();
        List<OrderDto> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDto.class))
                .toList();
        if (orderDTOs.isEmpty()) {
            throw new APIException("No orders placed yet by the users");
        }

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setContent(orderDTOs);
        orderResponse.setPageNumber(pageOrders.getNumber());
        orderResponse.setPageSize(pageOrders.getSize());
        orderResponse.setTotalElements(pageOrders.getTotalElements());
        orderResponse.setTotalPages(pageOrders.getTotalPages());
        orderResponse.setLastPage(pageOrders.isLast());

        return orderResponse;
    }

    @Override
    public OrderDto updateOrder(String emailId, Long orderId, String orderStatus) {
        Order order = orderRepository.findOrderByEmailAndOrderId(emailId, orderId);

        if (order == null) {
            throw new ResourceNotFoundException("Order", "orderId", orderId);
        }

        order.setOrderStatus(orderStatus);

        return modelMapper.map(order, OrderDto.class);
    }
}

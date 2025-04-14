package com.server.api.ecommerce.repository;

import com.server.api.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.email = ?1 AND o.id = ?2")
    Order findOrderByEmailAndOrderId(String email, Long cartId);
    List<Order> findAllByEmail(String emailId);
}

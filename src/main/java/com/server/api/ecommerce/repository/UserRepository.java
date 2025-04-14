package com.server.api.ecommerce.repository;

import com.server.api.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.addresses a WHERE a.id = ?1")
    List<User> findByAddress(Long addressId);
    Optional<User> findByEmail(String email);
}

package com.example.FoodHub.repository;

import com.example.FoodHub.entity.Cart;
import com.example.FoodHub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    List<Cart> findByUser(User user);
    Optional<Cart> findByUserAndMenuItemId(User user, Integer menuItemId);
}
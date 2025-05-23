package com.example.FoodHub.repository;

import com.example.FoodHub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User save(User user);
    boolean existsByUsername(String username);
    List<User> findAll();
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}

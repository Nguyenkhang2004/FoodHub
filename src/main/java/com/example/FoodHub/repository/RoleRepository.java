package com.example.FoodHub.repository;

import com.example.FoodHub.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    boolean existsByName(String name);
    Role save(Role role);
    void deleteByName(String name);
    Optional<Role> findById(String id);
}

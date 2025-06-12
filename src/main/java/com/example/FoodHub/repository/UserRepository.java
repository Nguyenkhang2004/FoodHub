package com.example.FoodHub.repository;

import com.example.FoodHub.entity.User;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(@Email @NotBlank @Size(max = 255) String email);
    List<User> findAll();
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.roleName.name != 'CUSTOMER' " +
            "AND (:role IS NULL OR u.roleName.name = :role) " +
            "AND (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> findEmployees(
            @Param("role") String role,
            @Param("keyword") String keyword,
            Pageable pageable);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    int updateStatusById(@Param("id") Integer id, @Param("status") String status);
    List<User> findByRoleName_Name(String roleName);
    @Query("SELECT COUNT(m) > 0 FROM User m WHERE LOWER(TRIM(m.username)) = LOWER(TRIM(:username)) AND m.id != :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("username") String username, @Param("id") Integer id);
    List<User> findByRoleName_NameAndStatus(String roleName, String status);


}

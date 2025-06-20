package com.example.FoodHub.repository;

import com.example.FoodHub.dto.response.CategoryResponse;
import com.example.FoodHub.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
     @Query("SELECT mi.name, SUM(oi.quantity), SUM(oi.quantity * oi.price) " +
            "FROM OrderItem oi " +
            "JOIN oi.menuItem mi " +
            "JOIN oi.order ro " +
            "WHERE oi.status = 'COMPLETED' AND ro.status = 'COMPLETED' AND ro.createdAt BETWEEN :start AND :end " +
            "GROUP BY mi.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopDishesByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT mi.name, SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.menuItem mi " +
            "JOIN oi.order o " +
            "WHERE oi.status = 'COMPLETED' " +
            "AND o.status = 'COMPLETED' " +
            "AND o.createdAt BETWEEN :start AND :end " +
            "AND (:categoryId IS NULL OR EXISTS (" +
            "    SELECT 1 FROM mi.categories c WHERE c.id = :categoryId)) " +
            "GROUP BY mi.id, mi.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findQuantityByMenuItem(@Param("start") Instant start,
                                          @Param("end") Instant end,
                                          @Param("categoryId") Integer categoryId);

    // Lấy danh sách tất cả danh mục (hỗ trợ dropdown #categoryFilter)
    @Query("SELECT new com.example.FoodHub.dto.response.CategoryResponse(c) " +
            "FROM Category c")
    List<CategoryResponse> findAllCategories();

    @Query("SELECT c.name, SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.menuItem mi " +
            "JOIN mi.categories c " +
            "JOIN oi.order o " +
            "WHERE oi.status = 'COMPLETED' " +
            "AND o.status = 'COMPLETED' " +
            "AND o.createdAt BETWEEN :start AND :end " +
            "AND (:categoryId IS NULL OR c.id = :categoryId) " +
            "GROUP BY c.id, c.name")
    List<Object[]> findQuantityByCategory(@Param("start") Instant start,
                                          @Param("end") Instant end,
                                          @Param("categoryId") Integer categoryId);

    List<OrderItem> findByOrderId(Integer orderId);
}
package com.example.FoodHub.repository;

import com.example.FoodHub.entity.MenuItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page; // Đúng package cho Page
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> , JpaSpecificationExecutor<MenuItem> {

//    @Query("SELECT m FROM MenuItem m JOIN m.categories c WHERE c.id = :categoryId " +
////           "AND(:Keyword IS NULL OR LOWER (m.name) Like LOWER(CONCAT('%',:keyword,'%')))" +
//            "ORDER BY CASE WHEN :sortField = 'name' AND :isAsc = true THEN m.name END ASC, " +
//            "CASE WHEN :sortField = 'name' AND :isAsc = false THEN m.name END DESC, " +
//            "CASE WHEN :sortField = 'price' AND :isAsc = true THEN m.price END ASC, " +
//            "CASE WHEN :sortField = 'price' AND :isAsc = false THEN m.price END DESC")
//    Page<MenuItem> findByCategoryIdOrderBy(@Param("sortField") String sortField,
//                                           @Param("isAsc") boolean isAsc,
//                                           @Param("categoryId") Integer categoryId,
//                                           @Param("keyword") String keyword,
//                                           Pageable pageable);
//
//    @Query("SELECT m FROM MenuItem m " +
////            "WHERE (:keyword IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
//            "ORDER BY CASE WHEN :sortField = 'name' AND :isAsc = true THEN m.name END ASC, " +
//            "CASE WHEN :sortField = 'name' AND :isAsc = false THEN m.name END DESC, " +
//            "CASE WHEN :sortField = 'price' AND :isAsc = true THEN m.price END ASC, " +
//            "CASE WHEN :sortField = 'price' AND :isAsc = false THEN m.price END DESC")
//    Page<MenuItem> findAllOrderBy(@Param("sortField") String sortField,
//                                  @Param("isAsc") boolean isAsc,
//                                  @Param("keyword") String keyword,
//                                  Pageable pageable);
@Query("SELECT m FROM MenuItem m " +
        "LEFT JOIN m.categories c ON (:categoryId IS NULL OR c.id = :categoryId) " +
        "WHERE (:categoryId IS NULL OR c.id IS NOT NULL) " +
        "AND (:keyword IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
        "ORDER BY " +
        "CASE WHEN :sortField = 'name' AND :isAsc = true THEN m.name END ASC, " +
        "CASE WHEN :sortField = 'name' AND :isAsc = false THEN m.name END DESC, " +
        "CASE WHEN :sortField = 'price' AND :isAsc = true THEN m.price END ASC, " +
        "CASE WHEN :sortField = 'price' AND :isAsc = false THEN m.price END DESC, " +
        "m.id ASC")
Page<MenuItem> findMenuItems(
        @Param("sortField") String sortField,
        @Param("isAsc") boolean isAsc,
        @Param("categoryId") Integer categoryId,
        @Param("keyword") String keyword,
        Pageable pageable
);
    @Query("SELECT COUNT(m) > 0 FROM MenuItem m WHERE LOWER(TRIM(m.name)) = LOWER(TRIM(:name))")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT COUNT(m) > 0 FROM MenuItem m WHERE LOWER(TRIM(m.name)) = LOWER(TRIM(:name)) AND m.id != :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE MenuItem m SET m.status = :status WHERE m.id = :id")
    int updateStatusById(@Param("id") Integer id, @Param("status") String status);

    // Method để check tồn tại
    boolean existsById(Integer id);

    

    // --------------------------
    // 🏆 Món đắt nhất / rẻ nhất
    // --------------------------
    MenuItem findTopByOrderByPriceDesc();
    MenuItem findTopByOrderByPriceAsc();

    // --------------------------
    // 🥗 Theo trạng thái (chay, available...)
    // --------------------------
    MenuItem findTopByStatusIgnoreCaseOrderByPriceDesc(String status);
    MenuItem findTopByStatusIgnoreCaseOrderByPriceAsc(String status);
    List<MenuItem> findByStatusIgnoreCase(String status);
    List<MenuItem> findByStatusIgnoreCaseAndPriceLessThanEqual(String status, Integer price);

    // --------------------------
    // 🍲 Theo danh mục (category name)
    // --------------------------
    MenuItem findTopByCategories_NameIgnoreCaseOrderByPriceDesc(String category);
    MenuItem findTopByCategories_NameIgnoreCaseOrderByPriceAsc(String category);
    List<MenuItem> findByCategories_NameIgnoreCase(String category);
    List<MenuItem> findByCategories_NameIgnoreCaseAndPriceLessThanEqual(String category, Integer budget);

    // --------------------------
    // 💰 Theo giá
    // --------------------------
    List<MenuItem> findByPriceLessThanEqual(Integer budget);
    List<MenuItem> findByPriceGreaterThanEqual(Integer price);
    MenuItem findTopByPriceLessThanEqualOrderByPriceDesc(Integer budget);
    MenuItem findTopByPriceLessThanEqualOrderByPriceAsc(Integer budget);

    // --------------------------
    // 🔤 Theo tên món
    // --------------------------
    List<MenuItem> findByNameContainingIgnoreCase(String keyword);
    List<MenuItem> findByNameStartingWithIgnoreCase(String prefix);
    List<MenuItem> findByNameEndingWithIgnoreCase(String suffix);

    // --------------------------
    // 🧩 Kết hợp nhiều điều kiện
    // --------------------------
    List<MenuItem> findByStatusIgnoreCaseAndCategories_NameIgnoreCase(String status, String category);
    MenuItem findTopByStatusIgnoreCaseAndCategories_NameIgnoreCaseOrderByPriceAsc(String status, String category);
    MenuItem findTopByStatusIgnoreCaseAndCategories_NameIgnoreCaseOrderByPriceDesc(String status, String category);
    List<MenuItem> findByStatusIgnoreCaseAndPriceLessThanEqualAndCategories_NameIgnoreCase(String status, Integer budget, String category);

    // --------------------------
    // ⏱️ Theo thời gian (nếu entity có trường createdAt / updatedAt)
    // --------------------------
    List<MenuItem> findByCreatedAtAfter(LocalDateTime time);
    List<MenuItem> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    MenuItem findTopByOrderByCreatedAtDesc();

    // --------------------------
    // 📊 Tồn tại và đếm
    // --------------------------
    long countByStatusIgnoreCase(String status);
    long countByCategories_NameIgnoreCase(String category);
}
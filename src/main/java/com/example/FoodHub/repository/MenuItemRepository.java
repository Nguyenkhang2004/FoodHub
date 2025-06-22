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

import java.math.BigDecimal;
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



    // 🔹 Món đắt nhất và rẻ nhất (tổng thể)
    MenuItem findTopByOrderByPriceDesc();
    MenuItem findTopByOrderByPriceAsc();

    // 🔹 Theo trạng thái (VD: VEGETARIAN, AVAILABLE)
    MenuItem findTopByStatusIgnoreCaseOrderByPriceDesc(String status);
    MenuItem findTopByStatusIgnoreCaseOrderByPriceAsc(String status);
    List<MenuItem> findByStatusIgnoreCase(String status);
    List<MenuItem> findByStatusIgnoreCaseAndPriceLessThanEqual(String status, Integer price);

    // 🔹 Theo tên danh mục (VD: 'Lẩu nước', 'Món nướng thịt'...)
    MenuItem findTopByCategories_NameIgnoreCaseOrderByPriceDesc(String categoryName);
    MenuItem findTopByCategories_NameIgnoreCaseOrderByPriceAsc(String categoryName);
    List<MenuItem> findByCategories_NameIgnoreCase(String categoryName);
    List<MenuItem> findByCategories_NameIgnoreCaseAndPriceLessThanEqual(String categoryName, Integer price);

    // 🔹 Theo giá
    List<MenuItem> findByPriceLessThanEqual(Integer price);
    List<MenuItem> findByPriceGreaterThanEqual(Integer price);
    MenuItem findTopByPriceLessThanEqualOrderByPriceDesc(Integer price);
    MenuItem findTopByPriceLessThanEqualOrderByPriceAsc(Integer price);

    // 🔹 Theo tên
    List<MenuItem> findByNameContainingIgnoreCase(String keyword);
    List<MenuItem> findByNameStartingWithIgnoreCase(String prefix);
    List<MenuItem> findByNameEndingWithIgnoreCase(String suffix);

    // 🔹 Kết hợp danh mục + trạng thái
    List<MenuItem> findByStatusIgnoreCaseAndCategories_NameIgnoreCase(String status, String categoryName);
    MenuItem findTopByStatusIgnoreCaseAndCategories_NameIgnoreCaseOrderByPriceDesc(String status, String categoryName);
    MenuItem findTopByStatusIgnoreCaseAndCategories_NameIgnoreCaseOrderByPriceAsc(String status, String categoryName);

    // 🔹 Kết hợp cả status + category + ngân sách
    List<MenuItem> findByStatusIgnoreCaseAndPriceLessThanEqualAndCategories_NameIgnoreCase(String status, Integer price, String categoryName);


}
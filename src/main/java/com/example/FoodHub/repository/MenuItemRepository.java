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


public interface MenuItemRepository extends JpaRepository<MenuItem, Integer>, JpaSpecificationExecutor<MenuItem> {

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

//@Query("SELECT m FROM MenuItem m " +
//        "LEFT JOIN m.categories c ON (:categoryId IS NULL OR c.id = :categoryId) " +
//        "WHERE (:categoryId IS NULL OR c.id IS NOT NULL) " +
//        "AND (:keyword IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
//        "AND (:status IS NULL OR m.status = :status) " +
//        "ORDER BY " +
//        "CASE WHEN :sortField = 'name' AND :isAsc = true THEN m.name END ASC, " +
//        "CASE WHEN :sortField = 'name' AND :isAsc = false THEN m.name END DESC, " +
//        "CASE WHEN :sortField = 'price' AND :isAsc = true THEN m.price END ASC, " +
//        "CASE WHEN :sortField = 'price' AND :isAsc = false THEN m.price END DESC, " +
//        "CASE WHEN :sortField = 'status' AND :isAsc = true THEN m.status END ASC, " +
//        "CASE WHEN :sortField = 'status' AND :isAsc = false THEN m.status END DESC, " +
//        "m.id ASC")
//Page<MenuItem> findMenuItems(
//        @Param("sortField") String sortField,
//        @Param("isAsc") boolean isAsc,
//        @Param("categoryId") Integer categoryId,
//        @Param("keyword") String keyword,
//        @Param("status") String status,
//        Pageable pageable
//);
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
}
package com.example.FoodHub.controller;

import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.CategoryResponse;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();

        ApiResponse<List<CategoryResponse>> response = ApiResponse.<List<CategoryResponse>>builder()
                .code(1000)
                .message("Lấy danh sách danh mục thành công")
                .result(categories)
                .build();

        return ResponseEntity.ok().body(response);
    }
}
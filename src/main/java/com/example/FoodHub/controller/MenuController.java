package com.example.FoodHub.controller;

import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.service.MenuService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuController {
    MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getMenu(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<MenuItemResponse> menuItems = menuService.getAllMenuItems(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<MenuItemResponse>>builder()
                .result(menuItems)
                .build());
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getMenuByCategory(
            @RequestParam("name") String categoryName,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MenuItemResponse> menuItems = menuService.getMenuItemsByCategory(categoryName, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<MenuItemResponse>>builder()
                .result(menuItems)
                .build());
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getAvailableMenuItems(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MenuItemResponse> menuItems = menuService.getAvailableMenuItems(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<MenuItemResponse>>builder()
                .result(menuItems)
                .build());
    }


}

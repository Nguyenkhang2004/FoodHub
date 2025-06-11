package com.example.FoodHub.controller;

import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.service.MenuService;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

//@.0.(origins = "http://127.0.0.1:5500")
@Slf4j
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class MenuController {
    MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getMenu(
            @PageableDefault(size = 10, page = 0, sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching all menu items with pageable: {}", pageable);
        Page<MenuItemResponse> menuItems = menuService.getAllMenuItems(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<MenuItemResponse>>builder()
                .result(menuItems)
                .message(menuItems.isEmpty() ? "No menu items found" : null)
                .build());
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getMenuByCategory(
            @RequestParam("name") @NotBlank String categoryName,
            @PageableDefault(size = 10, sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching menu items for category: {}", categoryName);
        Page<MenuItemResponse> menuItems = menuService.getMenuItemsByCategory(categoryName, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<MenuItemResponse>>builder()
                .result(menuItems)
                .message(menuItems.isEmpty() ? "No items found for category: " + categoryName : null)
                .build());
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getAvailableMenuItems(
            @PageableDefault(size = 10, sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching available menu items: {}", pageable);
        Page<MenuItemResponse> menuItems = menuService.getAvailableMenuItems(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<MenuItemResponse>>builder()
                .result(menuItems)
                .message(menuItems.isEmpty() ? "No available items found" : null)
                .build());
    }
}
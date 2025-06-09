package com.example.FoodHub.controller;

import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.service.MenuService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenu() {
        List<MenuItemResponse> menuItems = menuService.getAllMenuItems();
        ApiResponse<List<MenuItemResponse>> response = ApiResponse.<List<MenuItemResponse>>builder()
                .result(menuItems)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuByCategory(@RequestParam("name") String categoryName) {
        List<MenuItemResponse> menuItems = menuService.getMenuItemsByCategory(categoryName);
        ApiResponse<List<MenuItemResponse>> response = ApiResponse.<List<MenuItemResponse>>builder()
                .result(menuItems)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getAvailableMenuItems() {
        List<MenuItemResponse> menuItems = menuService.getAvailableMenuItems();
        ApiResponse<List<MenuItemResponse>> response = ApiResponse.<List<MenuItemResponse>>builder()
                .result(menuItems)
                .build();
        return ResponseEntity.ok().body(response);
    }


}

package com.example.FoodHub.controller;

import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.service.MenuService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

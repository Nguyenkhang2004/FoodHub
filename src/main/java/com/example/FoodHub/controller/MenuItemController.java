package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.MenuItemRequest;
import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @Autowired
    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public ResponseEntity<Page<MenuItemResponse>> getMenuItems(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MenuItemResponse> menuItems = menuItemService.getMenuItems(categoryId, keyword, sortBy, sortDirection, page, size);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Integer id) {
        MenuItemResponse menuItem = menuItemService.getMenuItemById(id);
        return ResponseEntity.ok(menuItem);
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest request) throws IOException {
        MenuItemResponse createdItem = menuItemService.createMenuItem(request);
        return ResponseEntity.status(201).body(createdItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Integer id, @Valid @RequestBody MenuItemRequest request) throws IOException {
        MenuItemResponse updatedItem = menuItemService.updateMenuItem(id, request);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Integer id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreMenuItem(@PathVariable Integer id) {
        menuItemService.restoreMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countMenuItems() {
        long count = menuItemService.countMenuItems();
        return ResponseEntity.ok(count);
    }


}
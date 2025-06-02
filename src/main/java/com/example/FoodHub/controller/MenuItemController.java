package com.example.FoodHub.controller;


import com.example.FoodHub.dto.request.MenuItemRequest;
import com.example.FoodHub.dto.response.MenuItemreponse;
import com.example.FoodHub.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // Import từ Spring Data
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/menu-items")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

//    @GetMapping
//    public ResponseEntity<Page<MenuItemreponse>> getMenuItems(
//            @RequestParam(required = false) Integer categoryId,
//            @RequestParam(required = false) String keyword,
//            @RequestParam(defaultValue = "name") String sortBy,
//            @RequestParam(defaultValue = "asc") String sortDirection,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        Page<MenuItemreponse> menuItems = menuItemService.getMenuItems(categoryId, keyword, sortBy, sortDirection, page,size);
//        return ResponseEntity.ok(menuItems);
//    }
@GetMapping
public ResponseEntity<Page<MenuItemreponse>> getMenuItems(
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDirection,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    Page<MenuItemreponse> menuItems = menuItemService.getMenuItems(categoryId, keyword, sortBy, sortDirection, page, size);
    return ResponseEntity.ok(menuItems);
}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMenuItem(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        boolean success = menuItemService.deleteMenuItem(id);

        if (success) {
            response.put("success", true);
            response.put("message", "Món ăn đã được xóa thành công (chuyển sang UNAVAILABLE)");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Không thể xóa món ăn. Vui lòng thử lại!");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreMenuItem(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        boolean success = menuItemService.restoreMenuItem(id);

        if (success) {
            response.put("success", true);
            response.put("message", "Món ăn đã được khôi phục thành công (chuyển sang AVAILABLE)");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Không thể khôi phục món ăn. Vui lòng thử lại!");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemreponse> getMenuItemById(@PathVariable Integer id) {
        MenuItemreponse menuItem = menuItemService.getMenuItemById(id);

        if (menuItem != null) {
            return ResponseEntity.ok(menuItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            MenuItemreponse createdItem = menuItemService.createMenuItem(request);
            response.put("success", true);
            response.put("message", "Tạo món ăn thành công!");
            response.put("data", createdItem);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể tạo món ăn. Vui lòng thử lại! " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMenuItem(
            @PathVariable Integer id,
            @Valid @RequestBody MenuItemRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            MenuItemreponse updatedItem = menuItemService.updateMenuItem(id, request);
            response.put("success", true);
            response.put("message", "Cập nhật món ăn thành công!");
            response.put("data", updatedItem);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể cập nhật món ăn. Vui lòng thử lại! " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
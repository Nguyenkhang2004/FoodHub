package com.example.FoodHub.controller;

import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.dto.request.MenuItemRequest;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.io.IOException;

@RestController
@RequestMapping("/menu-items")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://127.0.0.1:5500") // Cho phép CORS nếu cần (tùy môi trường)

public class MenuItemController {

    private final MenuItemService menuItemService;

    //    @GetMapping
//    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getMenuItems(
//            @RequestParam(required = false) Integer categoryId,
//            @RequestParam(required = false) String keyword,
//            @RequestParam(defaultValue = "name") String sortBy,
//            @RequestParam(defaultValue = "asc") String sortDirection,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        if (page < 0) {
//            throw new AppException(ErrorCode.INVALID_KEY);
//        }
//
//        Page<MenuItemResponse> menuItems = menuItemService.getMenuItems(categoryId, keyword, sortBy, sortDirection, page, size);
//
//        ApiResponse<Page<MenuItemResponse>> response = ApiResponse.<Page<MenuItemResponse>>builder()
//                .code(1000)
//                .message("Lấy danh sách món ăn thành công")
//                .result(menuItems)
//                .build();
//
//        return ResponseEntity.ok().body(response);
//    }
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getMenuItems(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MenuItemResponse> menuItems = menuItemService.getMenuItems(categoryId, keyword, status, sortBy, sortDirection, page, size);

        ApiResponse<Page<MenuItemResponse>> response = ApiResponse.<Page<MenuItemResponse>>builder()
                .code(1000)
                .message("Lấy danh sách món ăn thành công")
                .result(menuItems)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Integer id) {
        menuItemService.deleteMenuItem(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(1000)
                .message("Món ăn đã được xóa thành công (chuyển sang UNAVAILABLE)")
                .result(null)
                .build();

        return ResponseEntity.ok().body(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreMenuItem(@PathVariable Integer id) {
        menuItemService.restoreMenuItem(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(1000)
                .message("Món ăn đã được khôi phục thành công (chuyển sang AVAILABLE)")
                .result(null)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItemById(@PathVariable Integer id) {
        MenuItemResponse menuItem = menuItemService.getMenuItemById(id);

        ApiResponse<MenuItemResponse> response = ApiResponse.<MenuItemResponse>builder()
                .code(1000)
                .message("Lấy thông tin món ăn thành công")
                .result(menuItem)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(@Valid @RequestBody MenuItemRequest request) throws IOException {
        MenuItemResponse createdItem = menuItemService.createMenuItem(request);

        ApiResponse<MenuItemResponse> response = ApiResponse.<MenuItemResponse>builder()
                .code(1000)
                .message("Tạo món ăn thành công")
                .result(createdItem)
                .build();

        return ResponseEntity.ok().body(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable Integer id,
            @Valid @RequestBody MenuItemRequest request) throws IOException {
        MenuItemResponse updatedItem = menuItemService.updateMenuItem(id, request);

        ApiResponse<MenuItemResponse> response = ApiResponse.<MenuItemResponse>builder()
                .code(1000)
                .message("Cập nhật món ăn thành công")
                .result(updatedItem)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/count")
    public ApiResponse<Long> getMenuItemCount() {
        Long totalItems = menuItemService.countMenuItems();
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Thành công")
                .result(totalItems)
                .build();
    }
}
package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.EmployeeUpdateRequest;
import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreationRequest request) {
        UserResponse userResponse = userService.createUser(request);

        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> userResponses = userService.getAllUsers();
        ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                .result(userResponses)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getEmployees(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> employeePage = userService.getEmployees(role, keyword, sortDirection, page, size);
        ApiResponse<Page<UserResponse>> response = ApiResponse.<Page<UserResponse>>builder()
                .result(employeePage)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<ApiResponse<Void>> inactiveUser(@PathVariable Integer id) {
        userService.inactiveUser(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(1000)
                .message("Nhân viên chuyển sang trạng thái INACTIVE thành công")
                .result(null)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/employees/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreUser(@PathVariable Integer id) {
        userService.restoreUser(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(1000)
                .message("Nhân viên được khôi phục thành công")
                .result(null)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Integer userId) {
        UserResponse userResponse = userService.getUserById(userId);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/my-info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        UserResponse userResponse = userService.myInfo();
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        userService.updateUser(id, request);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(1000)
                .message("Cập nhật người dùng thành công")
                .result("User ID " + id + " đã được cập nhật")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ApiResponse<Long> getUserCount() {
        Long totalItems = userService.countUser();
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Thành công")
                .result(totalItems)
                .build();
    }
}
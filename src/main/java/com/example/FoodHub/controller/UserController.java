package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.*;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = "http://127.0.0.1:5500") // Cho phép CORS nếu cần (tùy môi trường)
@Slf4j
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getCustomer(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserResponse> customerPage = userService.getCustomers(keyword, status,sortDirection, page, size);

        ApiResponse<Page<UserResponse>> response = ApiResponse.<Page<UserResponse>>builder()
                .result(customerPage)
                .build();

        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getEmployees(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Gọi service để lấy danh sách nhân viên
        Page<UserResponse> employeePage = userService.getEmployees(role, keyword, status, sortDirection, page, size);

        // Xây dựng response với ApiResponse
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


    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> updateUser(
            @PathVariable Integer userId,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        userService.updateUser(userId, request);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(1000)
                .message("Cập nhật người dùng thành công")
                .result("User ID " + userId + " đã được cập nhật")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/cus/{userId}")
    public ResponseEntity<ApiResponse<String>> updateCustomer(
            @PathVariable Integer userId,
            @Valid @RequestBody UserUpdateRequest request) {
        userService.updateCustomer(userId, request);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(1000)
                .message("Cập nhật người dùng thành công")
                .result("User ID " + userId + " đã được cập nhật")
                .build();
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count")
    public ApiResponse<Long> getUserCount() {
        Long totalItems = userService.countUser();
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Thành công")
                .result(totalItems)
                .build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal principal) {

        String email = principal.getName(); // CHẮC CHẮN là email đã đăng nhập
        userService.changePassword(email, request);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .message("Đổi mật khẩu thành công")
                .result("Mật khẩu đã được cập nhật")
                .build());
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendResetPasswordOtp(@RequestBody ForgotPasswordRequest request) {
        userService.sendOtpForPasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.<Void>builder().message("OTP đã gửi về email").build());
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordWithOtpRequest request) {
        userService.resetPasswordWithOtp(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Mật khẩu đã được cập nhật")
                .result("Đổi mật khẩu thành công")
                .build());
    }



}
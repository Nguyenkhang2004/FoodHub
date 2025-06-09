package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.request.UserUpdateRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserCreationRequest request) {
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

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Integer userId) {
        UserResponse userResponse = userService.getUserById(userId);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Integer userId, @RequestBody UserUpdateRequest request) {
        UserResponse userResponse = userService.updateUser(userId, request);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        ApiResponse response = ApiResponse.<Void>builder()
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/myInfo")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        UserResponse userResponse = userService.myInfo();
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }
}


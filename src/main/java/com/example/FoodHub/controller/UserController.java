package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.request.UserUpdateRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(UserCreationRequest request) {
        UserResponse userResponse = userService.createUser(request);
        return ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<UserResponse> userResponses = userService.getAllUsers();
        return ApiResponse.<List<UserResponse>>builder()
                .result(userResponses)
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Integer userId) {
        UserResponse userResponse = userService.getUserById(userId);
        return ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
    }

    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Integer userId, @RequestBody UserUpdateRequest request) {
        UserResponse userResponse = userService.updateUser(userId, request);
        return ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ApiResponse.<Void>builder()
                .build();
    }

    @GetMapping("/myInfo")
    public ApiResponse<UserResponse> getMyInfo() {
        UserResponse userResponse = userService.myInfo();
        return ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
    }
}

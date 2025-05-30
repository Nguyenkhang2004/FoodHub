package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.UserCreationRequest;
import com.example.FoodHub.dto.request.UserUpdateRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.UserResponse;
import com.example.FoodHub.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    public String createUser(@RequestBody UserCreationRequest request, Model model) {
        try {
            UserResponse userResponse = userService.createUser(request);
            model.addAttribute("user", userResponse);
            model.addAttribute("success", true);
            model.addAttribute("message", "User created successfully");
            return "user/create-result";
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Error creating user: " + e.getMessage());
            return "user/create-result";
        }
    }

    @GetMapping
    public String getAllUsers(Model model) {
        try {
            List<UserResponse> userResponses = userService.getAllUsers();
            model.addAttribute("users", userResponses);
            return "user/list";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching users: " + e.getMessage());
            return "user/error";
        }
    }

    @GetMapping("/{userId}")
    public String getUserById(@PathVariable Integer userId, Model model) {
        try {
            UserResponse userResponse = userService.getUserById(userId);
            model.addAttribute("user", userResponse);
            return "user/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching user: " + e.getMessage());
            return "user/error";
        }
    }

    @PutMapping("/{userId}")
    public String updateUser(@PathVariable Integer userId, @RequestBody UserUpdateRequest request, Model model) {
        try {
            UserResponse userResponse = userService.updateUser(userId, request);
            model.addAttribute("user", userResponse);
            model.addAttribute("success", true);
            model.addAttribute("message", "User updated successfully");
            return "user/update-result";
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Error updating user: " + e.getMessage());
            return "user/update-result";
        }
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable Integer userId, Model model) {
        try {
            userService.deleteUser(userId);
            model.addAttribute("success", true);
            model.addAttribute("message", "User deleted successfully");
            return "user/delete-result";
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Error deleting user: " + e.getMessage());
            return "user/delete-result";
        }
    }

    @GetMapping("/myInfo")
    public String getMyInfo(Model model) {
        try {
            UserResponse userResponse = userService.myInfo();
            model.addAttribute("user", userResponse);
            return "user/my-info";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching user info: " + e.getMessage());
            return "user/error";
        }
    }
}
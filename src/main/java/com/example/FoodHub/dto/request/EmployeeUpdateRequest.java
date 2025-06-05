package com.example.FoodHub.dto.request;

import com.example.FoodHub.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeUpdateRequest {
    @NotBlank
    @Size(max = 255)
    private String username;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;
    @Size(max = 255)
    private String password;

    @NotBlank
    private String roleName;  // Phải là 1 trong 5 role được chấp nhận

    @NotBlank
    private String status; // ACTIVE / INACTIVE

    @Size(max = 255)
    private String address;

    @Size(max = 20)
    private String phone;

    private String oauthProvider;
}


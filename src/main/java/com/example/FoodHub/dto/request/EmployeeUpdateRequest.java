package com.example.FoodHub.dto.request;

import com.example.FoodHub.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class    EmployeeUpdateRequest {
    @NotBlank(message="USERNAME_NOT_BLANK")
    @Size(min=5, max=255, message="USERNAME_INVALID")
    String username;
    @Size(min=8, max=255, message="PASSWORD_INVALID")
    String password;

    @NotBlank(message="EMAIL_NOT_BLANK")
    @Email
    @Size(max=255)
    String email;

    @NotBlank(message="PHONE_NOT_BLANK")
    @Pattern(regexp="^[0-9]{10}$", message="PHONE_PATTERN")
    String phone;


    @Size(max=255)
    String address;

    @NotBlank(message="ROLE_NAME_NOT_BLANK")
    @Size(min=2, message="ROLE_NAME_SIZE")
    String roleName ;

    private String oauthProvider;
}


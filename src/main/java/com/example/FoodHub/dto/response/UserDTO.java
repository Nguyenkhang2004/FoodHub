package com.example.FoodHub.dto.response;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String email;
    private String password;
    private String roleName;
    private String status;
    private String address;
    private String phone;
    private Boolean isAuthUser;
    private String oauthProvider;
    private String registrationDate;
}
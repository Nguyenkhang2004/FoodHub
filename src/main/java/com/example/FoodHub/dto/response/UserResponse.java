package com.example.FoodHub.dto.response;

import com.example.FoodHub.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    Integer id;
    String username;
    String email;
    String phone;
    RoleResponse roleName;
    String status;
    String address;
    Instant registrationDate;
}

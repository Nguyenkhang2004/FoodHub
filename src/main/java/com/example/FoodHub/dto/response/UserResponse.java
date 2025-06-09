
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
    String password;
    String roleName; // Sửa từ Role → String
    String status;
    String address;
    String phone;
    Boolean isAuthUser;
    String oauthProvider;
    Instant registrationDate;
}

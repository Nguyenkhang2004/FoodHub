package com.example.FoodHub.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeCreateRequest {
    @NotBlank(message = "USERNAME_NOT_BLANK")
    @Size(min = 2, message = "USERNAME_SIZE")
    String username;

    @NotBlank(message = "PASSWORD_NOT_BLANK")
    @Size(min = 8, max = 8, message = "PASSWORD_SIZE")
    String password;

    @NotBlank(message = "EMAIL_NOT_BLANK")
    @Size(min = 2, message = "EMAIL_SIZE")
    String email;

    @NotBlank(message = "PHONE_NOT_BLANK")
    @Size(min = 2, message = "PHONE_SIZE")
    String phone;

    @NotBlank(message = "ADDRESS_NOT_BLANK")
    @Size(min = 2, message = "ADDRESS_SIZE")
    String address;

    @NotBlank(message = "ROLE_NAME_NOT_BLANK")
    @Size(min = 2, message = "ROLE_NAME_SIZE")
    String roleName;
}
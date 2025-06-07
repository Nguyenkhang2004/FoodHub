package com.example.FoodHub.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCreationRequest {
    @NotBlank(message="USERNAME_NOT_BLANK")
    @Size(min=5,max = 255,message="USERNAME_INVALID")
    private String username;
    @NotBlank(message="PASSWORD_NOT_BLANK")
    @Size(min=8,max = 255,message ="PASSWORD_INVALID")
    private String password;
    @NotBlank(message="EMAIL_NOT_BLANK")
    @Email
    @Size(max = 255)
    private String email;
    @NotBlank(message = "PHONE_NOT_BLANK")
    @Pattern(regexp = "^[0-9]{10}$", message = "PHONE_PATTERN")
    private String phone;
    @NotBlank(message="ADDRESS_NOT_BLANK")
    @Size(max = 255)
    private String address;
    @NotBlank(message="ROLE_NOT_BLANK")
    private String roleName;
}

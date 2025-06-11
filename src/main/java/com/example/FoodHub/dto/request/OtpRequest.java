package com.example.FoodHub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpRequest {
    @NotBlank(message="EMAIL_NOT_BLANK")
    @Size(max=255)
    String email;

    @NotBlank(message="OTP_NOT_BLANK")
    @Size(min=6, max=6, message="OTP_INVALID")
    String otp;
}
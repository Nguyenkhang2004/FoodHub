package com.example.FoodHub.dto.request;

import lombok.Data;

@Data
public class ResetPasswordWithOtpRequest {
    private String email;
    private String otp;
    private String newPassword;
}

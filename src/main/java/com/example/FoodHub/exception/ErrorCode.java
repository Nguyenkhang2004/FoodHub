package com.example.FoodHub.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1001, "Username already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "User not found", HttpStatus.NOT_FOUND),
    PASSWORD_INVALID(1003, "password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1004, "username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_KEY(1005, "invalid key", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Unauthorized", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min} years old", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(1009, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_NOT_EXISTED(1010, "Role not found", HttpStatus.NOT_FOUND),
    MENU_ITEM_NOT_FOUND(1011, "Menu item not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1012, "One or more categories not found", HttpStatus.NOT_FOUND),
    IMAGE_UPLOAD_FAILED(1013, "Failed to upload image", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_IMAGE_URL(1014, "Invalid image URL format", HttpStatus.BAD_REQUEST),
    MENU_ITEM_EXISTED(1015, "Menu item already exists", HttpStatus.BAD_REQUEST),
    MENU_NAME_NOT_BLANK(1016, "Menu name must not be blank", HttpStatus.BAD_REQUEST),
    MENU_NAME_SIZE(1017, "Menu name must be at least 2 characters", HttpStatus.BAD_REQUEST),
    PRICE_NOT_NULL(1018, "Price must not be null", HttpStatus.BAD_REQUEST),
    PRICE_POSITIVE(1019, "Price must be a positive number", HttpStatus.BAD_REQUEST),
    USERNAME_NOT_BLANK(1020, "Username must not be blank", HttpStatus.BAD_REQUEST),
    ROLE_NOT_BLANK(1021, "Role must not be blank", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_BLANK(1022, "Address must not be blank", HttpStatus.BAD_REQUEST),
    PHONE_NOT_BLANK(1023, "Phone number must not be blank", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_BLANK(1024, "Email must not be blank", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_BLANK(1025, "Password must not be blank", HttpStatus.BAD_REQUEST),
    USER_HAS_SCHEDULE(1027, "User has existing work schedules", HttpStatus.BAD_REQUEST),
    USER_HAS_FUTURE_SCHEDULE(1028, "User has future work schedules", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1029, "User not found", HttpStatus.NOT_FOUND),
    INVALID_SHIFT_DATE(1030, "Cannot schedule shifts for days before today", HttpStatus.BAD_REQUEST),
    DUPLICATE_SHIFT(1031, "Shift already exists for the specified date and time", HttpStatus.BAD_REQUEST),
    INVALID_SHIFT_TYPE(1032, "Shift time must be one of: morning, afternoon, night", HttpStatus.BAD_REQUEST),
    PHONE_PATTERN(1026, "Phone number must be exactly 10 digits", HttpStatus.BAD_REQUEST);

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
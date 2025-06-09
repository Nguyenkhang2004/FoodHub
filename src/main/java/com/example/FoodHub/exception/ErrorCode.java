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
    PAYMENT_PENDING(1016, "Payment is pending, please wait for confirmation", HttpStatus.ACCEPTED),
    PAYMENT_FAILED(1017, "Payment failed due to {reason}", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(1018, "Invalid date range, end date must be after start date", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1019, "Order not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_FUNDS(1020, "Insufficient funds, please use another payment method", HttpStatus.PAYMENT_REQUIRED),
    ORDER_COMPLETED(1021, "Order cannot be paid in current status: COMPLETED", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND(1022, "Payment record not found", HttpStatus.NOT_FOUND),
    INVALID_ORDER_AMOUNT(1023, "Invalid order amount", HttpStatus.BAD_REQUEST);

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
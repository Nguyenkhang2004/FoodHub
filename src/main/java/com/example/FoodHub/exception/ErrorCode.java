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
    INTERNAL_SERVER_ERROR(1008, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_NOT_EXISTED(1009, "Role not found", HttpStatus.NOT_FOUND),
    TABLE_NOT_EXISTED(1010, "Table not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_EXISTED(1011, "Order not found", HttpStatus.NOT_FOUND),
    MENU_ITEM_NOT_EXISTED(1012, "Menu item not found", HttpStatus.NOT_FOUND),
    ORDER_ITEMS_REQUIRED(1013, "Order items are required", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_NOT_EXISTED(1014, "Order item not found", HttpStatus.NOT_FOUND),
    ORDER_CANNOT_BE_UPDATED(1015, "Order cannot be updated", HttpStatus.BAD_REQUEST),
    TABLE_NOT_AVAILABLE(1016, "Table is not available", HttpStatus.BAD_REQUEST),
    MENU_ITEM_NOT_AVAILABLE(1017, "Menu item is not available", HttpStatus.BAD_REQUEST),
    INVALID_STATUS_TRANSITION(1018, "Invalid status transition", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1019, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_TABLE_STATUS(1020, "Invalid table status", HttpStatus.BAD_REQUEST),
    TABLE_NUMBER_INVALID(1021, "Table number must not be null", HttpStatus.BAD_REQUEST),
    QR_CODE_INVALID(1022, "QR code must not be null", HttpStatus.BAD_REQUEST),
    AREA_INVALID(1023, "Area must be an alphabet and not be null", HttpStatus.BAD_REQUEST),


    TABLE_ID_REQUIRED(1024, "Table ID is required", HttpStatus.BAD_REQUEST),
    TABLE_ID_INVALID(1025, "Table ID must be a positive number", HttpStatus.BAD_REQUEST),
    USER_ID_REQUIRED(1026, "User ID is required", HttpStatus.BAD_REQUEST),
    USER_ID_INVALID(1027, "User ID must be a positive number", HttpStatus.BAD_REQUEST),
    NOTE_TOO_LONG(1028, "Note must not exceed 500 characters", HttpStatus.BAD_REQUEST),
    ORDER_TYPE_INVALID(1029, "Order type must be DINE_IN, TAKEAWAY, or DELIVERY", HttpStatus.BAD_REQUEST),
    ORDER_STATUS_INVALID(1030, "Invalid order status", HttpStatus.BAD_REQUEST),
    MENU_ITEM_ID_REQUIRED(1031, "Menu item ID is required", HttpStatus.BAD_REQUEST),
    MENU_ITEM_ID_INVALID(1032, "Menu item ID must be a positive number", HttpStatus.BAD_REQUEST),
    QUANTITY_REQUIRED(1033, "Quantity is required", HttpStatus.BAD_REQUEST),
    QUANTITY_INVALID(1034, "Quantity must be at least 1", HttpStatus.BAD_REQUEST),
    QUANTITY_TOO_LARGE(1035, "Quantity cannot exceed 99", HttpStatus.BAD_REQUEST),
    PRICE_REQUIRED(1036, "Price is required", HttpStatus.BAD_REQUEST),
    PRICE_INVALID(1037, "Price must be greater than 0", HttpStatus.BAD_REQUEST),
    PRICE_FORMAT_INVALID(1038, "Price format is invalid", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_STATUS_INVALID(1039, "Order item status must be PENDING, CONFIRMED, READY, CANCELLED, or COMPLETED", HttpStatus.BAD_REQUEST),

    INVALID_DOB(1008, "Your age must be at least {min} years old", HttpStatus.BAD_REQUEST),
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
    PHONE_PATTERN(1026, "Phone number must be exactly 10 digits", HttpStatus.BAD_REQUEST),
    INVALID_TABLE_ID(2000, "Invalid table ID", HttpStatus.BAD_REQUEST),
    INVALID_QR_TOKEN(2001, "Invalid QR token", HttpStatus.UNAUTHORIZED),
    OCCUPIED_TABLE(2002, "Table is already occupied", HttpStatus.BAD_REQUEST),
    INVALID_SESSION_ID(2003, "Invalid session ID", HttpStatus.BAD_REQUEST);
    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

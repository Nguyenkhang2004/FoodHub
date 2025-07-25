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
    INVALID_SESSION_ID(2003, "Invalid session ID", HttpStatus.BAD_REQUEST),
    USER_HAS_SCHEDULED_SHIFTS(1040, "User has scheduled shifts and cannot be deactivated", HttpStatus.BAD_REQUEST),
    DUPLICATE_SHIFT(1042, "Duplicate shift found for the user on the same date", HttpStatus.BAD_REQUEST),
    INVALID_SHIFT_TYPE(1043, "Invalid shift type. Must be morning, afternoon, or night", HttpStatus.BAD_REQUEST),
    USERNAME_SIZE(1045, "Username must be at least 2 characters long", HttpStatus.BAD_REQUEST),
    PASSWORD_SIZE(1047, "Password must be exactly 8 characters", HttpStatus.BAD_REQUEST),
    EMAIL_SIZE(1049, "Email must be at least 2 characters long", HttpStatus.BAD_REQUEST),
    PHONE_SIZE(1051, "Phone must be at least 2 characters long", HttpStatus.BAD_REQUEST),
    ADDRESS_SIZE(1053, "Address must be at least 2 characters long", HttpStatus.BAD_REQUEST),
    ROLE_NAME_NOT_BLANK(1054, "Role name cannot be blank", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTS(1056, "Email already exists", HttpStatus.BAD_REQUEST),
    ROLE_NAME_SIZE(1055, "Role name must be at least 2 characters long", HttpStatus.BAD_REQUEST),
    OTP_NOT_FOUND(1060, "OTP not found", HttpStatus.NOT_FOUND),
    OTP_EXPIRED(1061, "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_ALREADY_VERIFIED(1062, "OTP has already been verified", HttpStatus.BAD_REQUEST),
    INVALID_OTP(1063, "Invalid OTP", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(1064, "Invalid role", HttpStatus.BAD_REQUEST),
    PAYMENT_PENDING(1016, "Payment is pending, please wait for confirmation", HttpStatus.ACCEPTED),
    PAYMENT_FAILED(1017, "Payment failed due to {reason}", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(1018, "Invalid date range, end date must be after start date", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1019, "Order not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_FUNDS(1020, "Insufficient funds, please use another payment method", HttpStatus.PAYMENT_REQUIRED),
    ORDER_COMPLETED(1021, "Order cannot be paid in current status: COMPLETED", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND(1022, "Payment record not found", HttpStatus.NOT_FOUND),
    INVALID_ORDER_AMOUNT(1023, "Invalid order amount", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_CANCELED(1024, "Order has already been canceled", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_PROCESSED(1025, "Payment has already been processed", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_METHOD(1026, "Invalid payment method", HttpStatus.BAD_REQUEST),
    WORK_SCHEDULE_NOT_FOUND(1041, "Work schedule not found", HttpStatus.NOT_FOUND),
    PAST_SHIFT(1041, "You cannot customize past calendars", HttpStatus.BAD_REQUEST),
    SHIFT_NOT_FOUND(1044, "Shift not found", HttpStatus.NOT_FOUND),
    PAST_DATE_NOT_ALLOWED(1046, "You cannot create OR delete a shift in the past", HttpStatus.BAD_REQUEST),
    PAST_SHIFT_TIME(1048, "You cannot create a shift that has already passed", HttpStatus.BAD_REQUEST),
    INVALID_TIME_FORMAT(1050, "Invalid time format, must be HH:mm", HttpStatus.BAD_REQUEST)
    ;

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

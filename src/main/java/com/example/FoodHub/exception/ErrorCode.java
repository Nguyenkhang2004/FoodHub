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

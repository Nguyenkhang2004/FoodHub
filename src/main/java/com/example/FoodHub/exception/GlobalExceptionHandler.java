package com.example.FoodHub.exception;

import com.example.FoodHub.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.expression.AccessException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage() + " : " + e.getMessage());
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<String>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(errorCode.getMessage());
        apiResponse.setCode(errorCode.getCode());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(AccessException.class)
    ResponseEntity<ApiResponse> handleAccessException(AccessException e) {
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setMessage(ErrorCode.UNAUTHORIZED.getMessage() + " : " + e.getMessage());
        apiResponse.setCode(ErrorCode.UNAUTHORIZED.getCode());
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String enumKey = e.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;
        try{
            errorCode = ErrorCode.valueOf(enumKey);
            var constraintViolation = e.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
        } catch (IllegalArgumentException ex) {
            // Handle the case where enumKey is not a valid ErrorCode
        }
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setMessage(attributes != null ? mapAttribute(errorCode.getMessage(), attributes) : errorCode.getMessage());
        apiResponse.setCode(errorCode.getCode());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        if (attributes.containsKey(MIN_ATTRIBUTE)) {
            return message.replace("{" + MIN_ATTRIBUTE + "}", attributes.get(MIN_ATTRIBUTE).toString());
        }
        return message;
    }

}

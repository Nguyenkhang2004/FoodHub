package com.example.FoodHub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
@Data
public class MenuItemRequest {
    @NotBlank(message = "MENU_NAME_NOT_BLANK")
    @Size(min = 2, message = "MENU_NAME_SIZE")
    private String name;
    private String description;
    @NotNull(message = "PRICE_NOT_NULL")
    @Positive(message = "PRICE_POSITIVE")
    private BigDecimal price;
    private String imageUrl;
    private List<Integer> categoryIds;

    // Getters and Setters
}
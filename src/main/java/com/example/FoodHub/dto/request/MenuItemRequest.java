package com.example.FoodHub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
@Data
public class MenuItemRequest {
    @NotBlank(message = "Tên món ăn không được để trống")
    private String name;
    private String description;
    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    private String imageUrl;
    private List<Integer> categoryIds;

    // Getters and Setters
}
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
    @NotBlank(message = "Tên món ăn không được để trống")
    @Size(min = 2, message = "Tên món ăn phải có ít nhất 2 ký tự")
    private String name;
    private String description;
    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    private String imageUrl;
    private List<Integer> categoryIds;

    // Getters and Setters
}
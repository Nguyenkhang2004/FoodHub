package com.example.FoodHub.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantTableRequest {
    @NotBlank(message = "TABLE_ID_REQUIRED")
    @Size(max = 50, message = "TABLE_NUMBER_INVALID")
    private String tableNumber;

    @NotBlank(message = "QR_CODE_INVALID")
    @Size(max = 255, message = "QR_CODE_INVALID")
    private String qrCode;

    @NotBlank(message = "INVALID_TABLE_STATUS")
    @Pattern(regexp = "AVAILABLE|OCCUPIED", message = "INVALID_TABLE_STATUS")
    private String status;

    @NotBlank(message = "AREA_INVALID")
    @Size(max = 10, message = "AREA_INVALID")
    private String area;
}

package com.parsa.miniSnappFood.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodItemRequest {

    @NotBlank(message = "Food name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @Min(value = 0, message = "Supply cannot be negative")
    private int supply;

    private String imageBase64;

    private Long categoryId;

}

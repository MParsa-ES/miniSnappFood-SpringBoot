package com.parsa.miniSnappFood.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FoodItemResponse {
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private int supply;
    private String imageBase64;
    private LocalDateTime createdAt;
}
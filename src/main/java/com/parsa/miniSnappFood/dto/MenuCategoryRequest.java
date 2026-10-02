package com.parsa.miniSnappFood.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

}

package com.parsa.miniSnappFood.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;
    @NotBlank(message = "Restaurant address is required")
    private String address;
    @NotBlank(message = "Restaurant phone is required")
    private String phone;

    private String logoBase64;
}

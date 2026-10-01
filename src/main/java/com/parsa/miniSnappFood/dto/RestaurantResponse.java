package com.parsa.miniSnappFood.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantResponse {
    private Long id;
    private Long ownerId;
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
}

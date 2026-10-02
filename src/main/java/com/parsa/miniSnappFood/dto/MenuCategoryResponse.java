package com.parsa.miniSnappFood.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuCategoryResponse {

    private Long id;
    private Long restaurantId;
    private String name;

}

package com.parsa.miniSnappFood.controller;

import com.parsa.miniSnappFood.dto.FoodItemRequest;
import com.parsa.miniSnappFood.dto.FoodItemResponse;
import com.parsa.miniSnappFood.dto.MenuCategoryRequest;
import com.parsa.miniSnappFood.dto.MenuCategoryResponse;
import com.parsa.miniSnappFood.exception.AccessDeniedException;
import com.parsa.miniSnappFood.model.Role;
import com.parsa.miniSnappFood.model.User;
import com.parsa.miniSnappFood.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    private void checkUserBeingSeller(User user) {
        if (user.getRole() != Role.SELLER) {
            throw new AccessDeniedException("Only sellers can manage menus");
        }
    }

    // Menu Paths

    @PostMapping("/{restaurantId}/menu")
    public ResponseEntity<MenuCategoryResponse>  createMenu(@PathVariable Long restaurantId,
                                                            @RequestBody @Valid MenuCategoryRequest menuCategoryRequest,
                                                            @AuthenticationPrincipal User owner) {
        checkUserBeingSeller(owner);
        MenuCategoryResponse response = menuService.createMenuCategory(menuCategoryRequest, restaurantId, owner);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuCategoryResponse>>  getMenus(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getMenusByRestaurantId(id));
    }

    @DeleteMapping("/menu/{menuId}")
    public ResponseEntity<Map<String,String>> deleteMenu(@PathVariable Long menuId, @AuthenticationPrincipal User owner) {
        checkUserBeingSeller(owner);
        menuService.deleteMenuCategory(menuId, owner);
        return new ResponseEntity<>(Map.of("message", "Food menu removed from restaurant successfully"), HttpStatus.OK);
    }


    // Food item Paths

    @PostMapping("/menu/{menuId}/item")
    public ResponseEntity<FoodItemResponse> createFoodItem(@PathVariable Long menuId, @RequestBody @Valid FoodItemRequest foodItemRequest, @AuthenticationPrincipal User owner) {
        checkUserBeingSeller(owner);
        FoodItemResponse response = menuService.createFoodItem(foodItemRequest, menuId, owner);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<FoodItemResponse> updateFoodItem(@PathVariable Long itemId,
                                                           @RequestBody FoodItemRequest foodItemRequest,
                                                           @AuthenticationPrincipal User owner) {

        checkUserBeingSeller(owner);
        FoodItemResponse response = menuService.updateFoodItem(foodItemRequest, itemId, owner);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/menu/{menuId}/items")
    public ResponseEntity<List<FoodItemResponse>> getFoodItems(@PathVariable Long menuId){
        return ResponseEntity.ok(menuService.getFoodItemsByCategory(menuId));
    }


    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String,String>> deleteFoodItem(@PathVariable Long itemId, @AuthenticationPrincipal User owner) {
        checkUserBeingSeller(owner);
        menuService.deleteFoodItem(itemId, owner);
        return ResponseEntity.ok(Map.of("message", "Food item deleted from restaurant successfully"));
    }



}

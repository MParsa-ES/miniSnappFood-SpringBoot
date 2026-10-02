package com.parsa.miniSnappFood.service;

import com.parsa.miniSnappFood.dto.FoodItemRequest;
import com.parsa.miniSnappFood.dto.FoodItemResponse;
import com.parsa.miniSnappFood.dto.MenuCategoryRequest;
import com.parsa.miniSnappFood.dto.MenuCategoryResponse;
import com.parsa.miniSnappFood.exception.AccessDeniedException;
import com.parsa.miniSnappFood.exception.ResourceNotFoundException;
import com.parsa.miniSnappFood.model.FoodItem;
import com.parsa.miniSnappFood.model.MenuCategory;
import com.parsa.miniSnappFood.model.Restaurant;
import com.parsa.miniSnappFood.model.User;
import com.parsa.miniSnappFood.repository.FoodItemRepository;
import com.parsa.miniSnappFood.repository.MenuCategoryRepository;
import com.parsa.miniSnappFood.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    final private MenuCategoryRepository menuCategoryRepository;
    final private FoodItemRepository foodItemRepository;
    final private RestaurantRepository restaurantRepository;

    //  Menu Methods

    public MenuCategoryResponse createMenuCategory(MenuCategoryRequest menuCategoryRequest, Long restaurantId, User owner) {

        Restaurant restaurant = getRestaurantAndVerifyOwnership(restaurantId, owner);
        MenuCategory menuCategory = new MenuCategory();

        menuCategory.setRestaurant(restaurant);
        menuCategory.setName(menuCategoryRequest.getName());

        MenuCategory savedMenuCategory = menuCategoryRepository.save(menuCategory);
        return mapToMenuCategoryResponse(savedMenuCategory);
    }

    public List<MenuCategoryResponse> getMenusByRestaurantId(Long restaurantId) {
        return menuCategoryRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::mapToMenuCategoryResponse)
                .collect(Collectors.toList());
    }

    public void deleteMenuCategory(Long categoryId, User owner) {
        MenuCategory menuCategory = menuCategoryRepository.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException("Category not found with id: " + categoryId)
        );

        getRestaurantAndVerifyOwnership(menuCategory.getRestaurant().getId(), owner);
        menuCategoryRepository.delete(menuCategory);
    }

    //  Food Item Methods

    public FoodItemResponse createFoodItem(FoodItemRequest foodItemRequest, Long categoryId, User owner) {

        MenuCategory category = menuCategoryRepository.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException("Category not found with id " + categoryId)
        );

        getRestaurantAndVerifyOwnership(category.getRestaurant().getId(), owner);

        FoodItem foodItem = new FoodItem();
        foodItem.setName(foodItemRequest.getName());
        foodItem.setCategory(category);
        foodItem.setDescription(foodItemRequest.getDescription());
        foodItem.setPrice(foodItemRequest.getPrice());
        foodItem.setSupply(foodItemRequest.getSupply());
        foodItem.setImageBase64(foodItemRequest.getImageBase64());

        FoodItem  savedFoodItem = foodItemRepository.save(foodItem);
        return mapToFoodItemResponse(savedFoodItem);
    }

    public FoodItemResponse updateFoodItem(FoodItemRequest foodItemRequest, Long foodItemId, User owner) {

        FoodItem foodItem = foodItemRepository.findById(foodItemId).orElseThrow(
                () -> new ResourceNotFoundException("FoodItem not found with id " + foodItemId)
        );

        getRestaurantAndVerifyOwnership(foodItem.getCategory().getRestaurant().getId(), owner);

        if (foodItemRequest.getCategoryId() != null) {
            MenuCategory newCategory = menuCategoryRepository.findById(foodItemRequest.getCategoryId()).orElseThrow(
                    () -> new ResourceNotFoundException("New Category not found with id " + foodItemRequest.getCategoryId())
            );

            getRestaurantAndVerifyOwnership(newCategory.getRestaurant().getId(), owner);
            foodItem.setCategory(newCategory);
        }

        if (foodItemRequest.getName() != null && !foodItemRequest.getName().isBlank()){
            foodItem.setName(foodItemRequest.getName());
        }
        if (foodItemRequest.getDescription() != null){
            foodItem.setDescription(foodItemRequest.getDescription());
        }
        if (foodItemRequest.getImageBase64() != null && !foodItemRequest.getImageBase64().isBlank()){
            foodItem.setImageBase64(foodItemRequest.getImageBase64());
        }
        if (foodItemRequest.getPrice() != null) {
            foodItem.setPrice(foodItemRequest.getPrice());
        }
        if (foodItemRequest.getSupply() >= 0){
            foodItem.setSupply(foodItemRequest.getSupply());
        }

        FoodItem updatedFoodItem = foodItemRepository.save(foodItem);
        return mapToFoodItemResponse(updatedFoodItem);
    }

    public List<FoodItemResponse> getFoodItemsByCategory(Long categoryId) {
        return foodItemRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToFoodItemResponse)
                .collect(Collectors.toList());
    }

    public void deleteFoodItem(Long foodItemId, User owner) {
        FoodItem foodItem = foodItemRepository.findById(foodItemId).orElseThrow(
                () -> new ResourceNotFoundException("FoodItem not found with id " + foodItemId)
        );

        getRestaurantAndVerifyOwnership(foodItem.getCategory().getRestaurant().getId(), owner);
        foodItemRepository.delete(foodItem);
    }

    // Helper Methods

    private Restaurant getRestaurantAndVerifyOwnership(Long restaurantId, User owner) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId)
        );

        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You do not own this restaurant");
        }

        return restaurant;
    }

    private MenuCategoryResponse mapToMenuCategoryResponse(MenuCategory  menuCategory) {

        return MenuCategoryResponse.builder()
                .id(menuCategory.getId())
                .name(menuCategory.getName())
                .restaurantId(menuCategory.getRestaurant().getId())
                .build();
    }

    private FoodItemResponse mapToFoodItemResponse(FoodItem foodItem) {
        return FoodItemResponse.builder()
                .id(foodItem.getId())
                .name(foodItem.getName())
                .categoryId(foodItem.getCategory().getId())
                .description(foodItem.getDescription())
                .imageBase64(foodItem.getImageBase64())
                .price(foodItem.getPrice())
                .supply(foodItem.getSupply())
                .build();
    }

}

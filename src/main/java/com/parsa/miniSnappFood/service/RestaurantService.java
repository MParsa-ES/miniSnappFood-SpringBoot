package com.parsa.miniSnappFood.service;

import com.parsa.miniSnappFood.dto.RestaurantRequest;
import com.parsa.miniSnappFood.dto.RestaurantResponse;
import com.parsa.miniSnappFood.exception.AccessDeniedException;
import com.parsa.miniSnappFood.exception.ResourceNotFoundException;
import com.parsa.miniSnappFood.model.Restaurant;
import com.parsa.miniSnappFood.model.User;
import com.parsa.miniSnappFood.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantResponse createRestaurant(RestaurantRequest restaurantRequest, User owner) {
        Restaurant restaurant = new Restaurant();

        restaurant.setName(restaurantRequest.getName());
        restaurant.setAddress(restaurantRequest.getAddress());
        restaurant.setPhone(restaurantRequest.getPhone());

        if (restaurantRequest.getLogoBase64() != null) {
            restaurant.setLogoBase64(restaurantRequest.getLogoBase64());
        }

        restaurant.setOwner(owner);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        return mapToResponse(savedRestaurant);
    }

    public List<RestaurantResponse> getRestaurantsByOwnerId(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest restaurantRequest, User owner) {

        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Restaurant not found with id: " + id)
        );

        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You do not have permission to update this restaurant");
        }

        if (restaurantRequest.getName() != null && !restaurantRequest.getName().isBlank()) {
            restaurant.setName(restaurantRequest.getName());
        }

        if (restaurantRequest.getAddress() != null && !restaurantRequest.getAddress().isBlank()) {
            restaurant.setAddress(restaurantRequest.getAddress());
        }

        if (restaurantRequest.getPhone() != null && !restaurantRequest.getPhone().isBlank()) {
            restaurant.setPhone(restaurantRequest.getPhone());
        }

        if (restaurantRequest.getLogoBase64() != null && !restaurantRequest.getLogoBase64().isBlank()) {
            restaurant.setLogoBase64(restaurantRequest.getLogoBase64());
        }
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        return mapToResponse(savedRestaurant);
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .ownerId(restaurant.getOwner().getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .logoBase64(restaurant.getLogoBase64())
                .build();
    }

}

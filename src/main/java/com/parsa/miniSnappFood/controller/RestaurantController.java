package com.parsa.miniSnappFood.controller;

import com.parsa.miniSnappFood.dto.RestaurantRequest;
import com.parsa.miniSnappFood.dto.RestaurantResponse;
import com.parsa.miniSnappFood.exception.AccessDeniedException;
import com.parsa.miniSnappFood.model.Role;
import com.parsa.miniSnappFood.model.User;
import com.parsa.miniSnappFood.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;


    @PostMapping("")
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @RequestBody @Valid RestaurantRequest request,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser.getRole() != Role.SELLER) {
            throw new AccessDeniedException("Only Sellers can create a restaurant");
        }

        RestaurantResponse restaurantResponse = restaurantService.createRestaurant(request, currentUser);
        return new ResponseEntity<>(restaurantResponse, HttpStatus.CREATED);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<RestaurantResponse>> getMineRestaurants(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != Role.SELLER) {
            throw new AccessDeniedException("Only Sellers have restaurants");
        }

        List<RestaurantResponse> restaurants = restaurantService.getRestaurantsByOwnerId(currentUser.getId());
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(@RequestBody RestaurantRequest request,
                                                               @AuthenticationPrincipal User currentUser,
                                                               @PathVariable long id) {
        if (currentUser.getRole() != Role.SELLER) {
            throw new AccessDeniedException("Only Sellers have restaurants");
        }

        RestaurantResponse response = restaurantService.updateRestaurant(id, request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

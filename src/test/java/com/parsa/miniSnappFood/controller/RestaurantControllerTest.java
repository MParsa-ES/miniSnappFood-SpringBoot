package com.parsa.miniSnappFood.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.parsa.miniSnappFood.dto.RestaurantRequest;
import com.parsa.miniSnappFood.model.Restaurant;
import com.parsa.miniSnappFood.model.Role;
import com.parsa.miniSnappFood.model.User;
import com.parsa.miniSnappFood.repository.RestaurantRepository;
import com.parsa.miniSnappFood.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RestaurantControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository  restaurantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String createTestUserAndGetToken(String phone, Role role) throws Exception {

        User user = new User();
        user.setFullName("Test " + role.name());
        user.setPhone(phone);
        user.setAddress("Random Address");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        userRepository.save(user);

        String loginJson = String.format("{\"phone\":\"%s\",\"password\":\"%s\"}", phone, "password123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }


    @Test
    void createRestaurant_AsSeller_Returns201() throws Exception {
        String token = createTestUserAndGetToken("09111111111", Role.SELLER);

        RestaurantRequest request = new RestaurantRequest();
        request.setName("Snapp Burger");
        request.setAddress("Tehran, Enghelab Sq");
        request.setPhone("02112345678");

        mockMvc.perform(post("/restaurants")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Snapp Burger"))
                .andExpect(jsonPath("$.ownerId").exists());
    }

    @Test
    void createRestaurant_AsBuyer_Returns403Forbidden() throws Exception {
        String token = createTestUserAndGetToken("09222222222", Role.BUYER);

        RestaurantRequest request = new RestaurantRequest();
        request.setName("Illegal Burger");
        request.setAddress("Nowhere");
        request.setPhone("000000");

        mockMvc.perform(post("/restaurants")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only Sellers can create a restaurant"));
    }


    @Test
    void getMineRestaurants_AsSeller_ReturnsOnlyOwnedRestaurants() throws Exception {
        String sellerToken = createTestUserAndGetToken("09111111111", Role.SELLER);
        User seller = userRepository.findByPhone("09111111111").orElseThrow();

        // Save a restaurant owned by this seller
        Restaurant r1 = new Restaurant();
        r1.setName("My Pizza");
        r1.setAddress("Street 1");
        r1.setPhone("111");
        r1.setOwner(seller);
        restaurantRepository.save(r1);

        mockMvc.perform(get("/restaurants/mine")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("My Pizza"));
    }


    @Test
    void updateRestaurant_OwnRestaurant_PartialUpdate_Returns200() throws Exception {
        String token = createTestUserAndGetToken("09111111111", Role.SELLER);
        User seller = userRepository.findByPhone("09111111111").orElseThrow();

        Restaurant original = new Restaurant();
        original.setName("Old Name");
        original.setAddress("Old Address");
        original.setPhone("Old Phone");
        original.setOwner(seller);
        restaurantRepository.save(original);

        RestaurantRequest updateRequest = new RestaurantRequest();
        updateRequest.setName("New Name");

        mockMvc.perform(put("/restaurants/" + original.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.address").value("Old Address"));
    }

    @Test
    void updateRestaurant_OtherSellersRestaurant_Returns403Forbidden() throws Exception {
        String token1 = createTestUserAndGetToken("09111111111", Role.SELLER);
        User seller1 = userRepository.findByPhone("09111111111").orElseThrow();

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Seller 1's Cafe");
        restaurant.setAddress("Street 1");
        restaurant.setPhone("111");
        restaurant.setOwner(seller1);
        restaurantRepository.save(restaurant);

        String token2 = createTestUserAndGetToken("09333333333", Role.SELLER);
        RestaurantRequest maliciousUpdate = new RestaurantRequest();
        maliciousUpdate.setName("Hacked Name");

        mockMvc.perform(put("/restaurants/" + restaurant.getId())
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousUpdate)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You do not have permission to update this restaurant"));
    }

}

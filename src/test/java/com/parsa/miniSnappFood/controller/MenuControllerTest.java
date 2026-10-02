package com.parsa.miniSnappFood.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parsa.miniSnappFood.dto.FoodItemRequest;
import com.parsa.miniSnappFood.dto.MenuCategoryRequest;
import com.parsa.miniSnappFood.model.*;
import com.parsa.miniSnappFood.repository.FoodItemRepository;
import com.parsa.miniSnappFood.repository.MenuCategoryRepository;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {

        foodItemRepository.deleteAll();
        menuCategoryRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();
    }


    private String createTestUserAndGetToken(String phone, Role role) throws Exception {
        User user = new User();
        user.setFullName("Test " + role.name() + " " + phone);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setAddress("Random Address");
        user.setRole(role);
        userRepository.save(user);

        String loginJson = String.format("{\"phone\":\"%s\",\"password\":\"%s\"}", phone, "password123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private Restaurant seedRestaurantForUser(String phone) {
        User owner = userRepository.findByPhone(phone).orElseThrow();
        Restaurant r = new Restaurant();
        r.setName("Test Restaurant");
        r.setAddress("Test Address");
        r.setPhone("12345");
        r.setOwner(owner);
        return restaurantRepository.save(r);
    }

    private MenuCategory seedCategoryForRestaurant(Restaurant restaurant, String name) {
        MenuCategory category = new MenuCategory();
        category.setName(name);
        category.setRestaurant(restaurant);
        return menuCategoryRepository.save(category);
    }

    // ==========================================
    // MENU CATEGORY TESTS
    // ==========================================

    @Test
    void createMenuCategory_AsOwner_Returns200() throws Exception {
        String token = createTestUserAndGetToken("09111111111", Role.SELLER);
        Restaurant restaurant = seedRestaurantForUser("09111111111");

        MenuCategoryRequest request = new MenuCategoryRequest();
        request.setName("Pizzas");

        mockMvc.perform(post("/restaurants/" + restaurant.getId() + "/menu")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pizzas"))
                .andExpect(jsonPath("$.restaurantId").value(restaurant.getId()));
    }

    @Test
    void createMenuCategory_AsOtherSeller_Returns403() throws Exception {
        // Seller 1 owns the restaurant
        createTestUserAndGetToken("09111111111", Role.SELLER);
        Restaurant restaurant = seedRestaurantForUser("09111111111");

        // Seller 2 tries to add a category to Seller 1's restaurant
        String thiefToken = createTestUserAndGetToken("09222222222", Role.SELLER);

        MenuCategoryRequest request = new MenuCategoryRequest();
        request.setName("Hacked Menu");

        mockMvc.perform(post("/restaurants/" + restaurant.getId() + "/menu")
                        .header("Authorization", "Bearer " + thiefToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You do not own this restaurant"));
    }

    // ==========================================
    // FOOD ITEM TESTS
    // ==========================================

    @Test
    void createFoodItem_AsOwner_Returns200() throws Exception {
        String token = createTestUserAndGetToken("09111111111", Role.SELLER);
        Restaurant restaurant = seedRestaurantForUser("09111111111");
        MenuCategory category = seedCategoryForRestaurant(restaurant, "Burgers");

        FoodItemRequest request = new FoodItemRequest();
        request.setName("Cheeseburger");
        request.setPrice(BigDecimal.valueOf(150.50));
        request.setSupply(50);
        request.setDescription("Delicious cheese");

        mockMvc.perform(post("/restaurants/menu/" + category.getId() + "/item")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cheeseburger"))
                .andExpect(jsonPath("$.price").value(150.5))
                .andExpect(jsonPath("$.supply").value(50));
    }

    @Test
    void updateFoodItem_ChangeCategoryAndPrice_Returns200() throws Exception {
        String token = createTestUserAndGetToken("09111111111", Role.SELLER);
        Restaurant restaurant = seedRestaurantForUser("09111111111");

        MenuCategory drinksCategory = seedCategoryForRestaurant(restaurant, "Drinks");
        MenuCategory sidesCategory = seedCategoryForRestaurant(restaurant, "Sides");

        FoodItem fries = new FoodItem();
        fries.setName("Fries");
        fries.setPrice(BigDecimal.valueOf(20.0));
        fries.setSupply(100);
        fries.setCategory(drinksCategory);
        foodItemRepository.save(fries);

        FoodItemRequest request = new FoodItemRequest();
        request.setCategoryId(sidesCategory.getId());
        request.setPrice(BigDecimal.valueOf(25.0));

        mockMvc.perform(put("/restaurants/items/" + fries.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fries"))
                .andExpect(jsonPath("$.price").value(25.0))
                .andExpect(jsonPath("$.categoryId").value(sidesCategory.getId()));
    }

    @Test
    void deleteMenuCategory_AsOwner_Returns200() throws Exception {
        String token = createTestUserAndGetToken("09111111111", Role.SELLER);
        Restaurant restaurant = seedRestaurantForUser("09111111111");
        MenuCategory category = seedCategoryForRestaurant(restaurant, "To Be Deleted");

        mockMvc.perform(delete("/restaurants/menu/" + category.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}
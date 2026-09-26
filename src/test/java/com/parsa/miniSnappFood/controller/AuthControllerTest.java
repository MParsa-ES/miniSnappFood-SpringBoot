package com.parsa.miniSnappFood.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parsa.miniSnappFood.dto.LoginRequest;
import com.parsa.miniSnappFood.dto.RegisterRequest;
import com.parsa.miniSnappFood.dto.UpdateProfileRequest;
import com.parsa.miniSnappFood.model.BankInfo;
import com.parsa.miniSnappFood.model.Role;
import com.parsa.miniSnappFood.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Registration Success - Returns 201 Created & Token")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Parsa Esmaeili",
                "09123456789",
                "parsa@example.com",
                "Password123!",
                Role.BUYER,
                "Tehran, Iran",
                null,
                null
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    @DisplayName("2. Registration Duplicate Phone - Returns 409 Conflict")
    void register_DuplicatePhone_ReturnsConflict() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Parsa Esmaeili",
                "09123456789",
                "parsa@example.com",
                "Password123!",
                Role.BUYER,
                "Tehran, Iran",
                null,
                null
        );


        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));


        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("3. Login Success - Returns 200 OK & Token")
    void login_Success() throws Exception {
        register_Success();

        LoginRequest loginRequest = new LoginRequest("09123456789", "Password123!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.phone").value("09123456789"));
    }

    @Test
    @DisplayName("4. Protected Route without JWT - Returns 401 Unauthorized")
    void profile_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5. Profile Fetch with valid JWT - Returns 200 OK")
    void profile_WithValidToken_ReturnsUser() throws Exception {
        String token = obtainJwtToken("09123456789", "Password123!");

        mockMvc.perform(get("/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Parsa Esmaeili"))
                .andExpect(jsonPath("$.phone").value("09123456789"));
    }

    @Test
    @DisplayName("6. Profile Update - Returns Updated Profile")
    void updateProfile_Success() throws Exception {
        String token = obtainJwtToken("09123456789", "Password123!");

        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setAddress("Amirkabir University of Technology");
        updateRequest.setBankInfo(new BankInfo("Mellat", "6104337712345678"));

        mockMvc.perform(put("/auth/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Amirkabir University of Technology"))
                .andExpect(jsonPath("$.bankInfo.bankName").value("Mellat"));
    }

    @Test
    @DisplayName("7. Logout Revokes JWT - Subsequent Request Returns 401 Unauthorized")
    void logout_RevokesToken() throws Exception {
        String token = obtainJwtToken("09123456789", "Password123!");


        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());


        mockMvc.perform(get("/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private String obtainJwtToken(String phone, String password) throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Parsa Esmaeili",
                phone,
                "parsa@example.com",
                password,
                Role.BUYER,
                "Tehran, Iran",
                null,
                null
        );

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseString).get("token").asText();
    }
}
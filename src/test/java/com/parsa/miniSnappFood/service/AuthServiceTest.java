package com.parsa.miniSnappFood.service;

import com.parsa.miniSnappFood.dto.RegisterRequest;
import com.parsa.miniSnappFood.dto.RegisterResponse;
import com.parsa.miniSnappFood.exception.UserAlreadyExistsException;
import com.parsa.miniSnappFood.model.Role;
import com.parsa.miniSnappFood.model.User;
import com.parsa.miniSnappFood.repository.UserRepository;
import com.parsa.miniSnappFood.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "Parsa Esmail",
                "09123456789",
                "parsa@example.com",
                "password123",
                Role.BUYER,
                "Tehran, Iran",
                null,
                null
        );
    }


    @Test
    @DisplayName("Should successfully register for a new user")
    void register_Success(){
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setPhone(registerRequest.getPhone());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("fake.jwt.token");

        RegisterResponse response = authService.register(registerRequest);


        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("fake.jwt.token", response.getToken());
        assertEquals("User registered successfully", response.getMessage());

        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    @DisplayName("Should throw UserAlreadyExistsException when phone is taken")
    void register_DuplicatePhone_ThrowsException() {

        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(true);


        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Phone number is already registered!", exception.getMessage());


        verify(userRepository, never()).save(any(User.class));
    }
}

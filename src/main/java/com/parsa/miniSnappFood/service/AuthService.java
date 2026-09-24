package com.parsa.miniSnappFood.service;

import com.parsa.miniSnappFood.dto.*;
import com.parsa.miniSnappFood.exception.InvalidCredentialsException;
import com.parsa.miniSnappFood.exception.UserAlreadyExistsException;
import com.parsa.miniSnappFood.model.User;
import com.parsa.miniSnappFood.repository.UserRepository;
import com.parsa.miniSnappFood.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("Phone number is already registered!");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered!");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        user.setRole(request.getRole());
        user.setProfileImageBase64(request.getProfileImageBase64());
        user.setBankInfo(request.getBankInfo());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        String token =  jwtService.generateToken(savedUser);

        return new RegisterResponse("User registered successfully", savedUser.getId(), token);
    }



    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByPhone(request.getPhone()).
                orElseThrow(() -> new InvalidCredentialsException("Invalid Phone number or password!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Phone number or password!");
        }

        String token = jwtService.generateToken(user);
        UserResponse userResponse = mapToUserResponse(user);

        return new AuthResponse(token, userResponse);
    }


    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getAddress(),
                user.getProfileImageBase64(),
                user.getBankInfo()
        );
    }
}

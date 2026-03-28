package com.talklingo.backend.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.talklingo.backend.dto.AuthRequest;
import com.talklingo.backend.dto.AuthResponse;
import com.talklingo.backend.dto.RegisterRequest;
import com.talklingo.backend.dto.UserProfileResponse;
import com.talklingo.backend.entity.User;
import com.talklingo.backend.entity.UserRole;
import com.talklingo.backend.repository.UserRepository;
import com.talklingo.backend.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("An account already exists for this email.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);
        return new AuthResponse(
                JwtUtil.generateToken(savedUser.getEmail()),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole() != null ? savedUser.getRole().name() : UserRole.USER.name());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        return new AuthResponse(
                JwtUtil.generateToken(user.getEmail()),
                user.getName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : UserRole.USER.name());
    }

    @GetMapping("/profile")
    public UserProfileResponse profile(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole() != null ? user.getRole().name() : UserRole.USER.name());
        return response;
    }
}

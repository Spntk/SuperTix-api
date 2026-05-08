package com.supertix.api.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.supertix.api.dtos.auth.SignInRequest;
import com.supertix.api.dtos.auth.SignUpRequest;
import com.supertix.api.enums.UserStatus;
import com.supertix.api.models.UserModel;
import com.supertix.api.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, String> signUp(SignUpRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already exists");
        }

        UserModel user = new UserModel();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        String token = jwtService.generateToken(user, false);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Register successful");
        return response;
    }

    public Map<String, String> signIn(SignInRequest dto) {
        UserModel user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account has been suspended");
        }

        String token = jwtService.generateToken(user, dto.isKeepSignedIn());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Login successful");
        return response;
    }

    public Map<String, String> refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired");
        }

        Long userId = jwtService.extractUserId(token);
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account has been suspended");
        }

        boolean keepSignedIn = jwtService.extractClaims(token).getExpiration().getTime()
                - jwtService.extractClaims(token).getIssuedAt().getTime() > 1000L * 60 * 60 * 24 * 2;

        String newToken = jwtService.generateToken(user, keepSignedIn);

        Map<String, String> response = new HashMap<>();
        response.put("token", newToken);
        response.put("message", "Token refreshed");
        return response;
    }
}

package com.supertix.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.supertix.api.dtos.user.UserResponse;
import com.supertix.api.dtos.user.UserUpdateInfoRequest;
import com.supertix.api.dtos.user.UserUpdatePasswordRequest;
import com.supertix.api.dtos.user.UserUpdateRequest;
import com.supertix.api.models.UserModel;
import com.supertix.api.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUser() {
        return userRepository.findAllByOrderByIdAsc()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getPhone(),
                        user.getAddress(),
                        user.getRole().name(),
                        user.getStatus().name(),
                        user.getCreatedAt().toString()))
                .toList();
    }

    public UserResponse getUserProfile(Long userId) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt().toString());
    }

    public Map<String, String> updateUserByAdmin(Long userId, UserUpdateRequest dto) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Update user successful");
        return response;
    }

    public Map<String, String> updateUser(Long userId, UserUpdateInfoRequest dto) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Update user successful");
        return response;
    }

    public Map<String, String> updatePassword(Long userId, UserUpdatePasswordRequest dto) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getGetNewPassword()));
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Update password successful");
        return response;
    }
}

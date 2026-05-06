package com.supertix.api.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supertix.api.dtos.user.UserResponse;
import com.supertix.api.dtos.user.UserUpdateInfoRequest;
import com.supertix.api.dtos.user.UserUpdatePasswordRequest;
import com.supertix.api.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateUser(
            HttpServletRequest request,
            @Valid @RequestBody UserUpdateInfoRequest dto) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(userService.updateUser(userId, dto));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            HttpServletRequest request,
            @Valid @RequestBody UserUpdatePasswordRequest dto) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(userService.updatePassword(userId, dto));
    }
}

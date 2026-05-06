package com.supertix.api.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supertix.api.dtos.auth.SignInRequest;
import com.supertix.api.dtos.auth.SignUpRequest;
import com.supertix.api.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody SignUpRequest dto) {
        return ResponseEntity.ok(authService.signUp(dto));
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, String>> signIn(@Valid @RequestBody SignInRequest dto) {
        return ResponseEntity.ok(authService.signIn(dto));
    }
}

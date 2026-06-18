package com.example.ems.controller;

import com.example.ems.dto.*;
import com.example.ems.entity.User;
import com.example.ems.service.AuthService;
import com.example.ems.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
         @Valid @RequestBody AuthRequest request
    ) {

         LoginResponse loginResponse=authService.login(request);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserDto userDto
    ) {

        UserResponseDto user = userService.createUser(userDto);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request
    ) {

        Object logout = authService.logout(request);

        return ResponseEntity.ok(
                logout
        );
    }
}
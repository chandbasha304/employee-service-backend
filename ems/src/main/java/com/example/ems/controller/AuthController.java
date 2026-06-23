package com.example.ems.controller;

import com.example.ems.dto.*;
import com.example.ems.entity.User;
import com.example.ems.service.AuthService;
import com.example.ems.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
         @Valid @RequestBody AuthRequest request
    ) {
        log.info("[START] AuthController.login - email: {}", request.getEmail());
        long start = System.currentTimeMillis();
        LoginResponse loginResponse = authService.login(request);
        log.info("[END] AuthController.login - email: {} finished in {}ms", request.getEmail(), System.currentTimeMillis() - start);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserDto userDto
    ) {
        log.info("[START] AuthController.createUser - email: {}", userDto.getEmail());
        long start = System.currentTimeMillis();
        UserResponseDto user = userService.createUser(userDto);
        log.info("[END] AuthController.createUser - email: {} finished in {}ms", userDto.getEmail(), System.currentTimeMillis() - start);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request
    ) {
        log.info("[START] AuthController.logout");
        long start = System.currentTimeMillis();
        Object logout = authService.logout(request);
        log.info("[END] AuthController.logout - finished in {}ms", System.currentTimeMillis() - start);
        return ResponseEntity.ok(
                logout
        );
    }
}
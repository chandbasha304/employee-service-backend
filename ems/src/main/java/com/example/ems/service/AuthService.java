package com.example.ems.service;

import com.example.ems.dto.AuthRequest;
import com.example.ems.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginResponse login(AuthRequest request);

    Object logout(HttpServletRequest request);
}
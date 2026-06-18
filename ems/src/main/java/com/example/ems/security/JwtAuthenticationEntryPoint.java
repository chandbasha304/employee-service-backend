package com.example.ems.security;

import com.example.ems.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Component
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {



        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication Failed. Invalid or Missing JWT Token",
                request.getRequestURI(),
                LocalDateTime.now()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType("application/json");

        ObjectMapper mapper = new ObjectMapper();

        response.getWriter()
                .write(mapper.writeValueAsString(error));
    }
}
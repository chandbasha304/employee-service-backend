package com.example.ems.service.impl;

import com.example.ems.dto.AuthRequest;

import com.example.ems.dto.LoginResponse;

import com.example.ems.entity.UserSession;

import com.example.ems.repository.UserSessionRepository;

import com.example.ems.service.AuthService;
import com.example.ems.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager
            authenticationManager;

    private final JwtService jwtService;

    private final UserSessionRepository
            sessionRepository;

    @Transactional
    public LoginResponse login(
            AuthRequest request
    ) {
        String currentUserAgent = "";
        try {
            org.springframework.web.context.request.ServletRequestAttributes attributes =
                    (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                currentUserAgent = attributes.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            // fallback
        }
        if (currentUserAgent == null) {
            currentUserAgent = "";
        }

        Optional<UserSession> activeSession =
                sessionRepository
                        .findByEmailAndIsActiveTrue(
                                request.getEmail()
                        );

        if (activeSession.isPresent()) {
            UserSession session = activeSession.get();
            String storedToken = session.getToken();
            String storedUserAgent = "";
            String jwt = storedToken;
            if (storedToken != null && storedToken.contains("|")) {
                int index = storedToken.indexOf("|");
                jwt = storedToken.substring(0, index);
                storedUserAgent = storedToken.substring(index + 1);
            }

            boolean isExpired = false;
            try {
                jwtService.extractUsername(jwt);
            } catch (Exception ex) {
                isExpired = true;
            }

            if (isExpired || storedUserAgent.equals(currentUserAgent)) {
                // Deactivate the old session
                session.setIsActive(false);
                sessionRepository.save(session);
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Session already active. Please logout first."
                );
            }
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token =
                jwtService.generateToken(
                        request.getEmail()
                );

        String dbToken = token + "|" + currentUserAgent;

        UserSession session =
                new UserSession();

        session.setEmail(
                request.getEmail()
        );

        session.setToken(dbToken);

        session.setIsActive(true);

        session.setCreatedAt(
                LocalDateTime.now()
        );

        sessionRepository.save(session);

        return new LoginResponse(token);
    }

    @Transactional
    public Object logout(
            HttpServletRequest request
    ) {

        final String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Authorization token missing"
            );
        }

        String token =
                authHeader.substring(7);

        UserSession session =

                sessionRepository
                        .findByTokenAndIsActiveTrue(
                                token
                        )
                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Session not found"
                                )
                        );

        session.setIsActive(false);

        sessionRepository.save(session);
        return session;
    }
}
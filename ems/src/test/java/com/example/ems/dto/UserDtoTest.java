package com.example.ems.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private UserDto createValidDto() {
        return UserDto.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .password("Password123!") // Valid password format
                .phone("+1234567890")
                .role("ADMIN")
                .status("active")
                .build();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        UserDto dto = createValidDto();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid User DTO should have no violations");
    }

    @Test
    void whenEmailExactly50Characters_thenNoViolations() {
        UserDto dto = createValidDto();
        String longEmail = "a".repeat(38) + "@example.com"; // 50 chars
        dto.setEmail(longEmail);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Email with exactly 50 characters should be valid");
    }

    @Test
    void whenEmailExceeds50Characters_thenViolation() {
        UserDto dto = createValidDto();
        String tooLongEmail = "a".repeat(39) + "@example.com"; // 51 chars
        dto.setEmail(tooLongEmail);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Email exceeding 50 characters should produce violation");

        boolean hasEmailSizeViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                        && v.getMessage().equals("Email cannot exceed 50 characters"));
        assertTrue(hasEmailSizeViolation);
    }
}

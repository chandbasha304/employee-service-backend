package com.example.ems.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private EmployeeRequestDto createValidDto() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setDesignation("Developer");
        dto.setPhone("+1234567890");
        dto.setSalary(new BigDecimal("5000.00"));
        dto.setDepartmentId(1L);
        return dto;
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        EmployeeRequestDto dto = createValidDto();
        Set<ConstraintViolation<EmployeeRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
    }

    @Test
    void whenEmailExactly50Characters_thenNoViolations() {
        EmployeeRequestDto dto = createValidDto();
        // "a" * 38 + "@example.com" = 38 + 12 = 50 characters
        String longEmail = "a".repeat(38) + "@example.com";
        assertEquals(50, longEmail.length());
        dto.setEmail(longEmail);

        Set<ConstraintViolation<EmployeeRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Email with exactly 50 characters should be valid");
    }

    @Test
    void whenEmailExceeds50Characters_thenViolation() {
        EmployeeRequestDto dto = createValidDto();
        // "a" * 39 + "@example.com" = 39 + 12 = 51 characters
        String tooLongEmail = "a".repeat(39) + "@example.com";
        assertEquals(51, tooLongEmail.length());
        dto.setEmail(tooLongEmail);

        Set<ConstraintViolation<EmployeeRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Email exceeding 50 characters should produce violation");

        boolean hasEmailSizeViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") 
                        && v.getMessage().equals("Email cannot exceed 50 characters"));
        assertTrue(hasEmailSizeViolation, "Should trigger the email size validation limit message");
    }

    @Test
    void whenEmailFormatInvalid_thenViolation() {
        EmployeeRequestDto dto = createValidDto();
        dto.setEmail("invalid-email-format");

        Set<ConstraintViolation<EmployeeRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Invalid email format should produce violation");

        boolean hasEmailFormatViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") 
                        && v.getMessage().equals("Invalid email format"));
        assertTrue(hasEmailFormatViolation, "Should trigger the invalid email format message");
    }

    @Test
    void whenFirstNameInvalid_thenViolations() {
        EmployeeRequestDto dto = createValidDto();
        dto.setFirstName("John123"); // contains numbers, regex is ^[A-Za-z ]+$

        Set<ConstraintViolation<EmployeeRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        boolean hasFirstNamePatternViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")
                        && v.getMessage().equals("First name can contain only letters"));
        assertTrue(hasFirstNamePatternViolation);
    }
}

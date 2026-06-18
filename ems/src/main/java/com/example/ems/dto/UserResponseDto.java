package com.example.ems.dto;


import jakarta.persistence.Column;
import jakarta.validation.Valid;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {



    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;


    private String role;

    private String phone;

    private String status;
}
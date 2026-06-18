package com.example.ems.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    private String email;

    @Column(length = 2000)
    private String token;

    private Boolean isActive;

    private LocalDateTime createdAt;


}

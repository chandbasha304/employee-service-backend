package com.example.ems.controller;

import com.example.ems.dto.UserDto;
import com.example.ems.entity.User;
import com.example.ems.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;




}

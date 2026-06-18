package com.example.ems.service;

import com.example.ems.dto.EmployeeRequestDto;
import com.example.ems.dto.EmployeeResponseDto;
import com.example.ems.dto.UserDto;
import com.example.ems.dto.UserResponseDto;
import com.example.ems.entity.User;

public interface UserService {


    UserResponseDto createUser(
            UserDto userDto
    );
}

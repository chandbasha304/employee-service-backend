package com.example.ems.service.impl;

import com.example.ems.dto.UserDto;
import com.example.ems.dto.UserResponseDto;
import com.example.ems.entity.User;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    @Override
    public UserResponseDto createUser(UserDto userDto) {


        Optional<User> byEmail =

                userRepository.findByEmail(userDto.getEmail());

        if(byEmail.isPresent())
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,String.format("%s Already Exists",userDto.getEmail()));
        }

        long id = System.currentTimeMillis() + new Random().nextInt(1000);

        String encode = passwordEncoder.encode(userDto.getPassword());
        User user = User.builder().id(id).firstName(userDto.getFirstName()).lastName(userDto.getLastName()).email(userDto.getEmail()).password(encode).role(userDto.getRole()).phone(userDto.getPhone()).status(userDto.getStatus()).build();

         userRepository.save(user);
        UserResponseDto userResponseDto = UserResponseDto.builder().firstName(userDto.getFirstName()).lastName(userDto.getLastName()).email(userDto.getEmail()).role(userDto.getRole()).phone(userDto.getPhone()).status(userDto.getStatus()).build();




        return userResponseDto;
    }
}

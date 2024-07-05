package com.example.jmt.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jmt.dto.UserDto;
import com.example.jmt.model.User;
import com.example.jmt.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void userJoin(UserDto userDto) {
        User user = userDto.toEntity();
        user.setPw(passwordEncoder.encode(user.getPw()));
        userRepository.save(user);
    }
}

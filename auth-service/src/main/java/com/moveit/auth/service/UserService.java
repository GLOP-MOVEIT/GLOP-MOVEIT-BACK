package com.moveit.auth.service;

import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.entity.UserAuth;
import com.moveit.auth.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    void init() {
        if (userRepository.findByNickname("admin").isPresent()) {
            return;
        }

        UserAuth admin = new UserAuth()
                .setNickname("admin")
                .setPassword(passwordEncoder.encode("123456"));
        userRepository.save(admin);
    }

    public List<UserAuth> allUsers() {
        return userRepository.findAll();
    }

    public UserAuth createUser(RegisterUserDto input) {
        UserAuth userAuth = new UserAuth()
                .setNickname(input.nickname())
                .setPassword(passwordEncoder.encode(input.password()));

        return userRepository.save(userAuth);
    }
}
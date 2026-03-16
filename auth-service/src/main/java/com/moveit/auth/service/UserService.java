package com.moveit.auth.service;

import com.moveit.auth.dto.CreateUserRequest;
import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.dto.UserCreatedResponse;
import com.moveit.auth.entity.UserAuth;
import com.moveit.auth.feign.UserFeignClient;
import com.moveit.auth.repository.UserAuthRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserFeignClient userFeignClient;

    @Value("${admin.initial.password}")
    private String adminPassword;

    @PostConstruct
    void init() {
        if (userAuthRepository.findByNickname("admin").isPresent()) {
            return;
        }

        UserAuth admin = new UserAuth()
                .setNickname("admin")
                .setPassword(passwordEncoder.encode(adminPassword));
        UserCreatedResponse createdAdmin = userFeignClient.createAdmin(new CreateUserRequest(
                "Admin",
                "Admin",
                "admin@moveit.com",
                "0000000000",
                "fr",
                false,
                false
        ));

        admin.setUserId(createdAdmin.getUserId());
        userAuthRepository.save(admin);
    }

    public List<UserAuth> allUsers() {
        return userAuthRepository.findAll();
    }

    public UserAuth createUser(RegisterUserDto input) {
        UserAuth userAuth = new UserAuth()
                .setNickname(input.nickname())
                .setPassword(passwordEncoder.encode(input.password()));

        return userAuthRepository.save(userAuth);
    }
}
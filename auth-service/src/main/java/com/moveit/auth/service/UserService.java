package com.moveit.auth.service;

import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.RoleEnum;
import com.moveit.auth.entity.User;
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
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    void init() {
        if (userRepository.findByNickname("admin").isPresent()) {
            return;
        }

        roleService.findByName(RoleEnum.ADMIN).ifPresent(role -> {
            User admin = new User()
                    .setNickname("admin")
                    .setPassword(passwordEncoder.encode("123456"))
                    .setRole(role);
            userRepository.save(admin);
        });
    }

    public List<User> allUsers() {
        return userRepository.findAll();
    }

    public User createAdministrator(RegisterUserDto input) {
        Role role = roleService.findByName(RoleEnum.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Admin role not found"));

        User user = new User()
                .setNickname(input.nickname())
                .setPassword(passwordEncoder.encode(input.password()))
                .setRole(role);

        return userRepository.save(user);
    }
}
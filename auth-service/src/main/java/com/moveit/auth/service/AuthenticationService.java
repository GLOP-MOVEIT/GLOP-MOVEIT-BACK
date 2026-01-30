package com.moveit.auth.service;

import com.moveit.auth.dto.LoginUserDto;
import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.RoleEnum;
import com.moveit.auth.entity.User;
import com.moveit.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public User signup(RegisterUserDto input) {
        Role role = roleService.findByName(RoleEnum.SPECTATOR)
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

        User user = new User()
                .setNickname(input.nickname())
                .setPassword(passwordEncoder.encode(input.password()))
                .setRole(role);

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.nickname(), input.password())
        );

        User user = userRepository.findByNickname(input.nickname()).orElseThrow();
        user.setLastConnectionDate(new Date());
        return userRepository.save(user);
    }
}
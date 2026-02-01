package com.moveit.auth.service;

import com.moveit.auth.dto.LoginUserDto;
import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.entity.UserAuth;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserAuth signup(RegisterUserDto input) {
        UserAuth userAuth = new UserAuth()
                .setNickname(input.nickname())
                .setPassword(passwordEncoder.encode(input.password()));

        return userRepository.save(userAuth);
    }

    public UserAuth authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.nickname(), input.password())
        );

        UserAuth userAuth = userRepository.findByNickname(input.nickname()).orElseThrow();
        userAuth.setLastConnectionDate(new Date());
        return userRepository.save(userAuth);
    }
}
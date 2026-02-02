package com.moveit.auth.service;

import com.moveit.auth.dto.LoginUserDto;
import com.moveit.auth.dto.RegisterUserDto;
import com.moveit.auth.entity.UserAuth;
import com.moveit.auth.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserAuth signup(RegisterUserDto input) {
        UserAuth userAuth = new UserAuth()
                .setNickname(input.nickname())
                .setPassword(passwordEncoder.encode(input.password()));

        return userAuthRepository.save(userAuth);
    }

    public UserAuth authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.nickname(), input.password())
        );

        UserAuth userAuth = userAuthRepository.findByNickname(input.nickname()).orElseThrow();
        userAuth.setLastConnectionDate(new Date());
        return userAuthRepository.save(userAuth);
    }
}
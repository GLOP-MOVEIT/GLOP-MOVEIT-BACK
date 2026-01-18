package com.moveit.championship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
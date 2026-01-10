package com.moveit.notification.configuration;

import com.moveit.common.security.SecurityConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SecurityConfiguration.class)
@ComponentScan(basePackages = "com.moveit.common.security")
public class SecurityConfig {
    // Cette classe importe la configuration de sécurité et active le scan des beans du module common
}

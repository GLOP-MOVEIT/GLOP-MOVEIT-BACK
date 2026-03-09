package com.moveit.notification.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Web pour le SSE.
 * Autorise les connexions CORS sur l'endpoint de streaming SSE.
 */
@Configuration
public class SSeConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/notifications/stream/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET")
                .allowCredentials(true);
    }
}

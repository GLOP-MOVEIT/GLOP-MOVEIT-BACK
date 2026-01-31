package com.moveit.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour la pagination des requêtes.
 * Point 10 - Évite les requêtes massives en limitant la taille des pages.
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "pagination")
public class PaginationConfig {
    
    // Valeurs par défaut
    private int defaultPageSize = 10;
    private int maxPageSize = 100;
    private int minPageSize = 1;
    
    // Getters & Setters
    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = Math.max(1, Math.min(defaultPageSize, maxPageSize));
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = Math.max(1, maxPageSize);
    }

    public int getMinPageSize() {
        return minPageSize;
    }

    public void setMinPageSize(int minPageSize) {
        this.minPageSize = Math.max(1, minPageSize);
    }
}

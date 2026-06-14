package com.capstone.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfig(@Value("${cors.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("POST", "GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

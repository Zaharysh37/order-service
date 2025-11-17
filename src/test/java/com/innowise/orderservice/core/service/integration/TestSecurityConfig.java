package com.innowise.orderservice.core.service.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true) // Включите security для @PreAuthorize
public class TestSecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> JwtDecoders.fromIssuerLocation("http://localhost:8083").decode(token);
    }
}
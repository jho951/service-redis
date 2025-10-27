package com.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 적당한 강도(기본 10). 필요하면 new BCryptPasswordEncoder(12) 처럼 올리세요.
        return new BCryptPasswordEncoder();
    }
}
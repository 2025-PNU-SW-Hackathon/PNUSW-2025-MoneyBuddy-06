package com.moneybuddy.moneylog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/users/**").permitAll()
                        .requestMatchers("/api/v1/knowledge/**").permitAll()
                        .requestMatchers("/api/v1/youth-policy/**").permitAll()  // ✅ 요거 추가!
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}

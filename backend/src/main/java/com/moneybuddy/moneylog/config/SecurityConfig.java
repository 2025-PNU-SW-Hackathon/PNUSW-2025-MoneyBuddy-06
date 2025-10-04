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
                        .requestMatchers("/api/v1/users/**").permitAll()        // 회원가입, 로그인 허용
                        .requestMatchers("/api/v1/knowledge/**").permitAll()   // 카드뉴스 API 허용
                        .anyRequest().authenticated()                          // 그 외는 인증 필요
                );

        return http.build();
    }
}

package com.moneybuddy.moneylog.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final Key secretKey;
    private final long expiration = 3600000L; // 1시간

    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmail(String token) {
        return parseToken(token).get("email", String.class);
    }

    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public void validateToken(String token) throws JwtException {
        parseToken(token);
    }

    public String createToken(Long userId, String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("email", email)
                .claim("userId", userId)
                .setId(UUID.randomUUID().toString())         // jti (로그아웃 토큰 차단용)
                .setIssuedAt(new Date(now))                  // 발급 시간
                .setExpiration(new Date(now + expiration))   // 만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Authorization 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    // 토큰 발급 시각
    public Date getIssuedAt(String token) {
        return parseToken(token).getIssuedAt();
    }

    // 토큰 만료 시각
    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    // 토큰 고유 ID(jti)
    public String getJti(String token) {
        return parseToken(token).getId();
    }
}
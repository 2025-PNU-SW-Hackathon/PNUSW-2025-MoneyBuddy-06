package com.moneybuddy.moneylog.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final Key secretKey;
    private final long expiration = 3600000L;

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
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    public void validateToken(String token) throws JwtException {
        parseToken(token);
    }

    public String createToken(Long userId,String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("email", email)
                .claim("userId", userId)
                .setId(UUID.randomUUID().toString())   // jti 추가
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 발급 시간: 비밀번호 변경 시 토큰 비교
    public Date getIssuedAt(String token) {
        Claims claims = parseToken(token);
        return claims.getIssuedAt();
    }

    // jti 반환
    public String getJti(String token) {
        return parseToken(token).getId();
    }

    // 만료시각
    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }
}
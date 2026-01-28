package com.moneybuddy.moneylog.security;

import com.moneybuddy.moneylog.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${spring.jwt.secret}")
    private String secret;

    @Value("${spring.jwt.expiration}")
    private long expirationMillis;

    public String generateAccessToken(User user) {
        long now = System.currentTimeMillis();
        long pwdAt = Optional.ofNullable(user.getPasswordChangedAt())
                .map(dt -> dt.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli())
                .orElse(0L);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId())) // Authentication.getName() = userId 가정
                .claim("pwd_at", pwdAt)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }
}

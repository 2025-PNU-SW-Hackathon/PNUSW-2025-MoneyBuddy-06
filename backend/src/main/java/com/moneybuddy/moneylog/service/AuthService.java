package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.RevokedAccessToken;
import com.moneybuddy.moneylog.jwt.JwtUtil;
import com.moneybuddy.moneylog.repository.RevokedAccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RevokedAccessTokenRepository revokedRepo;

    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더가 유효하지 않습니다.");
        }
        String token = bearerToken.substring(7);

        // 토큰 유효성 검사
        jwtUtil.validateToken(token);

        String jti = jwtUtil.getJti(token);
        Date exp = jwtUtil.getExpiration(token);

        if (jti == null || exp == null) {
            throw new IllegalStateException("토큰 정보가 누락되었습니다.");
        }

        LocalDateTime expiresAt = LocalDateTime.ofInstant(exp.toInstant(), ZoneId.of("Asia/Seoul"));

        // 로그아웃된 토큰이면 스킵 = 이미 블랙리스트에 있으면
        if (!revokedRepo.existsByJti(jti)) {
            try {
                revokedRepo.save(RevokedAccessToken.builder()
                        .jti(jti)
                        .expiresAt(expiresAt)
                        .build());
            } catch (org.springframework.dao.DataIntegrityViolationException ignore) {
                // 동시요청 등으로 이미 저장된 경우 무시
            }
        }
    }
}
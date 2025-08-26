package com.moneybuddy.moneylog.jwt;

import com.moneybuddy.moneylog.repository.RevokedAccessTokenRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RevokedAccessTokenRepository revokedRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // 서명/만료 등 1차 검증
                jwtUtil.validateToken(token);

                // 로그아웃된 토큰인지(JTI) 확인
                String jti = jwtUtil.getJti(token);
                if (jti != null && revokedRepo.existsByJti(jti)) {
                    unauthorized(response, "로그아웃된 토큰입니다.");
                    return;
                }

                Long userId = jwtUtil.getUserId(token);
                String email = jwtUtil.getEmail(token);
                Date iat = jwtUtil.getIssuedAt(token); // 토큰 발급 시각

                var userOpt = userRepository.findById(userId);
                if (userOpt.isEmpty()) {
                    unauthorized(response, "사용자를 찾을 수 없습니다.");
                    return;
                }
                var user = userOpt.get();

                // 비밀번호 변경 요청인지 (해당 요청은 이전 토큰 차단 로직 제외)
                String path = request.getRequestURI();
                boolean isPasswordChange =
                        "PUT".equalsIgnoreCase(request.getMethod())
                                && "/api/v1/users/password".equals(path);

                if (!isPasswordChange && user.getPasswordChangedAt() != null) {
                    // 시스템 타임존 기준으로 비교
                    var changedAtInstant = user.getPasswordChangedAt()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toInstant();

                    if (iat == null || iat.toInstant().isBefore(changedAtInstant)) {
                        unauthorized(response, "로그인이 만료되었습니다. 다시 로그인해 주세요.");
                        return;
                    }
                } // ← ★ 빠져 있던 닫는 중괄호 (이게 없어서 catch without try 오류가 뜸)

                // UTC 기준으로 한 번 더 안전 비교(선택)
                long tokenIssuedAt = Optional.ofNullable(iat)
                        .map(d -> d.toInstant().toEpochMilli())
                        .orElse(0L);

                long passwordChangedAt = Optional.ofNullable(user.getPasswordChangedAt())
                        .map(dt -> dt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
                        .orElse(0L);

                if (!isPasswordChange && passwordChangedAt > tokenIssuedAt) {
                    unauthorized(response, "로그인이 만료되었습니다. 다시 로그인해 주세요.");
                    return;
                }

                // 인증 객체 생성 및 컨텍스트 설정
                var authentication = new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(userId, email),
                        null,
                        Collections.emptyList()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (io.jsonwebtoken.JwtException e) {
                unauthorized(response, "토큰이 유효하지 않습니다.");
                return;
            } catch (Exception e) {
                System.out.println("JWT 필터 오류: " + e.getMessage());
                // 문제 있어도 체인은 흘려보냄(상황에 따라 차단해도 됨)
            }
        }

        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/users/login")
                || path.startsWith("/api/v1/users/signup");
    }
}
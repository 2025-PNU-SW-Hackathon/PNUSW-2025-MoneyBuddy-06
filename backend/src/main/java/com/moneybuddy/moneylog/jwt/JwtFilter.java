package com.moneybuddy.moneylog.jwt;

import com.moneybuddy.moneylog.domain.User;
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


                // 토큰 유효성 검사
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

                // DB에서 유저 조회
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

                // request attribute에 user 저장 (컨트롤러 등에서 바로 꺼내 쓸 수 있음)
                request.setAttribute("user", user);

                // 비밀번호 변경 요청 여부 확인
                String path = request.getRequestURI();
                boolean isPasswordChange =
                        "PATCH".equalsIgnoreCase(request.getMethod()) &&
                                "/api/v1/users/password".equals(path);

                // 비밀번호 변경 요청이 아닐 때만 이전 토큰 차단
                if (!isPasswordChange && user.getPasswordChangedAt() != null) {
                    // 시스템 타임존 기준으로 비교
                    var changedAtInstant = user.getPasswordChangedAt()
                            .atZone(ZoneOffset.UTC)
                            .toInstant();

                    if (iat == null || iat.toInstant().isBefore(changedAtInstant)) {
                        unauthorized(response, "로그인이 만료되었습니다. 다시 로그인해 주세요.");
                        return;
                    }

                    // iat vs passwordChangedAt 비교
                    long tokenIssuedAt = Optional.ofNullable(iat)
                            .map(d -> d.toInstant().toEpochMilli())
                            .orElse(0L);

                    long passwordChangedAt = Optional.ofNullable(user.getPasswordChangedAt())
                            .map(dt -> dt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
                            .orElse(0L);

                    if (passwordChangedAt > tokenIssuedAt) {
                        unauthorized(response, "로그인이 만료되었습니다. 다시 로그인해 주세요.");
                        return;
                    }
                }

                // 인증 객체 생성 및 SecurityContext 저장
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
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

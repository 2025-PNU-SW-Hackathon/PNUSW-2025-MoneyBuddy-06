package com.moneybuddy.moneylog.jwt;

import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.repository.RevokedAccessTokenRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;
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
                jwtUtil.validateToken(token);

                // 로그아웃된 토큰인지 체크
                String jti = jwtUtil.getJti(token);
                if (jti != null && revokedRepo.existsByJti(jti)) {
                    unauthorized(response, "로그아웃된 토큰입니다.");
                    return;
                }

                Long userId = jwtUtil.getUserId(token);
                String email = jwtUtil.getEmail(token);
                Date iat = jwtUtil.getIssuedAt(token);

                // DB에서 password_changed_at 조회
                var userOpt = userRepository.findById(userId);
                if (userOpt.isEmpty()) {
                    unauthorized(response, "사용자를 찾을 수 없습니다.");
                    return;
                }
                var user = userOpt.get();

                // 비밀번호 변경 요청인지
                String path = request.getRequestURI();
                boolean isPasswordChange =
                        "PUT".equalsIgnoreCase(request.getMethod())
                                && "/api/v1/users/password".equals(path);

                // 토큰 발급 시각(iat)과 비번 변경 시각 비교
                long tokenIssuedAt = Optional.ofNullable(jwtUtil.getIssuedAt(token))
                        .map(d -> d.toInstant().toEpochMilli())
                        .orElse(0L);

                long passwordChangedAt = Optional.ofNullable(user.getPasswordChangedAt())
                        .map(dt -> dt.atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli())
                        .orElse(0L);

                // 비번 변경 이후에 발급된 토큰만 통과
                if (passwordChangedAt > tokenIssuedAt) {
                    unauthorized(response, "로그인이 만료되었습니다. 다시 로그인해 주세요.");
                    return;
                }

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
        return path.startsWith("/api/v1/users/login") || path.startsWith("/api/v1/users/signup");
    }
}
package com.moneybuddy.moneylog.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }

        if (principal instanceof Map<?,?> m && m.get("userId") != null) {
            Object v = m.get("userId");
            if (v instanceof Long l) return l;
            if (v instanceof Integer i) return i.longValue();
            return Long.parseLong(v.toString());
        }

        // username을 Long으로 쓸 때
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new AccessDeniedException("사용자 식별 정보를 찾을 수 없습니다.");
        }
    }
}
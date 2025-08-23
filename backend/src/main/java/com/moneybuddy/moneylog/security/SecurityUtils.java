package com.moneybuddy.moneylog.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            throw new AccessDeniedException("로그인이 필요합니다.");

        Object p = auth.getPrincipal();

        if (p instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }
        if (p instanceof java.util.Map<?,?> m && m.get("userId") != null) { // 과거 Map 기반 호환
            Object v = m.get("userId");
            if (v instanceof Long l) return l;
            if (v instanceof Integer i) return i.longValue();
            return Long.parseLong(v.toString());
        }
        throw new AccessDeniedException("사용자 식별 정보를 찾을 수 없습니다.");
    }
}

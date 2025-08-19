package com.moneybuddy.moneylog.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("Unauthenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }
        // username을 Long으로 쓸 때
        try { return Long.parseLong(auth.getName()); }
        catch (NumberFormatException e) {
            throw new RuntimeException("Cannot resolve userId from principal");
        }
    }
}

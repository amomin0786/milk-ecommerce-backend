package com.milk_ecommerce_backend.security;

import com.milk_ecommerce_backend.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecurityActorService {

    public Set<String> roles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Set.of();
        return auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toSet());
    }

    public Long requireUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new ForbiddenException("Unauthenticated");

        Object principal = auth.getPrincipal();
        if (principal == null) throw new ForbiddenException("Invalid principal");

        Long id = tryInvokeLong(principal, "getId");
        if (id == null) id = tryInvokeLong(principal, "getUserId");
        if (id == null) throw new ForbiddenException("UserId not found in principal");

        return id;
    }

    private Long tryInvokeLong(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            Object v = m.invoke(target);
            if (v == null) return null;
            if (v instanceof Number n) return n.longValue();
            return Long.parseLong(String.valueOf(v));
        } catch (Exception ignored) {
            return null;
        }
    }
}
package com.milk_ecommerce_backend.audit;

import com.milk_ecommerce_backend.model.AuditLog;
import com.milk_ecommerce_backend.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository repo;
    private final HttpServletRequest request;

    public AuditAspect(AuditLogRepository repo, HttpServletRequest request) {
        this.repo = repo;
        this.request = request;
    }

    @Around("@annotation(audited)")
    public Object around(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        AuditLog log = new AuditLog();
        fillActor(log);
        fillRequest(log);

        log.setAction(audited.action());
        log.setEntityType(audited.entityType().isBlank() ? null : audited.entityType());
        log.setEntityId(resolveEntityId(pjp, audited.entityIdParam()));

        try {
            Object result = pjp.proceed();
            log.setSuccess(true);
            repo.save(log);
            return result;
        } catch (Exception ex) {
            log.setSuccess(false);
            log.setErrorMessage(trimTo255(ex.getMessage()));
            repo.save(log);
            throw ex;
        }
    }

    private String resolveEntityId(ProceedingJoinPoint pjp, String paramName) {
        if (paramName == null || paramName.isBlank()) return null;

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String[] names = sig.getParameterNames();
        Object[] args = pjp.getArgs();

        for (int i = 0; i < names.length; i++) {
            if (paramName.equals(names[i]) && args[i] != null) {
                return String.valueOf(args[i]);
            }
        }
        return null;
    }

    private void fillActor(AuditLog log) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return;

        log.setActorRoles(auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.joining(",")));

        Object principal = auth.getPrincipal();
        if (principal == null) return;

        // email
        String email = tryInvokeString(principal, "getEmail");
        if (email == null) email = tryInvokeString(principal, "getUsername");
        log.setActorEmail(email);

        // userId
        Long id = tryInvokeLong(principal, "getId");
        if (id == null) id = tryInvokeLong(principal, "getUserId");
        log.setActorUserId(id);
    }

    private void fillRequest(AuditLog log) {
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(trimTo255(request.getHeader("User-Agent")));
        log.setRequestPath(trimTo255(request.getRequestURI()));
        log.setRequestMethod(trimTo255(request.getMethod()));
    }

    private String tryInvokeString(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            Object v = m.invoke(target);
            return v == null ? null : String.valueOf(v);
        } catch (Exception ignored) {
            return null;
        }
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

    private String trimTo255(String s) {
        if (s == null) return null;
        return s.length() <= 255 ? s : s.substring(0, 255);
    }
}
package com.cts.foodchainx.aspect;

import com.cts.foodchainx.model.User;
import com.cts.foodchainx.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Aspect-oriented component responsible for handling cross-cutting audit concerns.
 * <p>
 * This class monitors methods annotated with {@link Auditable} and records 
 * the activity in the system audit logs after the method completes successfully.
 * </p>
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    /**
     * Pointcut advice that runs after any method annotated with {@literal @Auditable} 
     * returns successfully.
     * <p>
     * It retrieves the current authenticated user from the {@link SecurityContextHolder} 
     * and delegates the logging task to the {@link AuditLogService}.
     * </p>
     * * @param joinPoint provides metadata about the intercepted method
     * @param auditable the annotation instance containing action and resource metadata
     */
    @AfterReturning("@annotation(auditable)")
    public void after(JoinPoint joinPoint, Auditable auditable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Pattern matching for instanceof (Java 16+) to extract user principal
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            auditLogService.log(user, auditable.action(), auditable.resource());
        }
    }
}
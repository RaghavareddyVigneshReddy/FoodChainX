package com.cts.foodchainx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuration class to enable global method-level security.
 * <p>
 * This allows the use of annotations like {@literal @PreAuthorize}, {@literal @Secured}, 
 * and JSR-250 annotations to restrict method execution based on user roles.
 * </p>
 */
@Configuration
@EnableMethodSecurity(jsr250Enabled = true, prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig { }
package com.cts.foodchainx.config;

import com.cts.foodchainx.model.User;
import com.cts.foodchainx.enums.UserStatus;
import com.cts.foodchainx.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main Security Configuration for FoodChainX.
 * <p>
 * This class defines the security filter chain, role-based access controls (RBAC),
 * and authentication providers for the Farm-to-Table transparency system.
 * </p>
 *
 * @author FoodChainX Development Team
 * @version 1.1
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // --- Role Constants to Reduce Code Smell ---
    private static final String ROLE_FARMER = "FARMER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_REGULATOR = "REGULATOR";
    private static final String ROLE_DISTRIBUTOR = "DISTRIBUTOR";
    private static final String JSON_CONTENT_TYPE = "application/json";

    /**
     * Bean for password encryption using BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Custom UserDetailsService to load user data from the database.
     * Checks account status (ACTIVE/SUSPENDED) during authentication.
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User u = userRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            boolean enabled = u.getStatus() == UserStatus.ACTIVE;
            boolean accountNonLocked = u.getStatus() != UserStatus.SUSPENDED;

            return org.springframework.security.core.userdetails.User
                    .withUsername(u.getEmail())
                    .password(u.getPasswordHash())
                    .roles(u.getRole().name())
                    .accountLocked(!accountNonLocked)
                    .disabled(!enabled)
                    .build();
        };
    }

    /**
     * Configures the DAO Authentication Provider.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService uds,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Exposes the AuthenticationManager bean for use in login controllers.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * Configures the main Security Filter Chain.
     * Defines stateless session management and role-based URL protection.
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider
    ) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) 
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType(JSON_CONTENT_TYPE);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType(JSON_CONTENT_TYPE);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"status\": 403, \"error\": \"Forbidden\", \"message\": \"Access denied: insufficient permissions\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                // --- NEW: Swagger/OpenAPI Public Endpoints ---
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Publicly accessible paths (Auth, Health, Consumer Portal)
                .requestMatchers("/api/auth/**", "/foodchainx/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/foodchainx/notifications/**", "/notifications/**").permitAll()
                .requestMatchers("/api/trace/**", "/api/consumer/**").permitAll()

                .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)

                // SECURED: Reporting Module
                .requestMatchers("/api/reports/**").authenticated()

                // SECURED: Farmer Module
                .requestMatchers("/api/farms/register/**", "/api/farms/farmer/**").hasRole(ROLE_FARMER)
                .requestMatchers(HttpMethod.POST, "/api/production/add").hasRole(ROLE_FARMER)
                .requestMatchers(HttpMethod.DELETE, "/api/farms/**", "/api/production/**").hasRole(ROLE_FARMER)

                // SECURED: Logistics & Distribution
                .requestMatchers("/api/logistics/**").hasAnyRole(ROLE_DISTRIBUTOR, ROLE_ADMIN)

                // SECURED: Regulatory & Admin Oversights
                .requestMatchers(HttpMethod.PATCH, "/api/farms/*/status").hasAnyRole(ROLE_REGULATOR, ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/quality-checks/inspect").hasAnyRole(ROLE_REGULATOR, ROLE_ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/production/**", "/api/quality-checks/status/**")
                    .hasAnyRole(ROLE_FARMER, ROLE_REGULATOR, ROLE_ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/quality-checks/**").hasAnyRole(ROLE_ADMIN, ROLE_REGULATOR)

                // SECURED: Compliance & Audit Module
                .requestMatchers("/api/compliance/failed").hasRole(ROLE_REGULATOR)
                .requestMatchers(HttpMethod.POST, "/api/compliance/records").hasRole(ROLE_REGULATOR)
                .requestMatchers("/api/compliance/history/**").hasAnyRole(ROLE_REGULATOR, ROLE_ADMIN, ROLE_FARMER)

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
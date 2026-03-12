package com.cts.foodchainx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cts.foodchainx.model.User;
import com.cts.foodchainx.model.UserStatus;
import com.cts.foodchainx.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User u = userRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"status\": 403, \"error\": \"Forbidden\", \"message\": \"You do not have the required permissions\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                // 1. Unified Public Auth & Health Endpoints
                // Added /foodchainx/auth/** to match your actual controller path
                .requestMatchers("/api/auth/**").permitAll() 
                .requestMatchers("/foodchainx/auth/**").permitAll() 
                .requestMatchers("/actuator/health").permitAll()

                // 2. Notifications Module
                .requestMatchers("/foodchainx/notifications/**").permitAll()
                .requestMatchers("/notifications/**").permitAll()

                // 3. Traceability & Consumer Portal
                .requestMatchers("/api/trace/**").permitAll()
                .requestMatchers("/api/consumer/**").permitAll()

                // 4. Reporting Module
                .requestMatchers("/api/reports/**").permitAll()

                // --- Secured Module Endpoints (Role Based) ---
                .requestMatchers("/api/farms/register/**").hasRole("FARMER")
                .requestMatchers("/api/farms/farmer/**").hasRole("FARMER")
                .requestMatchers(HttpMethod.DELETE, "/api/farms/**").hasRole("FARMER")
                .requestMatchers("/api/logistics/shipments/**").hasAnyRole("DISTRIBUTOR", "ADMIN")
                .requestMatchers("/api/logistics/warehouses/**").hasAnyRole("DISTRIBUTOR", "ADMIN")
                .requestMatchers("/api/logistics/deliveries/**").hasAnyRole("DISTRIBUTOR", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/farms/*/status").hasAnyRole("REGULATOR", "ADMIN")

                // Production Module Matchers
                .requestMatchers(HttpMethod.POST, "/api/production/add").hasRole("FARMER")
                .requestMatchers(HttpMethod.DELETE, "/api/production/{id}").hasRole("FARMER")
                .requestMatchers(HttpMethod.GET, "/api/production/**").hasAnyRole("FARMER", "REGULATOR", "ADMIN")

                // Quality Checks Matchers
                .requestMatchers(HttpMethod.POST, "/api/quality-checks/inspect").hasAnyRole("REGULATOR", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/quality-checks/status/**").hasAnyRole("REGULATOR", "ADMIN", "FARMER")
                .requestMatchers(HttpMethod.DELETE, "/api/quality-checks/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
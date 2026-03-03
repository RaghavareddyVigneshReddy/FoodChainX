package com.cts.FoodChainX.config;

import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.model.UserStatus;
import com.cts.FoodChainX.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
        .csrf(csrf -> csrf.disable()) // Mandatory to prevent 403 on POST/PUT
        .cors(Customizer.withDefaults())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authenticationProvider(authenticationProvider)
        .authorizeHttpRequests(auth -> auth
            // 1. Unified Authentication Permits
            .requestMatchers("/api/auth/**").permitAll() 
            .requestMatchers("/actuator/health").permitAll()

            // 2. Notifications & Alerts Module
            .requestMatchers(HttpMethod.GET, "/foodchainx/notifications/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/foodchainx/notifications/**").permitAll()
            .requestMatchers(HttpMethod.PUT, "/foodchainx/notifications/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/foodchainx/notifications/**").permitAll()
            .requestMatchers("/notifications/**").permitAll()

            // 3. Traceability & Reporting Modules (Your new feature/traceability changes)
            .requestMatchers("/api/trace/**").permitAll()
            .requestMatchers("/api/reports/**").permitAll()

            // 4. Secure all other endpoints
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
}
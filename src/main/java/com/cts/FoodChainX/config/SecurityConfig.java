package com.cts.FoodChainX.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.model.UserStatus;
import com.cts.FoodChainX.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Maps your domain User to Spring Security's UserDetails */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User u = userRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            boolean enabled = u.getStatus() == UserStatus.ACTIVE;
            boolean accountNonLocked = u.getStatus() != UserStatus.SUSPENDED;

            UserDetails details = org.springframework.security.core.userdetails.User
                    .withUsername(u.getEmail())
                    .password(u.getPasswordHash())   // MUST be a BCrypt hash
                    .roles(u.getRole().name())        // -> ROLE_<ROLE>
                    .accountLocked(!accountNonLocked)
                    .disabled(!enabled)
                    .build();
            return details;
        };
    }

    /** Use the no-arg provider and set both dependencies explicitly */
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
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth
                // 1. PUBLIC ENDPOINTS (Already there)
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // 2. ✅ INSERT YOUR FARM ENDPOINTS HERE
                // Restrict based on the roles defined in your User model
                .requestMatchers("/api/farms/register/**").hasRole("FARMER")
                .requestMatchers("/api/farms/farmer/**").hasRole("FARMER")
                .requestMatchers(HttpMethod.DELETE, "/api/farms/**").hasRole("FARMER")
                
                // Allow Regulators or Admins to update the certification status
                .requestMatchers(HttpMethod.PATCH, "/api/farms/*/status").hasAnyRole("REGULATOR", "ADMIN")

                // 3. CATCH-ALL (Everything else is locked)
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
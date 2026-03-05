package com.cts.FoodChainX.config;

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

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User u = userRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
           // System.out.println("Login attempt: " + u.getEmail() + " | Role: " + u.getRole() + " | Status: " + u.getStatus());
            
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
        // Added from teammate: Ensures no session is stored on server (JWT standard)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authenticationProvider(authenticationProvider)
        .authorizeHttpRequests(auth -> auth
            // 1. Unified Public Auth & Health Endpoints
            .requestMatchers("/api/auth/**").permitAll() 
            .requestMatchers("/actuator/health").permitAll()

            // 2. Notifications Module (Merged from teammate)
            // Permitting these allows the notification system to trigger alerts freely
            .requestMatchers("/foodchainx/notifications/**").permitAll()
            .requestMatchers("/notifications/**").permitAll()

            // 3. Traceability & Consumer Portal (Merged & Secured)
            // Note: Use permitAll() for public trace or hasAnyRole for secured portal
            .requestMatchers("/api/trace/**").permitAll()
            .requestMatchers("/api/consumer/**").hasAnyRole("CONSUMER", "ADMIN")

            // 4. Reporting Module
            .requestMatchers("/api/reports/**").permitAll()

                            // Restrict based on the roles defined in your User model
                .requestMatchers("/api/farms/register/**").hasRole("FARMER")
                .requestMatchers("/api/farms/farmer/**").hasRole("FARMER")
                .requestMatchers(HttpMethod.DELETE, "/api/farms/**").hasRole("FARMER")
                .requestMatchers("/api/logistics/shipments/**").hasAnyRole("DISTRIBUTOR", "ADMIN")

                 // Secures warehouse capacity monitoring
                 .requestMatchers("/api/logistics/warehouses/**").hasAnyRole("DISTRIBUTOR", "ADMIN")

                  // Secures delivery logging
                .requestMatchers("/api/logistics/deliveries/**").hasAnyRole("DISTRIBUTOR", "ADMIN")
                
                // Allow Regulators or Admins to update the certification status
                .requestMatchers(HttpMethod.PATCH, "/api/farms/*/status").hasAnyRole("REGULATOR", "ADMIN")
                

                // Insert these matchers into your authorizeHttpRequests(auth -> { ... }) block

// 1. Only Farmers can create new batches
                   .requestMatchers(HttpMethod.POST, "/api/production/add").hasRole("FARMER")

// 2. Only Farmers can delete batches
                 .requestMatchers(HttpMethod.DELETE, "/api/production/{id}").hasRole("FARMER")

// 3. Farmers can view their own, Regulators/Admins can view for inspections
                   .requestMatchers(HttpMethod.GET, "/api/production/**").hasAnyRole("FARMER", "REGULATOR", "ADMIN")
            // 5. Secure all other endpoints


         // 1. Only Regulators or Admins can perform the actual inspection
               .requestMatchers(HttpMethod.POST, "/api/quality-checks/inspect").hasAnyRole("REGULATOR", "ADMIN")

// 2. Regulators, Admins, and Farmers can view inspection results (Farmers need to see if their batch passed)
                  .requestMatchers(HttpMethod.GET, "/api/quality-checks/status/**").hasAnyRole("REGULATOR", "ADMIN", "FARMER")

// 3. Only Admins should ideally delete quality logs to maintain audit integrity
                    .requestMatchers(HttpMethod.DELETE, "/api/quality-checks/**").hasRole("ADMIN")





            .anyRequest().authenticated()
        )
        // JWT filter must process the request before Spring's internal auth filter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
}
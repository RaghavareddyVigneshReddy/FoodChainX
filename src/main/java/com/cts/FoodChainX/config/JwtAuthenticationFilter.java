package com.cts.FoodChainX.config;

import com.cts.FoodChainX.model.User;
import com.cts.FoodChainX.repository.UserRepository;
import com.cts.FoodChainX.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtService.parseToken(token);
                String email = claims.getSubject();

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
                    if (user != null && jwtService.isTokenValid(token, user)) {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                user, null,
                                List.of(
                                   new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                       "ROLE_" + user.getRole().name()
                                   )
                                )
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception ignored) {
                // token parse/validation failed → proceed without auth
            }
        }
        filterChain.doFilter(request, response);
    }
}
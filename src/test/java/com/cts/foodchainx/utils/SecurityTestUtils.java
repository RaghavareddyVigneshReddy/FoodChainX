package com.cts.foodchainx.utils;

import com.cts.foodchainx.model.User;
import com.cts.foodchainx.enums.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityTestUtils {
    public static void setCustomUser(Long id, String email, Role role) {
        User user = User.builder()
                .userId(id)
                .email(email)
                .role(role)
                .build();
        
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}

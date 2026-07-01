package com.christnow.devotionals.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static String resolveEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        String name = authentication.getName();
        if (name != null && !name.isBlank() && !"anonymousUser".equalsIgnoreCase(name)) {
            return name;
        }

        return null;
    }
}

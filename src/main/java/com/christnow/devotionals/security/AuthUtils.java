package com.christnow.devotionals.security;

import jakarta.servlet.http.HttpServletRequest;
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
            return normalizeEmail(userDetails.getUsername());
        }

        String name = authentication.getName();
        if (name != null && !name.isBlank() && !"anonymousUser".equalsIgnoreCase(name)) {
            return normalizeEmail(name);
        }

        return null;
    }

    public static String resolveEmail(Authentication authentication, HttpServletRequest request, JwtUtil jwtUtil) {
        String email = resolveEmail(authentication);
        if (email != null) {
            return email;
        }
        return extractEmailFromBearer(request, jwtUtil);
    }

    public static String extractEmailFromBearer(HttpServletRequest request, JwtUtil jwtUtil) {
        if (request == null || jwtUtil == null) {
            return null;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        try {
            return normalizeEmail(jwtUtil.extractUsername(authHeader.substring(7).trim()));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }
}

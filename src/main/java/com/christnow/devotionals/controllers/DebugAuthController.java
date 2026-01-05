package com.christnow.devotionals.controllers;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/debug")
public class DebugAuthController {


    // This endpoint MUST work even when you're NOT logged in.
    // It tells us whether the Authorization header is arriving at the server.
    @GetMapping("/auth-header")
    public ResponseEntity<String> authHeader(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank()) {
            return ResponseEntity.ok("Authorization header = null");
        }
        return ResponseEntity.ok("Authorization header = " + auth);
    }


    // This endpoint requires auth; it tells us what Spring Security thinks you are.
    @GetMapping("/whoami")
    public ResponseEntity<String> whoami() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return ResponseEntity.ok("Authentication = null");
        return ResponseEntity.ok(
                "Authentication.name=" + a.getName() + " | isAuthenticated=" + a.isAuthenticated()
        );
    }
}

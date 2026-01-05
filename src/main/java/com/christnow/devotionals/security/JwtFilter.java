package com.christnow.devotionals.security;


import java.io.IOException;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
public class JwtFilter extends OncePerRequestFilter {


    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;


    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        final String path = request.getServletPath();


        // Allow preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }


        // Do not require JWT for login/register
        if ("/users/login".equals(path) || "/users/register".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }


        final String authHeader = request.getHeader("Authorization");


        // If no token, just continue (Security will block later if endpoint needs auth)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


        final String jwt = authHeader.substring(7).trim();


        String subject;
        try {
            subject = jwtUtil.extractUsername(jwt); // subject = email in your setup
        } catch (Exception e) {
            response.setHeader("X-JWT-FAIL", "Token parse failed");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }


        if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(subject);
            } catch (Exception e) {
                response.setHeader("X-JWT-FAIL", "User not found for token subject");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token user");
                return;
            }


            boolean ok;
            try {
                ok = jwtUtil.validateToken(jwt, subject);
            } catch (Exception e) {
                response.setHeader("X-JWT-FAIL", "Token validate exception");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token validation");
                return;
            }


            if (ok) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );


                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                response.setHeader("X-JWT-FAIL", "validateToken returned false");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        }


        filterChain.doFilter(request, response);
    }
}

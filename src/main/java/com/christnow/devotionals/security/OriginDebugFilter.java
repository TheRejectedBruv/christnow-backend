package com.christnow.devotionals.security;


import java.io.IOException;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OriginDebugFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String path = request.getRequestURI();
        if (path != null && path.contains("/users/login")) {
            String origin = request.getHeader("Origin");
            String acrm = request.getHeader("Access-Control-Request-Method");
            String acrh = request.getHeader("Access-Control-Request-Headers");


            System.out.println("=== LOGIN DEBUG ===");
            System.out.println("Method: " + request.getMethod());
            System.out.println("URI: " + request.getRequestURI());
            System.out.println("Origin: " + origin);
            System.out.println("AC-Req-Method: " + acrm);
            System.out.println("AC-Req-Headers: " + acrh);
            System.out.println("===================");
        }


        filterChain.doFilter(request, response);
    }
}

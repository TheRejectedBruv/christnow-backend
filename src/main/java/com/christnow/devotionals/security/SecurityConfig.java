package com.christnow.devotionals.security;


import java.util.List;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;




@Configuration
public class SecurityConfig {


    private final JwtFilter jwtFilter;


    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // IMPORTANT: explicitly use our CorsConfigurationSource
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/users/register","/users/login","/api/users/register", "/api/users/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/courses").permitAll()
                .requestMatchers(HttpMethod.GET, "/courses/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/courses/exists").permitAll()
                .requestMatchers(HttpMethod.GET, "/lessons/by-course/**").permitAll()
                .requestMatchers("/payment/webhook", "/api/payment/webhook").permitAll()
                .requestMatchers("/courses/**").authenticated()
                .requestMatchers("/devotionals/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler(corsAwareAccessDeniedHandler())
            );


        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();


        // Use patterns so you don't get stuck on exact-match Origin issues
        config.setAllowedOriginPatterns(List.of(
            "https://christnow.co",
            "https://www.christnow.co",
            "https://*.pages.dev",
            "https://*.netlify.app",
            "https://*.netlify.com",
            "http://localhost:*",
            "http://127.0.0.1:*"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));


        // Allow the headers browsers commonly send (especially for JSON + auth)


        // Optional: lets frontend read Authorization header if you return JWT there
        config.setExposedHeaders(List.of("Authorization"));


        // Keep false if you're using JWT in localStorage + Authorization header (no cookies)
        config.setAllowCredentials(false);


        config.setMaxAge(3600L);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private AccessDeniedHandler corsAwareAccessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.access.AccessDeniedException accessDeniedException) -> {
            applyCorsHeaders(request, response);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Forbidden. Sign in again, then retry.");
        };
    }

    private void applyCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin == null || origin.isBlank()) {
            return;
        }
        List<String> allowedPatterns = List.of(
                "https://christnow.co",
                "https://www.christnow.co"
        );
        boolean allowed = allowedPatterns.contains(origin)
                || origin.endsWith(".pages.dev")
                || origin.endsWith(".netlify.app")
                || origin.endsWith(".netlify.com")
                || origin.startsWith("http://localhost:")
                || origin.startsWith("http://127.0.0.1:");
        if (allowed) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
        }
    }


}

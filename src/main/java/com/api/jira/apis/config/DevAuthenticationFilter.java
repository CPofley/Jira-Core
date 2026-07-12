package com.api.jira.apis.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class DevAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 1. Look for our magic developer string instead of a real Google Token
        if ("Bearer dev-mode".equals(authHeader)) {

            // 2. Build a fake token using your exact database user details
            Jwt mockJwt = Jwt.withTokenValue("dev-mode")
                    .header("alg", "none")
                    .claim("email", "boradkarsankalp6@gmail.com")
                    .claim("name", "sankalp boradkar")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(360000)) // Expires in 100 hours!
                    .build();

            JwtAuthenticationToken mockAuth = new JwtAuthenticationToken(
                    mockJwt,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(mockAuth);
            HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return null; // Makes the strict filter think there is no token!
                    }
                    return super.getHeader(name);
                }
            };
            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        // Continue the filter chain normally for all other requests
        filterChain.doFilter(request, response);
    }
}

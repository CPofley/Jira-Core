package com.api.jira.apis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final DevAuthenticationFilter devAuthenticationFilter;

    // Inject our new backdoor filter
    public SecurityConfig(DevAuthenticationFilter devAuthenticationFilter) {
        this.devAuthenticationFilter = devAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF completely for local REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Attach our custom CORS configuration source right into the security filter chain
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // --- INJECT THE BACKDOOR HERE ---
                .addFilterBefore(devAuthenticationFilter, BearerTokenAuthenticationFilter.class)

                // 3. Open up Swagger, but LOCK DOWN your API routes
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/tasks/**").authenticated() // Require login for tasks!
                        .requestMatchers("/api/users/**").authenticated() // Require login for user sync!
                        .anyRequest().authenticated()
                )

                // 4. THIS IS THE MAGIC LINE YOU WERE MISSING:
                // It tells Spring to actually parse the Bearer token and create the JwtAuthenticationToken!
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow your Vite React Dev server port explicitly
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply globally to all paths
        return source;
    }
}
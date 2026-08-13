package com.api.jira.apis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final DevAuthenticationFilter devAuthenticationFilter;

    public SecurityConfig(DevAuthenticationFilter devAuthenticationFilter) {
        this.devAuthenticationFilter = devAuthenticationFilter;
    }


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/api/github/webhook", "/webhook");
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Instantiate the default resolver to delegate to when not hitting webhooks
        DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(devAuthenticationFilter, BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/users/sync").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/github/webhook", "/webhook").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/github/webhook", "/webhook").permitAll()
                        .requestMatchers("/api/tasks/**").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/github/webhook", "/webhook").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(request -> {
                            String path = request.getRequestURI();
                            if ("/api/github/webhook".equals(path) || "/webhook".equals(path)) {
                                return null; // Skip Bearer token validation for Webhook requests
                            }
                            return defaultResolver.resolve(request);
                        })
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://192.168.1.6.nip.io:5173",
                "http://192.168.1.6:5173",
                "http://3.110.217.23",
                "http://3.110.217.23:3000",
                "http://3.110.217.23:8080"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
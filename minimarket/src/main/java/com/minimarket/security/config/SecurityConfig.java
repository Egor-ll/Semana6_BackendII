package com.minimarket.security.config;

import com.minimarket.security.filter.JwtAuthenticationFilter;
import com.minimarket.security.filter.RateLimitingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RateLimitingFilter rateLimitingFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; object-src 'none'; frame-ancestors 'self'"))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000)
                        )
                )
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                    .requestMatchers("/auth/login").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/actuator/info").permitAll()
                    .requestMatchers("/h2-console/**").permitAll() // Perimitod solo para ingresar datos de prueba
                    .requestMatchers("/api/public").permitAll()
                    .requestMatchers("/public/**").permitAll()

                    .requestMatchers(HttpMethod.GET, "/api/productos/**")
                    .hasAnyRole("CLIENTE", "EMPLEADO", "ADMINISTRADOR")

                    .requestMatchers(HttpMethod.GET, "/api/categorias/**")
                    .hasAnyRole("CLIENTE", "EMPLEADO", "ADMINISTRADOR")

                    .requestMatchers("/api/usuarios/**")
                    .hasRole("ADMINISTRADOR")

                    .requestMatchers("/api/inventario/**")
                    .hasAnyRole("EMPLEADO", "ADMINISTRADOR")

                    .requestMatchers("/api/ventas/**")
                    .hasAnyRole("CLIENTE", "EMPLEADO", "ADMINISTRADOR")

                    .requestMatchers("/api/detalle-ventas/**")
                    .hasAnyRole("CLIENTE", "EMPLEADO", "ADMINISTRADOR")

                    .requestMatchers("/api/carrito/**")
                    .hasAnyRole("CLIENTE", "EMPLEADO", "ADMINISTRADOR")

                    .anyRequest().authenticated()
                )
                .addFilterBefore(
                    rateLimitingFilter,
                    UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
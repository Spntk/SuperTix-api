package com.supertix.api.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.supertix.api.filter.JwtFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs-ui")
                        .permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/auth/refresh-token").authenticated()

                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/user/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/venue/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/venue/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/venue/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/venue/**").hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/event/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/event/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/event/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/event/**").hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/zone/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/zone/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/zone/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/zone/**").hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/seat/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/seat/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/seat/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/seat/**").hasAuthority("ADMIN")

                        .requestMatchers("/order/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/ticket/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/ticket/**").hasAuthority("ADMIN")

                        .requestMatchers("/notification/**").authenticated()

                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

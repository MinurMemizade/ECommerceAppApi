package com.company.ecommercebackend.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  private final JWTRequestFilter jwtRequestFilter;

  // Constructor to inject JWTRequestFilter
  public WebSecurityConfig(JWTRequestFilter jwtRequestFilter) {
    this.jwtRequestFilter = jwtRequestFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()));
    http.addFilterBefore(jwtRequestFilter, AuthorizationFilter.class);
    http.authorizeHttpRequests(authorization -> authorization
            .requestMatchers("/product", "/auth/register", "/auth/login", "/auth/verify",
                    "/auth/forgot", "/auth/reset", "/error",
                    "/websocket","/websocket/**").permitAll()
            .anyRequest().authenticated()
    );

    return http.build();
  }
}

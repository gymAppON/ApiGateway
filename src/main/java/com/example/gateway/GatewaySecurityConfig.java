package com.example.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                // Disabled CSRF
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Disabled CORS spring additions
                .cors(ServerHttpSecurity.CorsSpec::disable)

                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/login/**", "/oauth2/**", "/auth/**").permitAll()
                        .anyExchange().permitAll()
                );
                //.oauth2Login(Customizer.withDefaults());

        return http.build();
    }
}

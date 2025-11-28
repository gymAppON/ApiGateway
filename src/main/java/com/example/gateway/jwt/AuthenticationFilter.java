package com.example.gateway.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;
    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (validator.isSecured.test(request)) {

                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7); // Убираем "Bearer "
                } else {
                    return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
                }

                // Token validation
                try {
                    jwtUtil.validateToken(authHeader);

                    // Set user id into header
                    // Can be used in UserManager to not extract it from jwt manually or somehow
                    Claims claims = jwtUtil.getAllClaimsFromToken(authHeader);
                    String userId = claims.getSubject();

                    request = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .build();

                } catch (Exception e) {
                    System.out.println("Invalid access token");
                    return onError(exchange, "Unauthorized access", HttpStatus.UNAUTHORIZED);
                }
            }

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config { }
}
package com.hireready.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                // Auth Service
                .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters(f -> f
                        .requestRateLimiter(config -> config
                                .setRateLimiter(redisRateLimiter())
                                .setKeyResolver(userKeyResolver())
                        )
                )
                .uri("http://auth-service:8081")
                )

                .route("resume-upload", r -> r
                .path("/api/candidates/*/resume")
                .filters(f -> f
                        .requestRateLimiter(config -> config
                                .setRateLimiter(redisRateLimiter())
                                .setKeyResolver(userKeyResolver())
                        )
                )
                .uri("http://candidate-service:8082")
                )

                .route("interview-start", r -> r
                .path("/api/interviews/start")
                .filters(f -> f
                        .requestRateLimiter(config -> config
                                .setRateLimiter(redisRateLimiter())
                                .setKeyResolver(userKeyResolver())
                        )
                )
                .uri("http://interview-service:8083")
                )

                .route("interview-submit", r -> r
                .path("/api/interviews/submit")
                .filters(f -> f
                        .requestRateLimiter(config -> config
                                .setRateLimiter(redisRateLimiter())
                                .setKeyResolver(userKeyResolver())
                        )
                )
                .uri("http://interview-service:8083")
                )

                 // Generic Candidate Routes
                .route("candidate-service", r -> r
                        .path("/api/candidates/**")
                        .uri("http://candidate-service:8082")
                )

                // Generic Interview Service
                .route("interview-service", r -> r
                        .path("/api/interviews/**")
                        .uri("http://interview-service:8083")
                )


                .build();
        }

        @Bean
        public KeyResolver userKeyResolver() {
        return exchange -> {
                String userId = exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-User-Id");

                if (userId == null) {
                userId = exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress();
                }

                return Mono.just(userId);
        };
}

@Bean
public RedisRateLimiter redisRateLimiter() {
    return new RedisRateLimiter(5, 10);
}

}


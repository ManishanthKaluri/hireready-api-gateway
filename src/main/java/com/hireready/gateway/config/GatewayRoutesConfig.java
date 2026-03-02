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
                                .setRateLimiter(authRateLimiter())
                                .setKeyResolver(userKeyResolver())
                        )
                )
                .uri("http://localhost:8081")
                )

                .route("resume-upload", r -> r
                .path("/api/candidates/*/resume")
                .filters(f -> f
                        .requestRateLimiter(config -> config
                                .setRateLimiter(resumeRateLimiter())
                                .setKeyResolver(userKeyResolver())
                        )
                )
                .uri("http://localhost:8082")
                )

                .route("interview-start", r -> r
                .path("/api/interviews/start")
                .filters(f -> f
                        .requestRateLimiter(config -> config
                                .setRateLimiter(interviewStartRateLimiter())
                                .setKeyResolver(userKeyResolver())
                        )
                )
                .uri("http://localhost:8083")
                )

                .route("interview-submit", r -> r
                .path("/api/interviews/submit")
                .filters(f -> f
                        .requestRateLimiter(config -> config
                                .setRateLimiter(interviewSubmitRateLimiter())
                                .setKeyResolver(userKeyResolver())
                        )
                )
                .uri("http://localhost:8083")
                )

                 // Generic Candidate Routes
                .route("candidate-service", r -> r
                        .path("/api/candidates/**")
                        .uri("http://localhost:8082")
                )

                // Generic Interview Service
                .route("interview-service", r -> r
                        .path("/api/interviews/**")
                        .uri("http://localhost:8083")
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

public RedisRateLimiter authRateLimiter() {
    return new RedisRateLimiter(3, 6);
}

public RedisRateLimiter resumeRateLimiter() {
    return new RedisRateLimiter(1, 2);
}

public RedisRateLimiter interviewStartRateLimiter() {
    return new RedisRateLimiter(2, 5);
}

public RedisRateLimiter interviewSubmitRateLimiter() {
    return new RedisRateLimiter(3, 6);
}

}


package org.mrshoffen.tasktracker.apigateway;

import org.mrshoffen.tasktracker.apigateway.security.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfiguration {

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20);
    }

    @Bean
    KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           JwtAuthorizationFilter jwtAuthorizationFilter,
                                           @Value("${gateway.api-prefix}") String apiPrefix) {

        return builder
                .routes()
                // Auth service
                .route("auth-service", r -> r
                        .path(apiPrefix + "/auth/**")
                        .filters(f -> f.rewritePath(apiPrefix + "/(?<segment>.*)", "/${segment}"))
                        .uri("lb://authentication-ws"))

                // User registration
                .route("user-registration", r -> r
                        .path(apiPrefix + "/users")
                        .and().method(HttpMethod.POST)
                        .filters(f -> f.rewritePath(apiPrefix + "/(?<segment>.*)", "/${segment}"))
                        .uri("lb://user-profile-ws"))

                // Current user
                .route("current-user", r -> r
                        .path(apiPrefix + "/users/me")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f
                                .rewritePath(apiPrefix + "/(?<segment>.*)", "/${segment}")
                                .filter(jwtAuthorizationFilter))
                        .uri("lb://user-profile-ws"))

                // Current user settings
                .route("current-user-settings", r -> r
                        .path(apiPrefix + "/users/me/settings")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f
                                .rewritePath(apiPrefix + "/(?<segment>.*)", "/${segment}")
                                .filter(jwtAuthorizationFilter))

                        .uri("lb://user-settings-ws"))

                // Check user status
                .route("check-user", r -> r
                        .path(apiPrefix + "/users/status")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.rewritePath(apiPrefix + "/(?<segment>.*)", "/${segment}"))
                        .uri("lb://user-profile-ws"))
                .build();

    }


}

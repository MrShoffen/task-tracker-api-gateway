package org.mrshoffen.tasktracker.apigateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GatewayConfiguration.class);

    @Bean
    public RedisRateLimiter redisRateLimiter(@Value("${app.gateway.rate-limiter.replenish}") Integer replenishRate,
                                             @Value("${app.gateway.rate-limiter.burst}") Integer burstCapacity,
                                             @Value("${app.gateway.rate-limiter.requested}") Integer requestedToken
    ) {
        System.out.println("replenishRate: " + replenishRate);;
        return new RedisRateLimiter(replenishRate, burstCapacity, requestedToken);
    }

    @Bean
    KeyResolver ipKeyResolver() {
        return exchange -> {
            String hostAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just(
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());

        };
    }

}

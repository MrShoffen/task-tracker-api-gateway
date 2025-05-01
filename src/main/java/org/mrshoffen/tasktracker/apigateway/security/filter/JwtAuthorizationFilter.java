package org.mrshoffen.tasktracker.apigateway.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mrshoffen.tasktracker.apigateway.security.service.JwtSignatureValidator;
import org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class JwtAuthorizationFilter extends AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config> {

    private final JwtSignatureValidator jwtValidator;

    private final ObjectMapper objectMapper;

    public JwtAuthorizationFilter(JwtSignatureValidator jwtValidator, ObjectMapper objectMapper) {
        super(Config.class);
        this.jwtValidator = jwtValidator;
        this.objectMapper = objectMapper;
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                ServerHttpRequest request = exchange.getRequest();
                HttpCookie accessToken = request.getCookies().getFirst(AuthenticationAttributes.ACCESS_TOKEN_COOKIE_NAME);
                if (accessToken == null) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED, "Отсутствует jwt access токен");
                }
                Map<String, String> payload = jwtValidator.validateAndExtractPayload(accessToken.getValue());
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(AuthenticationAttributes.AUTHORIZED_USER_HEADER_NAME, payload.get("userId"))
                        .build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception ex) {
                return onError(exchange, HttpStatus.UNAUTHORIZED, ex.getMessage());
            }
        };
    }

    @SneakyThrows
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus, String message) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(httpStatus, message);

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(objectMapper.writeValueAsString(problem).getBytes())));
    }
}

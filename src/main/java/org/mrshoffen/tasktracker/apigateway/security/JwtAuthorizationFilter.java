package org.mrshoffen.tasktracker.apigateway.security;

import org.mrshoffen.tasktracker.apigateway.security.service.JwtSignatureValidator;
import org.mrshoffen.tasktracker.commons.web.authentication.AuthenticationAttributes;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthorizationFilter extends AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config> {

    private final JwtSignatureValidator jwtValidator;

    public JwtAuthorizationFilter(JwtSignatureValidator jwtValidator) {
        super(Config.class);
        this.jwtValidator = jwtValidator;
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                ServerHttpRequest request = exchange.getRequest();
                HttpCookie accessToken = request.getCookies().getFirst(AuthenticationAttributes.ACCESS_TOKEN_COOKIE_NAME);
                Map<String, String> payload = jwtValidator.validateAndExtractPayload(accessToken.getValue());
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(AuthenticationAttributes.AUTHORIZED_USER_HEADER_NAME, payload.get("userId"))
                        .build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception ex){
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Некорректный access токен");
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus, String message) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", httpStatus.toString());
        errorResponse.put("error", httpStatus.getReasonPhrase());
        errorResponse.put("message", message);

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(toJson(errorResponse).getBytes())));
    }

    private String toJson(Map<String, String> map) {
        return map.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                .collect(Collectors.joining(",", "{", "}"));
    }
}

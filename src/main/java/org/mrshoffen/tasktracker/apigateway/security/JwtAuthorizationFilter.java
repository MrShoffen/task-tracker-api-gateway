package org.mrshoffen.tasktracker.apigateway.security;

import org.mrshoffen.tasktracker.apigateway.security.exception.InvalidJwsSignatureException;
import org.mrshoffen.tasktracker.apigateway.security.service.JwtService;
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


    private final JwtService jwtService;

    public JwtAuthorizationFilter(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            HttpCookie accessToken = request.getCookies().getFirst("accessToken");

            if (accessToken == null) {
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Отсутствует access токен");
            }

            try {
                Map<String, String> payload = jwtService.validateAndExtractPayload(accessToken.getValue());

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-ID", payload.get("userId")) // Пример: ID пользователя
                        .header("X-User-Email", payload.get("userEmail")) // Пример: роли
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (InvalidJwsSignatureException ex){
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Некорректный access токен");
            }

//            return chain.filter(exchange);
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

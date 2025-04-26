package org.mrshoffen.tasktracker.apigateway.security.exception;

public class JwtExpiredException extends RuntimeException {
    public JwtExpiredException(String message) {
        super(message);
    }
}

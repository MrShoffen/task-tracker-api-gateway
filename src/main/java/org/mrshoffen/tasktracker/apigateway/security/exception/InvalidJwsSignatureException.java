package org.mrshoffen.tasktracker.apigateway.security.exception;

public class InvalidJwsSignatureException extends RuntimeException{
    public InvalidJwsSignatureException(String message) {
        super(message);
    }

    public InvalidJwsSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}

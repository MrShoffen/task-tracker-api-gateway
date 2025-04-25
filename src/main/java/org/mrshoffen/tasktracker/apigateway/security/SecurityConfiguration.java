package org.mrshoffen.tasktracker.apigateway.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import org.mrshoffen.tasktracker.apigateway.security.service.JwtSignatureValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.ParseException;

@Configuration
public class SecurityConfiguration {

    @Bean
    JwtSignatureValidator jwtValidator(@Value("${jwt-user.keys.access-token-key}") String accessKey) throws ParseException, JOSEException {
        return new JwtSignatureValidator(new MACVerifier(OctetSequenceKey.parse(accessKey)));
    }
}

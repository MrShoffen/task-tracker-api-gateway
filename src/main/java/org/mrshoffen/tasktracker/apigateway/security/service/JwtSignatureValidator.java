package org.mrshoffen.tasktracker.apigateway.security.service;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.tasktracker.apigateway.security.exception.InvalidJwsSignatureException;

import java.text.ParseException;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtSignatureValidator {

    private final JWSVerifier jwsVerifier;

    public Map<String, String> validateAndExtractPayload(String accessToken) throws InvalidJwsSignatureException {
        try {
            if (accessToken == null) {
                throw new InvalidJwsSignatureException("Отсутствует access токен");
            }

            var signedJWT = SignedJWT.parse(accessToken);

            if (signedJWT.verify(this.jwsVerifier)) {
                var claimsSet = signedJWT.getJWTClaimsSet();
                return extractPayload(claimsSet);
            } else {
                throw new InvalidJwsSignatureException("Некорретная подпись access токена");
            }
        } catch (ParseException | JOSEException exception) {
            throw new InvalidJwsSignatureException("Некорретный access токен", exception);
        }
    }

    private Map<String, String> extractPayload(JWTClaimsSet claimsSet) {
        return claimsSet.getClaims().entrySet().stream()
                .filter(entry -> entry.getValue() instanceof String)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (String) entry.getValue()
                ));
    }


}

package com.ist.idp.controller;

import com.ist.idp.security.jwt.JwtService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwksController {

    private final JwtService jwtService;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        // Retrieve the public key AND the stable keyId from our JwtService
        RSAPublicKey publicKey = (RSAPublicKey) jwtService.getPublicKey();
        String keyId = jwtService.getKeyId();

        RSAKey jwk = new RSAKey.Builder(publicKey)
                .keyID(keyId)
                .build();

        return new JWKSet(jwk).toJSONObject();
    }
}

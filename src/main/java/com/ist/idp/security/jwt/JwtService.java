package com.ist.idp.security.jwt;

import com.ist.idp.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.access-token.expiration-ms}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String keyId;

    @PostConstruct
    public void init() {
        try {
            // Generate an RSA key pair (2048 bits)
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
            keyId = UUID.randomUUID().toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing RSA key pair", e);
        }
    }

    //  Public method to expose the public key for the JWKS endpoint
    public PublicKey getPublicKey() {
        return publicKey;
    }

    //  Getter for the keyId
    public String getKeyId() {
        return keyId;
    }

    public String generateAccessToken(User user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String role = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(roleName -> roleName.replace("ROLE_", ""))
                .orElseThrow();
        claims.put("role", role);

        return createToken(claims, user.getEmail(), accessTokenExpiration);
    }

    public String generateRefreshToken(User user) {
        return createToken(new HashMap<>(), user.getEmail(), refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(privateKey, SignatureAlgorithm.RS256) // Sign with the private key
                .compact();
    }

    // --- We will add token validation and claim extraction methods later ---
}

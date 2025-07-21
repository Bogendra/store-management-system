package com.store.inventory.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtSecretValidator {

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void validateSecret() {
        // Print a hash of the secret for validation
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        String algorithm = SignatureAlgorithm.HS256.getJcaName();
        
        // Log the encoded representation for comparison
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("JWT Secret Hash: " + encodedKey.substring(0, 10) + "..." + 
                           " Algorithm: " + algorithm);
    }
}

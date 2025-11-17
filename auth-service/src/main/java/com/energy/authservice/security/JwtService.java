package com.energy.authservice.security;

import com.energy.authservice.entity.Credential;
import com.energy.authservice.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMillis;

    public JwtToken generateToken(Credential credential) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationMillis);
        String token = Jwts.builder()
                .setSubject(credential.getUsername())
                .claim("uid", credential.getId().toString())
                .claim("role", mapRole(credential.getRole()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        return new JwtToken(token, expiresAt);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        String value = extractAllClaims(token).get("uid", String.class);
        return value == null ? null : UUID.fromString(value);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Instant extractExpiration(String token) {
        Date date = extractAllClaims(token).getExpiration();
        return date == null ? null : date.toInstant();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private String mapRole(Role role) {
        return role == Role.ADMIN ? "ROLE_ADMIN" : "ROLE_USER";
    }
}

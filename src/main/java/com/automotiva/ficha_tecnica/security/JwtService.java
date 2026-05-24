package com.automotiva.ficha_tecnica.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecurityProperties properties;

    public JwtService(SecurityProperties properties) {
        this.properties = properties;
    }

    public String generateAccessToken(UserDetails userDetails) {
        Instant now = Instant.now();
        String roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(Map.of("roles", roles, "token_type", "access"))
                .issuer(properties.getJwtIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.getJwtExpirationSeconds())))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(username)
                .claims(Map.of("token_type", "refresh"))
                .issuer(properties.getJwtIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.getJwtRefreshExpirationSeconds())))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return parseAllClaims(token).getSubject();
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        Claims claims = parseAllClaims(token);
        return "access".equals(claims.get("token_type"))
                && userDetails.getUsername().equals(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    public boolean isRefreshTokenValid(String token) {
        Claims claims = parseAllClaims(token);
        return "refresh".equals(claims.get("token_type"))
                && claims.getExpiration().after(new Date());
    }

    private Claims parseAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        String secret = properties.getJwtSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("app.security.jwt-secret deve ter ao menos 32 caracteres");
        }

        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

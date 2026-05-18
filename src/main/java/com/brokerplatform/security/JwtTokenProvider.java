package com.brokerplatform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildToken(principal.getUsername(), principal.getId().toString(),
                principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                expirationMs);
    }

    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildToken(principal.getUsername(), principal.getId().toString(), List.of(), refreshExpirationMs);
    }

    private String buildToken(String subject, String brokerId, List<String> roles, long ttl) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttl);

        return Jwts.builder()
                .subject(subject)
                .claim("brokerId", brokerId)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String generateAccessTokenFromEmail(String email, Long brokerId, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("brokerId", brokerId.toString())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getBrokerIdFromToken(String token) {
        String id = parseClaims(token).get("brokerId", String.class);
        return Long.parseLong(id);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT não suportado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformado: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Assinatura JWT inválida: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT vazio: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

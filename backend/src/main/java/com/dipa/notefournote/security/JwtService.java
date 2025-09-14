package com.dipa.notefournote.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    @Value("${JWT_ACCESS_EXPIRATION:3600000}") // Default di 1 ora
    private Long jwtAccessExpiration;

    @Value("${JWT_REFRESH_EXPIRATION:604800000}") // Default di 7 giorni
    private Long jwtRefreshExpiration;

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String ACCESS_TOKEN_TYPE = "access";

    public String generateToken(Authentication authentication, boolean isRefreshToken) {
        final long expiration = isRefreshToken ? jwtRefreshExpiration : jwtAccessExpiration;
        final Map<String, String> extraClaim = isRefreshToken
                ? Map.of(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                : Map.of(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);

        final String username = authentication.getName();
        final Date currentDate = new Date();
        final Date expireDate = new Date(currentDate.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .claims(extraClaim)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isAccessTokenValid(String token) {
        try {
            final Claims claims = getClaims(token);
            if (isTokenExpired(claims)) {
                log.warn("Access token is expired");
                return false;
            }
            return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM));
        } catch (Exception e) {
            log.error("Invalid access token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            final Claims claims = getClaims(token);
            if (isTokenExpired(claims)) {
                log.warn("Refresh token is expired");
                return false;
            }
            return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM));
        } catch (Exception e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
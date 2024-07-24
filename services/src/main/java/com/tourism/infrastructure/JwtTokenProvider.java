package com.tourism.infrastructure;

import com.tourism.model.Role;
import com.tourism.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${session.access-token-expiration-time}")
    private Long accessTokenExpirationTime;

    @Value("${session.refresh-token-expiration-time}")
    private Long refreshTokenExpirationTime;

    @Value("${session.client-secret}")
    private String secretKey;

    private static final String EMAIL = "email";
    private static final String ROLE = "role";
    private static final String ID = "id";

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expirationInstant = now.plus(accessTokenExpirationTime, ChronoUnit.HOURS);

        return Jwts.builder()
                .claim(ID, user.getId().toString())
                .claim(ROLE, user.getRole())
                .claim(EMAIL, user.getEmail())
                .expiration(Date.from(expirationInstant))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expirationInstant = now.plus(refreshTokenExpirationTime, ChronoUnit.DAYS);

        return Jwts.builder()
                .claim(ID, user.getId().toString())
                .claim(ROLE, user.getRole())
                .claim(EMAIL, user.getEmail())
                .expiration(Date.from(expirationInstant))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes())).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes())).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    public User getUserFromToken(HttpServletRequest request) {
        String token;
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseSignedClaims(token);

            Claims claims = claimsJws.getPayload();
            return new User(
                    UUID.fromString(claims.get(ID).toString()),
                    claims.get(EMAIL).toString(),
                    Role.valueOf(claims.get(ROLE).toString())
            );
        } else {
            return null;
        }
    }

}

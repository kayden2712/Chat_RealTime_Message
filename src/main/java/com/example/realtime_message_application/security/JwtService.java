package com.example.realtime_message_application.security;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expirationInSec}") // 432000 - 5 days in seconds
    private Long expirationInSec; // 5 days

    public Key getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String extracUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role")).toString();
    }

    public Long extractUserId(String token) {
        return (Long) extractClaim(token, claims -> claims.get("userId"));
    }

    // dung de lay 1 thong tin trong claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) { // một hàm ẩn danh (lambda). Nó nhận
                                                                                  // đầu vào là đối tượng Claims và trả
                                                                                  // về một giá trị kiểu T.
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();

        if (user instanceof CustomUserDetails customUser) {
            claims.put("userId", customUser.getUserId());
        }

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        if (!authorities.isEmpty()) {
            // It extracts the user’s role from Spring Security and
            // stores it inside the JWT token without the "ROLE_" prefix.
            List<String> roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", ""))
                    .toList();

            claims.put("roles", roles);
        }
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationInSec * 1000))
                .signWith(getSignKey()).compact();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        final String username = extracUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, claims -> claims.getExpiration().before(new Date()));
    }
}

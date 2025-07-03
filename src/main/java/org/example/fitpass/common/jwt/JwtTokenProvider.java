package org.example.fitpass.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.security.CustomUserDetailsService;
import org.example.fitpass.config.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    private final long accessTokenValidityInMs = 1000 * 60 * 60; // 1시간
    private final long refreshTokenValidityInMs = 1000 * 60 * 60 * 24 * 7; // 7일

    private static final String BEARER_PREFIX = "Bearer ";

    private final CustomUserDetailsService userDetailsService;
    private final RedisService redisService;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String email, String role) {
        return generateToken(email, role, accessTokenValidityInMs);
    }

    public String createRefreshToken(String email, String role) {
        String refreshToken = generateToken(email, role, refreshTokenValidityInMs);
        redisService.setRefreshToken(email, refreshToken, refreshTokenValidityInMs);
        return refreshToken;
    }

    private String generateToken(String email, String role, long validityInMs) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.put("role", "ROLE_" + role);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return BEARER_PREFIX + Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new BaseException(ExceptionCode.JWT_TOKEN_REQUIRED);
    }

    public String getUserEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUserEmail(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public long getRemainingTime(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    public void blacklistAccessToken(String accessToken, long expiration) {
        redisService.setBlackList(accessToken, "logout", expiration);
    }
}

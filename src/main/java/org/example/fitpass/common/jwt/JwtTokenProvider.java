package org.example.fitpass.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.common.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Base64;
import java.util.Date;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    private final long tokenValidityInMilliseconds = 1000 * 60 * 60;

    private static final String BEARER_PREFIX = "Bearer ";// 1시간

    private final CustomUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 토큰 생성
    public String createToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return BEARER_PREFIX + Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new RuntimeException("토큰을 찾지 못했습니다.");
    }

    public Long getUserIdFromToken(String token) {
        String email = getUserEmail(token); // 이미 있는 메서드 사용
        User user = userDetailsService.loadUserEntityByEmail(email);
        return user.getId();
    }


    // 인증 정보 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUserEmail(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 이메일 추출
    public String getUserEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Request Header에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

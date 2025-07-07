package org.example.fitpass.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.security.CustomUserDetailsService;
import org.example.fitpass.common.config.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
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
        log.info("[JWT INIT] JWT 토큰 프로바이더 초기화 완료 - ACCESS_TOKEN_VALIDITY: {}ms, REFRESH_TOKEN_VALIDITY: {}ms", 
                accessTokenValidityInMs, refreshTokenValidityInMs);
    }

    // Access Token 생성
    public String createAccessToken(String email, String role) {
        log.debug("[JWT CREATE] Access Token 생성 시작 - EMAIL: {}, ROLE: {}", email, role);
        
        try {
            String token = generateToken(email, role, accessTokenValidityInMs);
            String tokenHash = generateTokenHash(token.substring(7)); // Bearer 제거 후 해시
            
            log.info("[JWT CREATE SUCCESS] Access Token 생성 완료 - EMAIL: {}, ROLE: {}, TOKEN_HASH: {}, EXPIRES_IN: {}ms", 
                    email, role, tokenHash, accessTokenValidityInMs);
            
            return token;
        } catch (Exception e) {
            log.error("[JWT CREATE FAILED] Access Token 생성 실패 - EMAIL: {}, ROLE: {}, ERROR: {}", 
                     email, role, e.getMessage(), e);
            throw new BaseException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
    }

    // Refresh Token 생성
    public String createRefreshToken(String email, String role) {
        log.debug("[JWT CREATE] Refresh Token 생성 시작 - EMAIL: {}, ROLE: {}", email, role);
        
        try {
            String refreshToken = generateToken(email, role, refreshTokenValidityInMs);
            String tokenHash = generateTokenHash(refreshToken.substring(7)); // Bearer 제거 후 해시
            
            // Redis에 저장
            redisService.setRefreshToken(email, refreshToken, refreshTokenValidityInMs);
            
            log.info("[JWT CREATE SUCCESS] Refresh Token 생성 및 Redis 저장 완료 - EMAIL: {}, ROLE: {}, TOKEN_HASH: {}, EXPIRES_IN: {}ms", 
                    email, role, tokenHash, refreshTokenValidityInMs);
            
            return refreshToken;
        } catch (Exception e) {
            log.error("[JWT CREATE FAILED] Refresh Token 생성 실패 - EMAIL: {}, ROLE: {}, ERROR: {}", 
                     email, role, e.getMessage(), e);
            throw new BaseException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 토큰 생성 (내부 메서드)
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

    // Bearer 토큰에서 실제 토큰 추출
    public String substringToken(String tokenValue) {
        log.debug("[JWT EXTRACT] Bearer 토큰에서 실제 토큰 추출 시도");
        
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            String token = tokenValue.substring(7);
            log.debug("[JWT EXTRACT SUCCESS] 토큰 추출 완료 - TOKEN_HASH: {}", generateTokenHash(token));
            return token;
        }
        
        log.warn("[JWT EXTRACT FAILED] Bearer 토큰 형식이 아님 - TOKEN_PREFIX: {}", 
                tokenValue != null ? tokenValue.substring(0, Math.min(10, tokenValue.length())) : "null");
        throw new BaseException(ExceptionCode.JWT_TOKEN_REQUIRED);
    }

    // 토큰에서 사용자 이메일 추출
    public String getUserEmail(String token) {
        log.debug("[JWT PARSE] 토큰에서 사용자 이메일 추출 시도 - TOKEN_HASH: {}", generateTokenHash(token));
        
        try {
            String email = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody().getSubject();
            
            log.debug("[JWT PARSE SUCCESS] 사용자 이메일 추출 완료 - EMAIL: {}, TOKEN_HASH: {}", email, generateTokenHash(token));
            return email;
        } catch (Exception e) {
            log.warn("[JWT PARSE FAILED] 토큰에서 이메일 추출 실패 - TOKEN_HASH: {}, ERROR: {}", 
                    generateTokenHash(token), e.getMessage());
            throw e;
        }
    }

    // 인증 객체 생성
    public Authentication getAuthentication(String token) {
        log.debug("[JWT AUTH] 인증 객체 생성 시작 - TOKEN_HASH: {}", generateTokenHash(token));
        
        try {
            String email = getUserEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            log.debug("[JWT AUTH SUCCESS] 인증 객체 생성 완료 - EMAIL: {}, AUTHORITIES: {}", 
                     email, userDetails.getAuthorities());
            
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        } catch (Exception e) {
            log.warn("[JWT AUTH FAILED] 인증 객체 생성 실패 - TOKEN_HASH: {}, ERROR: {}", 
                    generateTokenHash(token), e.getMessage());
            throw e;
        }
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        String tokenHash = generateTokenHash(token);
        log.debug("[JWT VALIDATE] 토큰 유효성 검증 시작 - TOKEN_HASH: {}", tokenHash);
        
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            log.debug("[JWT VALIDATE SUCCESS] 토큰 유효성 검증 성공 - TOKEN_HASH: {}", tokenHash);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT VALIDATE FAILED] 토큰 만료 - TOKEN_HASH: {}, EXPIRED_AT: {}", 
                    tokenHash, e.getClaims().getExpiration());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("[JWT VALIDATE FAILED] 잘못된 토큰 형식 - TOKEN_HASH: {}, ERROR: {}", 
                    tokenHash, e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("[JWT VALIDATE FAILED] 토큰 서명 검증 실패 - TOKEN_HASH: {}, ERROR: {}", 
                    tokenHash, e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("[JWT VALIDATE FAILED] 지원하지 않는 토큰 - TOKEN_HASH: {}, ERROR: {}", 
                    tokenHash, e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("[JWT VALIDATE FAILED] 잘못된 토큰 인자 - TOKEN_HASH: {}, ERROR: {}", 
                    tokenHash, e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("[JWT VALIDATE FAILED] 기타 JWT 오류 - TOKEN_HASH: {}, ERROR: {}", 
                    tokenHash, e.getMessage());
            return false;
        }
    }

    // HTTP 요청에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("[JWT RESOLVE] HTTP 요청에서 토큰 추출 - HAS_TOKEN: {}", bearerToken != null);
        return bearerToken;
    }

    // 토큰 남은 시간 계산
    public long getRemainingTime(String token) {
        String tokenHash = generateTokenHash(token);
        log.debug("[JWT TIME] 토큰 남은 시간 계산 - TOKEN_HASH: {}", tokenHash);
        
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();
            
            log.debug("[JWT TIME SUCCESS] 토큰 남은 시간 계산 완료 - TOKEN_HASH: {}, REMAINING: {}ms", 
                     tokenHash, remainingTime);
            
            return remainingTime;
        } catch (Exception e) {
            log.warn("[JWT TIME FAILED] 토큰 남은 시간 계산 실패 - TOKEN_HASH: {}, ERROR: {}", 
                    tokenHash, e.getMessage());
            return 0;
        }
    }

    // 토큰을 블랙리스트에 추가 (로그아웃)
    public void blacklistAccessToken(String accessToken, long expiration) {
        String tokenHash = generateTokenHash(accessToken);
        log.info("[JWT BLACKLIST] 토큰 블랙리스트 추가 - TOKEN_HASH: {}, EXPIRATION: {}ms", tokenHash, expiration);
        
        try {
            redisService.setBlackList(accessToken, "logout", expiration);
            log.info("[JWT BLACKLIST SUCCESS] 토큰 블랙리스트 추가 완료 - TOKEN_HASH: {}", tokenHash);
        } catch (Exception e) {
            log.error("[JWT BLACKLIST FAILED] 토큰 블랙리스트 추가 실패 - TOKEN_HASH: {}, ERROR: {}", 
                     tokenHash, e.getMessage(), e);
        }
    }

    // 토큰 해시 생성 (보안상 토큰 전체를 로그에 남기지 않음)
    private String generateTokenHash(String token) {
        if (token == null || token.length() < 8) {
            return "***INVALID***";
        }
        return "***" + token.substring(Math.max(0, token.length() - 8));
    }
}

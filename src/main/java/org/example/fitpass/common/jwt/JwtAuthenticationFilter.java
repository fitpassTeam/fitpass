package org.example.fitpass.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.security.CustomUserDetailsService;
import org.example.fitpass.common.config.RedisService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String clientInfo = getClientInfo(request);

        // 인증이 필요없는 경로는 필터 통과
        if (requestURI.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = jwtTokenProvider.resolveToken(request);

        // 토큰이 없는 경우
        if (bearerToken == null || bearerToken.trim().isEmpty()) {
            log.debug("[JWT AUTH] 토큰 없음 - PATH: {}, CLIENT: {}", requestURI, clientInfo);
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 토큰 형식 검증
        if (!bearerToken.startsWith("Bearer ")) {
            log.warn("[JWT SECURITY] 잘못된 토큰 형식 - PATH: {}, CLIENT: {}, TOKEN_PREFIX: {}", 
                    requestURI, clientInfo, bearerToken.substring(0, Math.min(10, bearerToken.length())));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String token = jwtTokenProvider.substringToken(bearerToken);
            String tokenHash = generateTokenHash(token);

            // 블랙리스트 체크
            if (redisService.isBlacklisted(token)) {
                log.warn("[JWT SECURITY] 블랙리스트 토큰 사용 시도 - TOKEN_HASH: {}, PATH: {}, CLIENT: {}", 
                        tokenHash, requestURI, clientInfo);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 토큰 유효성 검사
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("[JWT SECURITY] 유효하지 않은 토큰 - TOKEN_HASH: {}, PATH: {}, CLIENT: {}", 
                        tokenHash, requestURI, clientInfo);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 유저 정보 확인 및 인증 설정
            String email = jwtTokenProvider.getUserEmail(token);
            
            try {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("[JWT AUTH SUCCESS] 인증 성공 - EMAIL: {}, PATH: {}, TOKEN_HASH: {}", 
                         email, requestURI, tokenHash);

            } catch (UsernameNotFoundException e) {
                log.warn("[JWT SECURITY] 존재하지 않는 사용자 토큰 - EMAIL: {}, TOKEN_HASH: {}, PATH: {}, CLIENT: {}", 
                        email, tokenHash, requestURI, clientInfo);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        } catch (Exception e) {
            log.error("[JWT ERROR] 토큰 처리 중 예외 발생 - PATH: {}, CLIENT: {}, ERROR: {}", 
                     requestURI, clientInfo, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // 클라이언트 정보 추출 (IP, User-Agent)
    private String getClientInfo(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("Unknown");
        String shortUserAgent = userAgent.length() > 50 ? userAgent.substring(0, 50) + "..." : userAgent;
        return String.format("IP:%s, UserAgent:%s", clientIp, shortUserAgent);
    }

    // 실제 클라이언트 IP 주소 가져오기 (프록시 고려)
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    // 토큰 해시 생성 (보안상 토큰 전체를 로그에 남기지 않음)
    private String generateTokenHash(String token) {
        return "***" + token.substring(Math.max(0, token.length() - 8));
    }
}

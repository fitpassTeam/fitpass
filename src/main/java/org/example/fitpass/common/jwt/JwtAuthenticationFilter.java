package org.example.fitpass.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.security.CustomUserDetailsService;
import org.example.fitpass.config.RedisService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        // 인증이 필요없는 경로들은 필터를 건너뜀
        if (shouldSkipAuthentication(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = jwtTokenProvider.resolveToken(request);

        if (bearerToken != null && !bearerToken.trim().isEmpty() && bearerToken.startsWith("Bearer ")) {
            String token = jwtTokenProvider.substringToken(bearerToken);

            // 블랙리스트 체크
            if (redisService.isBlacklisted(token)) {
                logger.warn("블랙리스트 토큰입니다");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 토큰 유효성 검사
            if (!jwtTokenProvider.validateToken(token)) {
                logger.warn("유효하지 않은 토큰입니다");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 유저 정보 확인
            String email = jwtTokenProvider.getUserEmail(token);
            try {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UsernameNotFoundException e) {
                logger.warn("해당 이메일의 유저를 찾을 수 없습니다");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipAuthentication(String requestURI) {
        // 인증이 필요없는 경로들
        String[] publicPaths = {
            "/auth",
            "/search",
            "/gyms",
            "/actuator/health",
            "/health",
            "/login",
            "/ws",
            "/error",
            "/swagger-ui",
            "/v3/api-docs",
            "/api-docs",
            "/memberships/purchases",
            "/api/payments/confirm",
            "/api/payments/fail",
            "/payment"
        };

        for (String path : publicPaths) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        
        // GET 요청인 경우 특정 경로들 허용
        if (requestURI.matches("/gyms/\\d+/trainers/.*") || 
            requestURI.matches("/gyms/\\d+/memberships/.*")) {
            return true;
        }
        
        return false;
    }
}

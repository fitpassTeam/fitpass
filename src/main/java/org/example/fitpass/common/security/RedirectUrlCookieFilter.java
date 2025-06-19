package org.example.fitpass.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class RedirectUrlCookieFilter extends OncePerRequestFilter {

    public static final String REDIRECT_URI_PARAM = "redirect_url";

    private static final int MAX_AGE = 180;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        log.info("[RedirectUrlCookieFilter] 요청 URI: {}", request.getRequestURI());

        if (request.getRequestURI().startsWith("/oauth2/authorization")) {
            try {
                log.info("요청 URI {} ", request.getRequestURI());
                String redirectUrl = request.getParameter(REDIRECT_URI_PARAM);
                log.info("[RedirectUrlCookieFilter] redirect_url 파라미터: {}", redirectUrl);

                Cookie cookie = new Cookie(REDIRECT_URI_PARAM, redirectUrl);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(MAX_AGE);
                StringBuilder cookieHeader = new StringBuilder();
                cookieHeader.append(REDIRECT_URI_PARAM).append("=").append(redirectUrl).append("; Path=/; HttpOnly; Max-Age=").append(MAX_AGE)
                    .append("; SameSite=None; Secure");

                response.addHeader("Set-Cookie", cookieHeader.toString());

            } catch (Exception ex) {
                log.error("보안 컨텍스트에 사용자 인증 정보를 설정할 수 없습니다.", ex);
                log.info("인증되지 않은 요청입니다.");
            }

        }
        filterChain.doFilter(request, response);
    }

}


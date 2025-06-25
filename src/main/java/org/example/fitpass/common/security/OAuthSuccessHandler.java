package org.example.fitpass.common.security;

import static org.example.fitpass.common.security.RedirectUrlCookieFilter.REDIRECT_URI_PARAM;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.domain.user.entity.CustomUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String LOCAL_REDIRECT_URL = "http://localhost:5173";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {

        String email;
        String role;

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            email = userDetails.getUsername();
            role = userDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new BaseException(ExceptionCode.NOT_HAS_AUTHORITY))
                .getAuthority();
        } else if (principal instanceof CustomUser) {
            CustomUser customUser = (CustomUser) principal;
            email = customUser.getEmail();

            role = customUser.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new BaseException(ExceptionCode.NOT_HAS_AUTHORITY))
                .getAuthority();
        } else {
            throw new IllegalArgumentException("알 수 없는 사용자 타입: " + principal.getClass().getName());
        }

        String accessToken = jwtTokenProvider.createAccessToken(email, role);

        log.info("accessToken: {}", accessToken);

        Optional<Cookie> oCookie = Optional.ofNullable(request.getCookies())
            .flatMap(cookies -> Arrays.stream(cookies)
                .filter(cookie -> REDIRECT_URI_PARAM.equals(cookie.getName()))
                .findFirst());

        String baseRedirectUrl = oCookie.map(Cookie::getValue)
            .orElse(LOCAL_REDIRECT_URL);

        // sociallogin 경로가 이미 포함되어 있으면 중복 붙이지 않음
        String targetUrl;
        if (baseRedirectUrl.endsWith("/sociallogin")) {
            targetUrl = baseRedirectUrl + "?token=" + accessToken;
        } else if (baseRedirectUrl.contains("/sociallogin?")) {
            // 이미 token 파라미터 포함 가능성도 고려
            targetUrl = baseRedirectUrl + "&token=" + accessToken;
        } else {
            // sociallogin 경로 없으면 붙임
            targetUrl = baseRedirectUrl + "/sociallogin?token=" + accessToken;
        }

        log.info("targetUrl: {}", targetUrl);

        response.sendRedirect(targetUrl);
    }
}
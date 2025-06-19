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

    private static final String LOCAL_REDIRECT_URL = "http://localhost:3000";

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

            // CustomUser는 GrantedAuthority를 super클래스인 DefaultOAuth2User에서 가지고 있으므로 가져오기
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

        String targetUrl = oCookie.map(Cookie::getValue)
            .orElse("http://localhost:3000") + "/sociallogin?token=" + accessToken;

        log.info("targetUrl: {}", targetUrl);

        response.sendRedirect(targetUrl);
    }
}
package org.example.fitpass.common.oAuth2;

import static org.example.fitpass.common.security.RedirectUrlCookieFilter.REDIRECT_URI_PARAM;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    // 개발환경과 운영환경 프론트엔드 URL 설정
    private static final String DEV_FRONTEND_URL = "http://localhost:3000";
    private static final String LOCAL_REDIRECT_URL = "http://localhost:5173";
    private static final String PROD_FRONTEND_URL = "https://your-production-domain.com"; // 운영환경 URL로 변경

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        try {
            // 사용자 정보 추출 (OAuth2User, UserDetails, CustomUser 모두 지원)
            UserInfo userInfo = extractUserInfo(authentication);

            if (userInfo.user == null && (userInfo.email == null || userInfo.role == null)) {
                log.error("OAuth2 로그인 성공했지만 사용자 정보를 찾을 수 없습니다.");
                redirectToErrorPage(response, "user_not_found");
                return;
            }

            // JWT 토큰 생성
            String accessToken;
            String refreshToken;

            if (userInfo.user != null) {
                // CustomOAuth2User에서 User 엔티티를 가져온 경우
                accessToken = jwtTokenProvider.createAccessToken(userInfo.user.getEmail(), userInfo.user.getUserRole().name());
                refreshToken = jwtTokenProvider.createRefreshToken(userInfo.user.getEmail(), userInfo.user.getUserRole().name());

                log.info("OAuth2 로그인 성공: email = {}, UserRole = {}, socialType = {}",
                    userInfo.user.getEmail(), userInfo.user.getUserRole().name(), getSocialType(authentication));

                // 추가 정보 입력이 필요한지 확인
                boolean needsAdditionalInfo = isAdditionalInfoNeeded(userInfo.user);

                if (needsAdditionalInfo) {
                    // 추가 정보 입력 페이지로 리다이렉트
                    String targetUrl = buildAdditionalInfoUrl(accessToken, refreshToken, request);
                    log.info("추가 정보 입력 필요 - 리다이렉트 URL: {}", targetUrl);
                    getRedirectStrategy().sendRedirect(request, response, targetUrl);
                    return;
                }
            } else {
                // UserDetails나 CustomUser에서 정보를 가져온 경우
                accessToken = jwtTokenProvider.createAccessToken(userInfo.email, userInfo.role);
                refreshToken = jwtTokenProvider.createRefreshToken(userInfo.email, userInfo.role);

                log.info("OAuth2 로그인 성공: email = {}, role = {}", userInfo.email, userInfo.role);
            }

            // sociallogin 페이지로 리다이렉트 (OAuthSuccessHandler 방식)
            String targetUrl = buildSocialLoginUrl(accessToken, refreshToken, request);

            log.info("OAuth2 성공 후 리다이렉트 URL: {}", targetUrl);

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 성공 핸들링 중 오류 발생: {}", e.getMessage(), e);
            redirectToErrorPage(response, "authentication_error");
        }
    }

    // 사용자 정보 추출 (OAuth2User, UserDetails, CustomUser 모두 지원)
    private UserInfo extractUserInfo(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // 1. CustomOAuth2User에서 User 엔티티 추출 (기존 방식)
        if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;
            User user = customOAuth2User.getUser();
            return new UserInfo(user, null, null);
        }

        // 2. UserDetails에서 정보 추출
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) principal;
            String email = customUserDetails.getUsername();
            String role = customUserDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new BaseException(ExceptionCode.NOT_HAS_AUTHORITY))
                .getAuthority();
            return new UserInfo(null, email, role);
        }

        // 3. CustomUser에서 정보 추출
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String email = userDetails.getUsername();
            String role = userDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new BaseException(ExceptionCode.NOT_HAS_AUTHORITY))
                .getAuthority();
            return new UserInfo(null, email, role);
        }

        log.warn("알 수 없는 사용자 타입: {}", principal.getClass().getName());
        return new UserInfo(null, null, null);
    }

    // 사용자 정보를 담는 내부 클래스
    private static class UserInfo {
        final User user;
        final String email;
        final String role;

        UserInfo(User user, String email, String role) {
            this.user = user;
            this.email = email;
            this.role = role;
        }
    }

    // 소셜 로그인 타입 추출
    private String getSocialType(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User) {
            User user = ((CustomOAuth2User) principal).getUser();
            return user.getAuthProvider();
        }
        return "UNKNOWN";
    }

    // 추가 정보 입력 필요 여부 확인
    private boolean isAdditionalInfoNeeded(User user) {
        return "NEED_INPUT".equals(user.getPhone()) ||
            user.getAge() == -1 ||
            "NEED_INPUT".equals(user.getAddress()) ||
            user.getName() == null ||
            user.getName().equals("NEED_INPUT") ||
            user.getName().trim().isEmpty();
    }

    // 추가 정보 입력 페이지 URL 생성
    private String buildAdditionalInfoUrl(String accessToken, String refreshToken, HttpServletRequest request) {
        String baseUrl = getRedirectBaseUrl(request);

        return UriComponentsBuilder.fromUriString(baseUrl + "/auth/additional-info")
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .queryParam("needsInfo", "true")
            .build().toUriString();
    }

    // sociallogin 페이지 URL 생성 (OAuthSuccessHandler 방식)
    private String buildSocialLoginUrl(String accessToken, String refreshToken, HttpServletRequest request) {
        String baseRedirectUrl = getRedirectBaseUrl(request);

        // sociallogin 경로가 이미 포함되어 있으면 중복 붙이지 않음
        String targetUrl;
        if (baseRedirectUrl.endsWith("/sociallogin")) {
            targetUrl = baseRedirectUrl + "?token=" + accessToken + "&refreshToken=" + refreshToken;
        } else if (baseRedirectUrl.contains("/sociallogin?")) {
            // 이미 token 파라미터 포함 가능성도 고려
            targetUrl = baseRedirectUrl + "&token=" + accessToken + "&refreshToken=" + refreshToken;
        } else {
            // sociallogin 경로 없으면 붙임
            targetUrl = baseRedirectUrl + "/sociallogin?token=" + accessToken + "&refreshToken=" + refreshToken;
        }

        return targetUrl;
    }

    // 리다이렉트 베이스 URL 결정
    private String getRedirectBaseUrl(HttpServletRequest request) {
        // 쿠키에서 redirect_url 찾기
        Optional<String> cookieRedirectUrl = Optional.ofNullable(request.getCookies())
            .flatMap(cookies -> Arrays.stream(cookies)
                .filter(cookie -> REDIRECT_URI_PARAM.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue));

        if (cookieRedirectUrl.isPresent()) {
            String redirectUrl = cookieRedirectUrl.get();
            log.info("쿠키에서 리다이렉트 URL 발견: {}", redirectUrl);

            // URL에서 베이스 부분만 추출 (프로토콜://도메인:포트)
            try {
                java.net.URI uri = java.net.URI.create(redirectUrl);
                return uri.getScheme() + "://" + uri.getAuthority();
            } catch (Exception e) {
                log.warn("쿠키의 redirect_url 파싱 실패: {}, 기본 URL 사용", redirectUrl);
            }
        }

        // 기본 프론트엔드 URL 사용 (LOCAL_REDIRECT_URL 우선)
        return LOCAL_REDIRECT_URL;
    }

    // 오류 페이지로 리다이렉트
    private void redirectToErrorPage(HttpServletResponse response, String errorType) throws IOException {
        String errorUrl = UriComponentsBuilder.fromUriString(LOCAL_REDIRECT_URL + "/login")
            .queryParam("error", errorType)
            .build().toUriString();

        response.sendRedirect(errorUrl);
    }
}
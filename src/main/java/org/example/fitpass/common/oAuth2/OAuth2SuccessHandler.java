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
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    private static final String PROD_FRONTEND_URL = "https://your-production-domain.com"; // 운영환경 URL로 변경

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // CustomOAuth2User에서 User 엔티티 추출
            User user = extractUserFromOAuth2User(oAuth2User);

            if (user == null) {
                log.error("OAuth2 로그인 성공했지만 사용자 정보를 찾을 수 없습니다.");
                redirectToErrorPage(response, "user_not_found");
                return;
            }

            // JWT 토큰 생성
            String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getUserRole().name());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getUserRole().name());

            log.info("OAuth2 로그인 성공: email = {}, UserRole = {}, socialType = {}", 
                    user.getEmail(), user.getUserRole().name(), getSocialType(oAuth2User));

            // 추가 정보 입력이 필요한지 확인
            boolean needsAdditionalInfo = isAdditionalInfoNeeded(user);

            // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
            String targetUrl = buildTargetUrl(accessToken, refreshToken, needsAdditionalInfo, request);
            
            log.info("OAuth2 성공 후 리다이렉트 URL: {}", targetUrl);
            
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 성공 핸들링 중 오류 발생: {}", e.getMessage(), e);
            redirectToErrorPage(response, "authentication_error");
        }
    }

    // OAuth2User에서 User 엔티티 추출
    private User extractUserFromOAuth2User(OAuth2User oAuth2User) {
        if (oAuth2User instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) oAuth2User).getUser();
        }
        
        log.warn("OAuth2User가 CustomOAuth2User 타입이 아닙니다: {}", oAuth2User.getClass().getName());
        return null;
    }

    // 소셜 로그인 타입 추출
    private String getSocialType(OAuth2User oAuth2User) {
        if (oAuth2User instanceof CustomOAuth2User) {
            User user = ((CustomOAuth2User) oAuth2User).getUser();
            return user.getAuthProvider(); // User의 authProvider 사용
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

    // 프론트엔드 리다이렉트 URL 생성
    private String buildTargetUrl(String accessToken, String refreshToken, boolean needsAdditionalInfo, HttpServletRequest request) {
        String baseUrl = getRedirectBaseUrl(request);
        
        if (needsAdditionalInfo) {
            // 추가 정보 입력 페이지로 리다이렉트
            return UriComponentsBuilder.fromUriString(baseUrl + "/auth/additional-info")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("needsInfo", "true")
                .build().toUriString();
        } else {
            // 메인 페이지로 리다이렉트
            return UriComponentsBuilder.fromUriString(baseUrl + "/oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
        }
    }

    // 리다이렉트 베이스 URL 결정
    // 1. 쿠키에 저장된 redirect_url 우선 사용
    // 2. 없으면 기본 프론트엔드 URL 사용
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
        
        // 기본 프론트엔드 URL 사용
        return getFrontendBaseUrl();
    }

    // 프론트엔드 베이스 URL 반환
    private String getFrontendBaseUrl() {
        // 환경변수나 프로파일에 따라 URL 결정
        // 현재는 개발환경 URL 사용
        return DEV_FRONTEND_URL;
    }

    // 오류 페이지로 리다이렉트
    private void redirectToErrorPage(HttpServletResponse response, String errorType) throws IOException {
        String errorUrl = UriComponentsBuilder.fromUriString(getFrontendBaseUrl() + "/login")
            .queryParam("error", errorType)
            .build().toUriString();
        
        response.sendRedirect(errorUrl);
    }
}

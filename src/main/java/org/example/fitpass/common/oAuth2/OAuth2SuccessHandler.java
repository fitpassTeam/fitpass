package org.example.fitpass.common.oAuth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2User에서 User 엔티티 추출
        User user = null;
        if(oAuth2User instanceof CustomOAuth2User) {
            user = ((CustomOAuth2User) oAuth2User).getUser();
        }

        if(user == null) {
            log.error("OAuth2 로그인 성공했지만 사용자 정보를 찾을 수 없습니다.");
            response.sendRedirect("/login?error=user_not_found");
            return;
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getUserRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getUserRole().name());

        log.info("OAuth2 로그인 성공 : email = {}, UserRole = {}", user.getEmail(), user.getUserRole().name());

        // 추가 정보 입력이 필요한지 확인
        boolean needsAdditionalInfo = isAdditionalInfoNeeded(user);

        // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String targetUrl;
        if (needsAdditionalInfo) {
            // 추가 정보 입력 페이지로 리다이렉트
            targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/auth/additional-info")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("needsInfo", "true")
                .build().toUriString();
        } else {
            // 메인 페이지로 리다이렉트
            targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private boolean isAdditionalInfoNeeded(User user) {
        return "NEED_INPUT".equals(user.getPhone()) ||
            user.getAge() == -1 ||
            "NEED_INPUT".equals(user.getAddress());
    }


}

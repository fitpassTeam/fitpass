package org.example.fitpass.common.oAuth2;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService로 사용자 정보 가져오기
        OAuth2User oauth2User = super.loadUser(userRequest);

        // 어떤 제공자인지 확인 (google, naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 사용자 식별 키 (Google의 경우 'sub', Naver의 경우 'id')
        String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        log.info("OAuth2 로그인 시도: registrationId = {}, userNameAttributeName = {}", 
                registrationId, userNameAttributeName);

        try {
            // OAuth2 사용자 정보 추출
            OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName,
                oauth2User.getAttributes());

            log.info("OAuth2 사용자 정보 추출 완료: email = {}, name = {}, authProvider = {}",
                attributes.getEmail(), attributes.getName(), attributes.getAuthProvider());

            // 사용자 저장 또는 업데이트
            User user = saveOrUpdate(attributes);

            // CustomOAuth2User 객체로 반환 (Spring Security가 인식할 수 있도록)
            return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey(),
                user
            );
        } catch (Exception e) {
            log.error("OAuth2 사용자 정보 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("OAuth2 사용자 정보 처리 실패: " + e.getMessage());
        }
    }

    // 사용자 정보 저장 또는 업데이트
    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
            .map(existingUser -> {
                log.info("기존 사용자 발견: email = {}", existingUser.getEmail());
                // 기존 사용자의 경우 필요시 정보 업데이트
                updateUserIfNeeded(existingUser, attributes);
                return existingUser;
            })
            .orElseGet(() -> {
                log.info("새로운 OAuth2 사용자 생성: email = {}, authProvider = {}",
                    attributes.getEmail(), attributes.getAuthProvider());
                return attributes.toEntity();
            });

        return userRepository.save(user);
    }

    // 필요시 기존 사용자 정보 업데이트
    private void updateUserIfNeeded(User existingUser, OAuthAttributes attributes) {
        boolean needUpdate = false;

        // 프로필 이미지 업데이트 (새로운 이미지가 있고, 기존에 없거나 다른 경우)
        if (attributes.getPicture() != null &&
            !attributes.getPicture().equals(existingUser.getUserImage())) {
            existingUser.updateUserImage(attributes.getPicture());
            needUpdate = true;
            log.info("사용자 프로필 이미지 업데이트: {}", attributes.getPicture());
        }

        // 이름 업데이트 (기존 이름이 없거나 "NEED_INPUT"인 경우)
        if (attributes.getName() != null &&
            (existingUser.getName() == null ||
             existingUser.getName().equals("NEED_INPUT") ||
             existingUser.getName().trim().isEmpty())) {
            // User 엔티티의 updateOAuthInfo 메서드 사용
            existingUser.updateOAuthInfo(attributes.getName(), null);
            needUpdate = true;
            log.info("사용자 이름 업데이트: {}", attributes.getName());
        }

        if (needUpdate) {
            log.info("OAuth2 사용자 정보 업데이트 완료: email = {}", existingUser.getEmail());
        }
    }
}

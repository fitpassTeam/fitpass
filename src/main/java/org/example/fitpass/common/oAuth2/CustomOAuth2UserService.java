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
// 사용자 정보 처리
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

   @Override
   public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
       // 기본 OAuth2UserService로 사용자 정보 가져오기
       OAuth2User oauth2User = super.loadUser(userRequest);

       // 어떤 제공자인지 확인 (google, kakao 등)
       String registrationId = userRequest.getClientRegistration().getRegistrationId();

       // 사용자 식별 키 (Google의 경우 'sub')
       String userNameAttributeName = userRequest.getClientRegistration()
           .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

       log.info("OAuth2 로그인 시도: registrationId = {}", registrationId);

       // OAuth2 사용자 정보 추출
       OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName,
           oauth2User.getAttributes());

       // 사용자 저장 또는 업데이트
       User user = saveOrUpdate(attributes);

       // CustomOAuth2User 객체로 반환 (Spring Security가 인식할 수 있도록)
       return new CustomOAuth2User(
           Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name())),
           attributes.getAttributes(),
           attributes.getNameAttributeKey(),
           user
       );
   }

    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
            .map(existingUser -> {
                log.info("기존 사용자 발견: {}", existingUser.getEmail());
                // 기존 사용자면 이름만 업데이트
                return existingUser;
            })
            .orElseGet(() -> {
                log.info("새로운 OAuth2 사용자 생성: {}", attributes.getEmail());
                return attributes.toEntity();
            });

        return userRepository.save(user);
    }
}

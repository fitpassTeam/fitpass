package org.example.fitpass.domain.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.dto.OAuthAttributes;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.user.entity.CustomUser;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (!"naver".equals(registrationId)) {
            throw new OAuth2AuthenticationException("오직 네이버 로그인만 지원됩니다.");
        }

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
            .getUserInfoEndpoint().getUserNameAttributeName();

        log.info("registrationId = " + registrationId);
        log.info("userNameAttributeName = " + userNameAttributeName);

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        String name = attributes.getName() != null ? attributes.getName() : "";
        String email = attributes.getEmail() != null ? attributes.getEmail() : "";
        String picture = attributes.getPicture();
        String id = attributes.getId();
        String socialType = "naver";

        log.info("name = " + name);
        log.info("email = " + email);
        log.info("picture = " + picture);

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        User user;
        if (!userRepository.existsByEmail(email)) {
            user = new User(email, name, socialType);
            user = userRepository.save(user);
        } else {
            user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ExceptionCode.USER_NOT_FOUND));
        }

        return new CustomUser(user.getId(), email, name, authorities, attributes);
    }



}

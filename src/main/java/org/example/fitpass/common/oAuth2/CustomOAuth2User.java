package org.example.fitpass.common.oAuth2;

import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
@RequiredArgsConstructor
// OAuth2User 인터페이스를 구현해서 Spring Security와 User 엔티티를 연결해주는 역할
// OAuth2 인증 후에 User 정보를 담고 있다
public class CustomOAuth2User implements OAuth2User {

    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final User user;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        Object nameValue = attributes.get(nameAttributeKey);
        if (nameValue == null && attributes.get("response") instanceof Map responseMap) {
            nameValue = responseMap.get(nameAttributeKey);
        }
        return nameValue != null ? nameValue.toString() : "UNKNOWN";
    }

    public String getEmail() {
        return user.getEmail();
    }
}

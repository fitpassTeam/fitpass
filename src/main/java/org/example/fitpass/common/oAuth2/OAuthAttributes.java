package org.example.fitpass.common.oAuth2;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.entity.User;

@Getter
@RequiredArgsConstructor
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;

    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
        String name, String email, String picture) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }


    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
        Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }

        throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName,
        Map<String, Object> attributes) {
        return new OAuthAttributes(
            attributes,
            userNameAttributeName,
            (String) attributes.get("name"),
            (String) attributes.get("email"),
            (String) attributes.get("picture")
        );
    }

    public User toEntity() {
        return new User(
            email,                    // email
            picture,                  // userImage (Google 프로필 이미지)
            "OAUTH2_TEMP",           // password (임시, OAuth2 사용자 구분용)
            name,                    // name
            "NEED_INPUT",            // phone (입력 필요 표시)
            -1,                      // age (입력 필요 표시, -1로 미입력 상태)
            "NEED_INPUT",            // address (입력 필요 표시)
            Gender.MAN,              // gender (임시, 나중에 입력받을 예정)
            UserRole.USER            // userRole
        );
    }


}

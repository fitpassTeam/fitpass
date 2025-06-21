package org.example.fitpass.common.oAuth2;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.entity.User;

@Getter
@RequiredArgsConstructor
// Google, Naver OAuth2 응답을 우리 시스템 형태로 변환
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String socialId;
    private String authProvider;

    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
        String name, String email, String picture, String socialId, String authProvider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.socialId = socialId;
        this.authProvider = authProvider;
    }

     // OAuth2 제공자별 사용자 정보 추출
    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
        Map<String, Object> attributes) {
        
        switch (registrationId.toLowerCase()) {
            case "google":
                return ofGoogle(userNameAttributeName, attributes);
            case "naver":
                return ofNaver(userNameAttributeName, attributes);
            default:
                throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }
    }

     // Google OAuth2 사용자 정보 추출
    private static OAuthAttributes ofGoogle(String userNameAttributeName,
        Map<String, Object> attributes) {
        return new OAuthAttributes(
            attributes,
            userNameAttributeName,
            (String) attributes.get("name"),
            (String) attributes.get("email"),
            (String) attributes.get("picture"),
            (String) attributes.get("sub"), // Google의 사용자 고유 ID
            "GOOGLE"
        );
    }

     // Naver OAuth2 사용자 정보 추출
    private static OAuthAttributes ofNaver(String userNameAttributeName,
        Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return new OAuthAttributes(
            response,
            userNameAttributeName,
            (String) response.get("name"),
            (String) response.get("email"),
            (String) response.get("profile_image"),
            (String) response.get("id"), // Naver의 사용자 고유 ID
            "NAVER"
        );
    }

    // OAuth2 사용자 정보를 User 엔티티로 변환
    public User toEntity() {
        return new User(
            email,                    // email
            picture,                  // userImage (프로필 이미지)
            "OAUTH2_TEMP",           // password (임시, OAuth2 사용자 구분용)
            name,                    // name
            "NEED_INPUT",            // phone (입력 필요 표시)
            -1,                      // age (입력 필요 표시, -1로 미입력 상태)
            "NEED_INPUT",            // address (입력 필요 표시)
            Gender.MAN,              // gender (임시, 나중에 입력받을 예정)
            UserRole.USER,            // userRole
            authProvider
        );
    }
}

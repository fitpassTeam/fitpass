package org.example.fitpass.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.likes.entity.Like;

import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userImage;

    @Column(nullable = false ,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private int pointBalance = 0;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column
    private String authProvider;

    @OneToMany
    private List<Like> likes = new ArrayList<>();

    // OAuth2 소셜 로그인용 생성자 (간단한 정보만)
    public User(String email, String name, String authProvider) {
        this.email = email;
        this.name = name;
        this.authProvider = authProvider;


        // OAuth2 기본값 세팅 (새로운 OAuth2 시스템과 일치)
        this.password = "OAUTH2_TEMP";
        this.phone = "NEED_INPUT";
        this.age = -1;
        this.address = "NEED_INPUT";
        this.pointBalance = 0;
        this.gender = Gender.MAN;  // 임시값, 추후 입력
        this.userRole = UserRole.USER;
    }

    // 일반 회원가입용 생성자 (전체 정보)
    public User(String email, String userImage, String password, String name, String phone, int age, String address, Gender gender, UserRole userRole) {
        this.email = email;
        this.userImage = userImage;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.age = age;
        this.address = address;
        this.gender = gender;
        this.userRole = userRole;
        this.authProvider = "LOCAL"; // 일반 회원가입은 LOCAL로 설정
        this.pointBalance = 0;
    }

    // OAuth2용 상세 생성자 (authProvider 포함)
    public User(String email, String userImage, String password, String name, String phone, int age, String address, Gender gender, UserRole userRole, String authProvider) {
        this.email = email;
        this.userImage = userImage;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.age = age;
        this.address = address;
        this.gender = gender;
        this.userRole = userRole;
        this.authProvider = authProvider;
        this.pointBalance = 0;
    }

    public void updateInfo(String name, int age, String address, String phone, String userImage) {
        this.name = name;
        this.age = age;
        this.address = address;
        this.phone = phone;
        this.userImage = userImage;
    }

    public void updatePhone(String newPhone) {
        this.phone = newPhone;
    }

    public void updatePassword(String encodedNewPassword) {
        this.password = encodedNewPassword;
    }

    // 포인트 잔액 업데이트
    public void updatePointBalance(int newBalance) {
        this.pointBalance = newBalance;
    }

    // OAuth2 프로필 이미지 업데이트
    public void updateUserImage(String userImage) {
        this.userImage = userImage;
    }

    // OAuth2 사용자 정보 업데이트 (이름, 프로필 이미지)
    public void updateOAuthInfo(String name, String userImage) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (userImage != null && !userImage.trim().isEmpty()) {
            this.userImage = userImage;
        }
    }

    public void requestOwnerUpgrade() {
        this.userRole = UserRole.PENDING_OWNER;
    }

    public void approveOwnerUpgrade() {
        this.userRole = UserRole.OWNER;
    }

    public void rejectOwnerUpgrade() {
        this.userRole = UserRole.USER;
    }

}

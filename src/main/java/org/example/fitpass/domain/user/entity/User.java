package org.example.fitpass.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.dto.UserRequestDto;

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

    public User(String email, String name, String authProvider) {
        this.email = email;
        this.name = name;
        this.authProvider = authProvider;

        // 기본값 세팅
        this.password = "SOCIAL_LOGIN";
        this.phone = "000-0000-0000";
        this.age = 0;
        this.address = "주소 미입력";
        this.pointBalance = 0;
        this.gender = Gender.NONE;
        this.userRole = UserRole.USER;
    }
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
        this.pointBalance = 0;
    }

    public void updateInfo(UserRequestDto dto) {
        this.name = dto.getName();
        this.age = dto.getAge();
        this.address = dto.getAddress();
        this.gender = dto.getGender();
        this.userRole = dto.getUserRole();
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
    public User(long userId, String userImage, Gender gender, UserRole userRole) {
        this.id = userId;
        this.userImage = userImage;
        this.gender = gender;
        this.userRole = userRole;
    }

    public static User of(long userId, String userImage, Gender gender, UserRole userRole) {
        return new User(userId, userImage, gender, userRole);
    }
}

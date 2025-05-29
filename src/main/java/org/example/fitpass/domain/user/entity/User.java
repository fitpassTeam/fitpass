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

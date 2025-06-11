package org.example.fitpass.domain.gym.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.enums.GymStatus;
import org.example.fitpass.common.entity.Image;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.entity.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE gyms SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Table(name = "gyms")
public class Gym extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false ,unique = true)
    private String number;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, columnDefinition = "TIME")
    private LocalTime openTime;

    @Column(nullable = false, columnDefinition = "TIME")
    private LocalTime closeTime;

    @Enumerated(EnumType.STRING)
    private GymStatus gymStatus = GymStatus.CLOSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private List <Trainer> trainers = new ArrayList<>();

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    public Gym(List<Image> gymImage, String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, User user) {
        this.name = name;
        this.number = number;
        this.content = content;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.user = user;

        for (Image image : gymImage) {
            image.assignToGym(this);
            this.images.add(image);
        }
    }

    public static Gym of(List<String> gymImage, String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, User user) {
        List<Image> images = gymImage.stream()
            .map(Image::new)
            .toList();
        return new Gym(images, name, number, content, address, openTime, closeTime, user);
    }

    public void isOwner(Long userId) {
        if (!this.user.getId().equals(userId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
    }

    public void updatePhoto(List<String> imageUrls, Gym gym) {
        this.images.clear();
        List<Image> convertedImages = imageUrls.stream()
                                       .map( url -> Image.from(url,gym))
                                       .toList();
        this.images.addAll(convertedImages);
    }

    public void update(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime) {
        if (name != null) {
            this.name = name;
        }
        if (number != null) {
            this.number = number;
        }
        if (content != null) {
            this.content = content;
        }
        if (address != null) {
            this.address = address;
        }
        if (openTime != null) {
            this.openTime = openTime;
        }
        if (closeTime != null) {
            this.closeTime = closeTime;
        }
    }

}

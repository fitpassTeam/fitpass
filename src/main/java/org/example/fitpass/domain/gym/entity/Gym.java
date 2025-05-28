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
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.gym.enums.GymStatus;
import org.example.fitpass.common.Image;
import org.example.fitpass.domain.gym.GymStatus;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.entity.User;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "gyms")
public class Gym extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gymImage;

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

    private Boolean isDeleted = false;

    public Gym(String gymImage, String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, User user) {
        this.gymImage = gymImage;
        this.name = name;
        this.number = number;
        this.content = content;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public static Gym of(String gymImage, String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, User user) {
        return new Gym(gymImage, name, number, content, address, openTime, closeTime, user);
    }
    private List<Trainer> trainers = new ArrayList<>();

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

}

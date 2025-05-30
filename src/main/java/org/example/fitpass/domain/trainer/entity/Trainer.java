package org.example.fitpass.domain.trainer.entity;

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
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.common.Image;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;


@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trainers")
public class Trainer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private TrainerStatus trainerStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

    public Trainer(List<Image> trainerImage,String name, int price, String content, TrainerStatus trainerStatus) {
        Trainer trainer = new Trainer();
        this.name = name;
        this.price = price;
        this.content = content;
        this.trainerStatus = trainerStatus;

        for (Image image : trainerImage) {
            image.assignToTrainer(trainer);
            trainer.getImages().add(image);
        }
    }

    public static Trainer of(List<Image> trainerImage, String name, int price, String content, TrainerStatus trainerStatus) {
        return new Trainer(trainerImage, name, price, content, trainerStatus);
    }

    public void updatePhoto(List<String> imageUrls, Trainer trainer) {
        this.images.clear();
        List<Image> convertedImages = imageUrls.stream()
            .map( url ->   Image.from(url,trainer))
            .toList();
        this.images.addAll(convertedImages);
    }

    public void update(List<Image> images, String name, int price, String content,
        TrainerStatus trainerStatus) {
        this.images = images;
        this.name = name;
        this.price = price;
        this.content = content;
        this.trainerStatus = trainerStatus;
    }
}

package org.example.fitpass.domain.trainer.entity;

import static org.example.fitpass.common.error.ExceptionCode.INVALID_GYM_TRAINER_RELATION;

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
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.common.error.BaseException;
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

    @Column(nullable = false)
    private String experience;

    @Enumerated(EnumType.STRING)
    private TrainerStatus trainerStatus = TrainerStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    public Trainer(List<Image> trainerImage, String name, int price, String content, String experience) {
        this.name = name;
        this.price = price;
        this.content = content;
        this.experience = experience;

        for (Image image : trainerImage) {
            image.assignToTrainer(this);
            this.images.add(image);
        }
    }

    public static Trainer of(List<String> trainerImage, String name, int price, String content, String experience) {
        List<Image> images = trainerImage.stream()
            .map(Image::new)
            .toList();
        return new Trainer(images, name, price, content, experience);
    }

    public void updatePhoto(List<String> imageUrls, Trainer trainer) {
        this.images.clear();
        List<Image> convertedImages = imageUrls.stream()
            .map(url -> Image.from(url, trainer))
            .toList();
        this.images.addAll(convertedImages);
    }

    public void update(String name, int price, String content,
        TrainerStatus trainerStatus, String experience, List<String> updatedImages) {
        this.name = name;
        this.price = price;
        this.content = content;
        this.trainerStatus = trainerStatus;
        this.experience = experience;
        if (updatedImages != null) {
            List<Image> newImages = updatedImages.stream()
                .map(url -> {
                    Image img = new Image(url);
                    img.assignToTrainer(this);
                    return img;
                })
                .toList();
            this.images.addAll(newImages);
        }
    }

    public void validateTrainerBelongsToGym(Trainer trainer, Gym gym) {
        if (!trainer.getGym().getId().equals(gym.getId())) {
            throw new BaseException(INVALID_GYM_TRAINER_RELATION);
        }
    }

    public void assignToGym(Gym gym) {
        this.gym = gym;
    }

}

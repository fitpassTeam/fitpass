package org.example.fitpass.common;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.trainer.entity.Trainer;

@Entity
@Getter
@Table(name = "images")
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue
    private Long id;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    public void assignToTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    private Image(String url, Trainer trainer) {
        this.url = url;
        this.trainer = trainer;
    }

    public static Image from(String url, Trainer trainer) {
        return new Image(url, trainer);
    }

    public void assignToGym(Gym gym) {
        this.gym = gym;
    }

    private Image(String url, Gym gym) {
        this.url = url;
        this.gym = gym;
    }

    public static Image from(String url, Gym gym) {
        return new Image(url, gym);
    }

}

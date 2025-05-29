package org.example.fitpass.domain.trainer.entity;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.trainer.TrainerStatus;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trainers")
public class Trainer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String trainerImage;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = true)
    private String content;

    @Enumerated(EnumType.STRING)
    private TrainerStatus trainerStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    public void update(String trainerImage, String name, int price, String content, TrainerStatus trainerStatus){
        this.trainerImage = trainerImage;
        this.name = name;
        this.price = price;
        this.content = content;
        this.trainerStatus = trainerStatus;
    }
}

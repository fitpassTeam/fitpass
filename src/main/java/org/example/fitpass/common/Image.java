package org.example.fitpass.common;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import org.example.fitpass.domain.gym.entity.Gym;

@Entity
@Getter
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue
    private Long id;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    public void assignToGym(Gym gym){
        this.gym = gym;
    }

    private Image(String url, Gym gym) {
        this.url = url;
        this.gym = null;
    }

    public static Image from(String url, Gym gym) {
        return new Image(url, gym);
    }

}

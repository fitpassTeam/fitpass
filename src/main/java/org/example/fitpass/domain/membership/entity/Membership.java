package org.example.fitpass.domain.membership.entity;

import static org.example.fitpass.common.error.ExceptionCode.NOT_BELONG_TO_GYM;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.gym.entity.Gym;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "memberships")
public class Membership extends BaseEntity {

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
    private int durationInDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    public Membership(String name, int price, String content, int durationInDays) {
        this.name = name;
        this.price = price;
        this.content = content;
        this.durationInDays = durationInDays;
    }

    public static Membership of(String name, int price, String content, int durationInDays) {
        return new Membership(name, price, content, durationInDays);
    }

    public void update(String name, int price, String content, int durationInDays) {
        this.name = name;
        this.price = price;
        this.content = content;
        this.durationInDays = durationInDays;
    }

    public void assignToGym(Gym gym) {
        this.gym = gym;
    }

}

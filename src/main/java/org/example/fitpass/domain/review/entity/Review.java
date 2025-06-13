package org.example.fitpass.domain.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.review.dto.request.ReviewCreateRequestDto;
import org.example.fitpass.domain.user.entity.User;

@Getter
@Entity
@Table(name = "reviews")
@NoArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int gymRating;

    @Column(nullable = false)
    private int trainerRating;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    public Review(String content, int gymRating, int trainerRating, User user, Reservation reservation) {
        this.content = content;
        this.gymRating = gymRating;
        this.trainerRating = trainerRating;
        this.user = user;
        this.reservation = reservation;
    }

    public static Review of(Reservation reservation, ReviewCreateRequestDto requestDto, User user) {
        return new Review(
            requestDto.content(),
            requestDto.gymRating(),
            requestDto.trainerRating(),
            user,
            reservation
        );
    }

    public void update(String content, int gymRating, int trainerRating) {
        if (content != null) {
            this.content = content;
        }
        if (gymRating >= 1 && gymRating <= 5) {
            this.gymRating = gymRating;
        }
        if (trainerRating >= 1 && trainerRating <= 5) {
            this.trainerRating = trainerRating;
        }
    }


}

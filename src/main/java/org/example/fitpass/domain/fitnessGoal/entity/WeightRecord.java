package org.example.fitpass.domain.fitnessGoal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "weight_records")
public class WeightRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fitness_goal_id", nullable = false)
    private FitnessGoal fitnessGoal;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(columnDefinition = "TEXT")
    private String memo;

    public WeightRecord(FitnessGoal fitnessGoal, double weight, LocalDate recordDate, String memo) {
        this.fitnessGoal = fitnessGoal;
        this.weight = weight;
        this.recordDate = recordDate;
        this.memo = memo;
    }

    public static WeightRecord of(FitnessGoal fitnessGoal, double weight, LocalDate recordDate,
        String memo) {
        return new WeightRecord(fitnessGoal, weight, recordDate, memo);
    }

    public void updateRecord(double weight, LocalDate recordDate, String memo) {
        this.weight = weight;
        this.recordDate = recordDate;
        this.memo = memo;
    }

}

package org.example.fitpass.domain.fitnessGoal.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.entity.BaseEntity;
import org.example.fitpass.common.Image.entity.Image;

@Getter
@Entity
@Table(name = "daily_records")
@NoArgsConstructor
public class DailyRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fitness_goal_id", nullable = false)
    private FitnessGoal fitnessGoal;

    @OneToMany(mappedBy = "dailyRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(nullable = false)
    private LocalDate recordDate;

    public DailyRecord(List<Image> images, FitnessGoal fitnessGoal,
        LocalDate recordDate, String memo) {
        this.fitnessGoal = fitnessGoal;
        this.recordDate = recordDate;
        this.memo = memo;

        for(Image image : images) {
            image.assignToDailyRecord(this); // DailyRecord 이미지라는 연관매핑
            this.images.add(image);
        }
    }

    public static DailyRecord of(List<String> dailyRecordImage, FitnessGoal fitnessGoal,
        LocalDate recordDate, String memo) {
        List<Image> images = dailyRecordImage.stream()
            .map(Image::new)
            .toList();
        return new DailyRecord(images,fitnessGoal, recordDate, memo);
    }



}

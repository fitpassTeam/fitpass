package org.example.fitpass.domain.point.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jdk.jshell.Snippet.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.point.PointStatus;
import org.example.fitpass.domain.point.PointType;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "points")
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int balance;

    @Enumerated(EnumType.STRING)
    private PointType pointType;

    @Enumerated(EnumType.STRING)
    private PointStatus pointStatus;

}

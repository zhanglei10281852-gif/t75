package com.mediation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_id", nullable = false)
    private Long trainingId;

    @Column(name = "mediator_id", nullable = false)
    private Long mediatorId;

    @Column(nullable = false)
    private int score;

    @Column(name = "passed")
    private boolean passed;

    @Column(name = "hours_earned")
    private int hoursEarned = 0;

    @Column(name = "need_makeup")
    private boolean needMakeup = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.passed = this.score >= 60;
    }
}

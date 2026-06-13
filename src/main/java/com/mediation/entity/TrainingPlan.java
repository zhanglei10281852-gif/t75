package com.mediation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type", nullable = false)
    private TrainingType trainingType;

    @Column(name = "training_time", nullable = false)
    private LocalDateTime trainingTime;

    @Column(nullable = false)
    private String location;

    @Column(name = "instructor_info")
    private String instructorInfo;

    @Column(name = "planned_count")
    private int plannedCount = 0;

    @Column(nullable = false)
    private int hours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TrainingStatus.计划中;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum TrainingType {
        岗前培训, 在职培训, 专题培训, 换届培训
    }

    public enum TrainingStatus {
        计划中, 报名中, 进行中, 已完成
    }
}

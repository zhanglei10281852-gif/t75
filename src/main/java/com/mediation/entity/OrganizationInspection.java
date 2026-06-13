package com.mediation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization_inspections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(nullable = false)
    private String inspector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionConclusion conclusion;

    @Column(columnDefinition = "TEXT")
    private String problems;

    @Column(name = "rectification_requirements", columnDefinition = "TEXT")
    private String rectificationRequirements;

    @Column(name = "rectification_deadline")
    private LocalDate rectificationDeadline;

    @Column(name = "is_rechecked")
    private Boolean rechecked = false;

    @Column(name = "recheck_date")
    private LocalDate recheckDate;

    @Column(name = "recheck_conclusion")
    @Enumerated(EnumType.STRING)
    private InspectionConclusion recheckConclusion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum InspectionConclusion {
        合格, 基本合格, 不合格
    }
}

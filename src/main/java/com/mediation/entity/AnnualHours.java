package com.mediation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "annual_hours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mediator_id", nullable = false)
    private Long mediatorId;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "completed_hours")
    private int completedHours = 0;

    @Column(name = "required_hours")
    private int requiredHours;

    @Column(name = "is_compliant")
    private boolean compliant;
}

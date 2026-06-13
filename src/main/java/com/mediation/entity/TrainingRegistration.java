package com.mediation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_id", nullable = false)
    private Long trainingId;

    @Column(name = "mediator_id", nullable = false)
    private Long mediatorId;

    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "sign_in_time")
    private LocalDateTime signInTime;

    @Column(name = "signed_in")
    private boolean signedIn = false;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }
}

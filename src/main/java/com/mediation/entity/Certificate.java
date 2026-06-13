package com.mediation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mediator_id", nullable = false)
    private Long mediatorId;

    @Column(name = "cert_no", unique = true, nullable = false)
    private String certNo;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "valid_years", nullable = false)
    private int validYears = 3;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertStatus status;

    @Column(name = "renewal_date")
    private LocalDate renewalDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.expiryDate == null && this.issueDate != null) {
            this.expiryDate = this.issueDate.plusYears(this.validYears);
        }
        if (this.status == null) {
            this.status = CertStatus.有效;
        }
    }

    public enum CertStatus {
        有效, 即将到期, 已过期, 已续证
    }
}

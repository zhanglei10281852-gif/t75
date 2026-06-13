package com.mediation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_type", nullable = false)
    private OrgType orgType;

    @Column(name = "jurisdiction", nullable = false)
    private String jurisdiction;

    @Column(name = "establish_date", nullable = false)
    private LocalDate establishDate;

    @Column(nullable = false)
    private String leader;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    @Column(name = "mediator_count")
    private int mediatorCount = 0;

    @Column(name = "office_address")
    private String officeAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrgStatus.正常运转;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum OrgType {
        人民调解委员会, 行业性调解组织, 企事业单位调解组织
    }

    public enum OrgStatus {
        正常运转, 整改中, 已撤销
    }
}

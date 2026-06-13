package com.mediation.repository;

import com.mediation.entity.Organization;
import com.mediation.entity.Organization.OrgStatus;
import com.mediation.entity.Organization.OrgType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Page<Organization> findByStatus(OrgStatus status, Pageable pageable);

    Page<Organization> findByOrgType(OrgType orgType, Pageable pageable);

    Page<Organization> findByJurisdiction(String jurisdiction, Pageable pageable);

    @Query("SELECT o FROM Organization o WHERE o.name LIKE %:keyword%")
    Page<Organization> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByOrgType(OrgType orgType);

    long countByStatus(OrgStatus status);

    @Query("SELECT o FROM Organization o WHERE o.orgType = :type AND o.status = :status")
    Page<Organization> findByOrgTypeAndStatus(@Param("type") OrgType orgType, @Param("status") OrgStatus status, Pageable pageable);
}

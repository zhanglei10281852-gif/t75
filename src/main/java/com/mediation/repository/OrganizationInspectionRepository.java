package com.mediation.repository;

import com.mediation.entity.OrganizationInspection;
import com.mediation.entity.OrganizationInspection.InspectionConclusion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationInspectionRepository extends JpaRepository<OrganizationInspection, Long> {

    Page<OrganizationInspection> findByOrganizationId(Long organizationId, Pageable pageable);

    List<OrganizationInspection> findByOrganizationIdOrderByYearDesc(Long organizationId);

    Page<OrganizationInspection> findByYear(Integer year, Pageable pageable);

    @Query("SELECT i FROM OrganizationInspection i WHERE i.organizationId = :orgId AND i.year = :year")
    List<OrganizationInspection> findByOrganizationIdAndYear(@Param("orgId") Long organizationId, @Param("year") Integer year);

    long countByConclusion(InspectionConclusion conclusion);

    long countByYear(Integer year);

    @Query("SELECT i FROM OrganizationInspection i WHERE i.conclusion = :conclusion AND i.rechecked = false AND i.rectificationDeadline < CURRENT_DATE")
    List<OrganizationInspection> findOverdueRectifications(@Param("conclusion") InspectionConclusion conclusion);

    @Query("SELECT COUNT(i) FROM OrganizationInspection i WHERE i.year = :year AND i.conclusion IN :conclusions")
    long countByYearAndConclusionIn(@Param("year") Integer year, @Param("conclusions") List<InspectionConclusion> conclusions);
}

package com.mediation.repository;

import com.mediation.entity.AnnualHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnualHoursRepository extends JpaRepository<AnnualHours, Long> {

    Optional<AnnualHours> findByMediatorIdAndYear(Long mediatorId, Integer year);

    List<AnnualHours> findByYear(Integer year);

    @Query("SELECT ah FROM AnnualHours ah WHERE ah.mediatorId = :mediatorId ORDER BY ah.year DESC")
    List<AnnualHours> findByMediatorIdOrderByYearDesc(@Param("mediatorId") Long mediatorId);

    @Query("SELECT COUNT(ah) FROM AnnualHours ah WHERE ah.year = :year AND ah.compliant = true")
    long countCompliantByYear(@Param("year") Integer year);

    @Query("SELECT COUNT(ah) FROM AnnualHours ah WHERE ah.year = :year AND ah.compliant = false")
    long countNonCompliantByYear(@Param("year") Integer year);

    long countByYear(Integer year);
}

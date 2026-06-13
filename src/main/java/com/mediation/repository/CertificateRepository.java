package com.mediation.repository;

import com.mediation.entity.Certificate;
import com.mediation.entity.Certificate.CertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByMediatorId(Long mediatorId);

    List<Certificate> findByStatus(CertStatus status);

    @Query("SELECT c FROM Certificate c WHERE c.expiryDate BETWEEN :start AND :end AND c.status = :status")
    List<Certificate> findByExpiryDateBetweenAndStatus(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("status") CertStatus status);

    @Query("SELECT c FROM Certificate c WHERE c.expiryDate <= :date AND c.status = :status")
    List<Certificate> findExpiredByDate(@Param("date") LocalDate date, @Param("status") CertStatus status);

    long countByStatus(CertStatus status);

    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.mediatorId IN :mediatorIds AND c.status = :status")
    long countByMediatorIdInAndStatus(@Param("mediatorIds") List<Long> mediatorIds, @Param("status") CertStatus status);

    Page<Certificate> findAll(Pageable pageable);
}

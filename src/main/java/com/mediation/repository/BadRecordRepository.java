package com.mediation.repository;

import com.mediation.entity.BadRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadRecordRepository extends JpaRepository<BadRecord, Long> {

    Page<BadRecord> findByMediatorId(Long mediatorId, Pageable pageable);

    List<BadRecord> findByMediatorId(Long mediatorId);

    @Query("SELECT b FROM BadRecord b WHERE b.mediatorId = :mediatorId AND b.revoked = false")
    List<BadRecord> findActiveByMediatorId(@Param("mediatorId") Long mediatorId);

    @Query("SELECT COUNT(b) FROM BadRecord b WHERE b.mediatorId = :mediatorId AND b.revoked = false")
    long countActiveByMediatorId(@Param("mediatorId") Long mediatorId);

    Page<BadRecord> findByRevokedFalse(Pageable pageable);

    Page<BadRecord> findByRevokedTrue(Pageable pageable);
}

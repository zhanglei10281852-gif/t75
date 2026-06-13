package com.mediation.repository;

import com.mediation.entity.TrainingRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRegistrationRepository extends JpaRepository<TrainingRegistration, Long> {

    Page<TrainingRegistration> findByTrainingId(Long trainingId, Pageable pageable);

    List<TrainingRegistration> findByTrainingId(Long trainingId);

    Page<TrainingRegistration> findByMediatorId(Long mediatorId, Pageable pageable);

    List<TrainingRegistration> findByMediatorId(Long mediatorId);

    @Query("SELECT r FROM TrainingRegistration r WHERE r.trainingId = :trainingId AND r.mediatorId = :mediatorId")
    Optional<TrainingRegistration> findByTrainingIdAndMediatorId(@Param("trainingId") Long trainingId, @Param("mediatorId") Long mediatorId);

    long countByTrainingId(Long trainingId);

    long countByTrainingIdAndSignedInTrue(Long trainingId);

    boolean existsByTrainingIdAndMediatorId(Long trainingId, Long mediatorId);
}

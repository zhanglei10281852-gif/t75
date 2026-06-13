package com.mediation.repository;

import com.mediation.entity.TrainingAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingAssessmentRepository extends JpaRepository<TrainingAssessment, Long> {

    Page<TrainingAssessment> findByTrainingId(Long trainingId, Pageable pageable);

    List<TrainingAssessment> findByTrainingId(Long trainingId);

    Page<TrainingAssessment> findByMediatorId(Long mediatorId, Pageable pageable);

    List<TrainingAssessment> findByMediatorId(Long mediatorId);

    @Query("SELECT a FROM TrainingAssessment a WHERE a.trainingId = :trainingId AND a.mediatorId = :mediatorId")
    Optional<TrainingAssessment> findByTrainingIdAndMediatorId(@Param("trainingId") Long trainingId, @Param("mediatorId") Long mediatorId);

    List<TrainingAssessment> findByMediatorIdAndPassedTrue(Long mediatorId);
}

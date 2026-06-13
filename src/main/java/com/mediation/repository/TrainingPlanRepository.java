package com.mediation.repository;

import com.mediation.entity.TrainingPlan;
import com.mediation.entity.TrainingPlan.TrainingStatus;
import com.mediation.entity.TrainingPlan.TrainingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {

    Page<TrainingPlan> findByStatus(TrainingStatus status, Pageable pageable);

    Page<TrainingPlan> findByTrainingType(TrainingType trainingType, Pageable pageable);

    @Query("SELECT t FROM TrainingPlan t WHERE t.topic LIKE %:keyword%")
    Page<TrainingPlan> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT t FROM TrainingPlan t WHERE t.trainingTime BETWEEN :start AND :end")
    List<TrainingPlan> findByTrainingTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM TrainingPlan t WHERE t.trainingTime BETWEEN :start AND :end")
    long countByTrainingTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM TrainingPlan t WHERE t.status = :status ORDER BY t.trainingTime ASC")
    Page<TrainingPlan> findByStatusOrderByTrainingTime(@Param("status") TrainingStatus status, Pageable pageable);
}

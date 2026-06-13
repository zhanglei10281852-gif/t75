package com.mediation.controller;

import com.mediation.dto.TrainingPlanDTO;
import com.mediation.entity.TrainingPlan;
import com.mediation.entity.TrainingPlan.TrainingStatus;
import com.mediation.entity.TrainingPlan.TrainingType;
import com.mediation.repository.TrainingPlanRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingPlanController {

    private final TrainingPlanRepository trainingPlanRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TrainingPlanDTO dto) {
        TrainingType trainingType;
        try {
            trainingType = TrainingType.valueOf(dto.getTrainingType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的培训类型"));
        }

        TrainingPlan plan = TrainingPlan.builder()
                .topic(dto.getTopic())
                .trainingType(trainingType)
                .trainingTime(dto.getTrainingTime())
                .location(dto.getLocation())
                .instructorInfo(dto.getInstructorInfo())
                .plannedCount(dto.getPlannedCount() != null ? dto.getPlannedCount() : 0)
                .hours(dto.getHours())
                .build();

        TrainingPlan saved = trainingPlanRepository.save(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<Page<TrainingPlan>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String trainingType,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<TrainingPlan> result;

        if (status != null) {
            TrainingStatus ts = TrainingStatus.valueOf(status);
            result = trainingPlanRepository.findByStatus(ts, pageable);
        } else if (trainingType != null) {
            TrainingType tt = TrainingType.valueOf(trainingType);
            result = trainingPlanRepository.findByTrainingType(tt, pageable);
        } else if (keyword != null) {
            result = trainingPlanRepository.searchByKeyword(keyword, pageable);
        } else {
            result = trainingPlanRepository.findAll(pageable);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return trainingPlanRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "状态不能为空"));
        }

        TrainingStatus targetStatus;
        try {
            targetStatus = TrainingStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的状态值，可选值：计划中/报名中/进行中/已完成"));
        }

        return trainingPlanRepository.findById(id)
                .map(plan -> {
                    TrainingStatus current = plan.getStatus();
                    if (targetStatus.ordinal() != current.ordinal() + 1) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "状态只能按顺序变更：计划中→报名中→进行中→已完成"));
                    }
                    plan.setStatus(targetStatus);
                    trainingPlanRepository.save(plan);
                    return ResponseEntity.ok(plan);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TrainingPlanDTO dto) {
        var planOpt = trainingPlanRepository.findById(id);
        if (planOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TrainingType trainingType;
        try {
            trainingType = TrainingType.valueOf(dto.getTrainingType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的培训类型"));
        }

        TrainingPlan plan = planOpt.get();
        plan.setTopic(dto.getTopic());
        plan.setTrainingType(trainingType);
        plan.setTrainingTime(dto.getTrainingTime());
        plan.setLocation(dto.getLocation());
        plan.setInstructorInfo(dto.getInstructorInfo());
        plan.setPlannedCount(dto.getPlannedCount() != null ? dto.getPlannedCount() : 0);
        plan.setHours(dto.getHours());

        trainingPlanRepository.save(plan);
        return ResponseEntity.ok(plan);
    }
}

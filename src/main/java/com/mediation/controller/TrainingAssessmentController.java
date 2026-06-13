package com.mediation.controller;

import com.mediation.dto.TrainingAssessmentDTO;
import com.mediation.entity.AnnualHours;
import com.mediation.entity.Mediator;
import com.mediation.entity.Mediator.MediatorLevel;
import com.mediation.entity.TrainingAssessment;
import com.mediation.entity.TrainingPlan;
import com.mediation.entity.TrainingPlan.TrainingStatus;
import com.mediation.repository.AnnualHoursRepository;
import com.mediation.repository.MediatorRepository;
import com.mediation.repository.TrainingAssessmentRepository;
import com.mediation.repository.TrainingPlanRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class TrainingAssessmentController {

    private final TrainingAssessmentRepository assessmentRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final MediatorRepository mediatorRepository;
    private final AnnualHoursRepository annualHoursRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TrainingAssessmentDTO dto) {
        Optional<TrainingPlan> planOpt = trainingPlanRepository.findById(dto.getTrainingId());
        if (planOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "培训计划不存在"));
        }

        TrainingPlan plan = planOpt.get();
        if (plan.getStatus() != TrainingStatus.已完成) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "培训未完成，无法进行考核"));
        }

        Optional<TrainingAssessment> existing = assessmentRepository.findByTrainingIdAndMediatorId(dto.getTrainingId(), dto.getMediatorId());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "该调解员已对此培训进行过考核"));
        }

        TrainingAssessment assessment = TrainingAssessment.builder()
                .trainingId(dto.getTrainingId())
                .mediatorId(dto.getMediatorId())
                .score(dto.getScore())
                .build();

        TrainingAssessment saved = assessmentRepository.save(assessment);

        if (saved.isPassed()) {
            saved.setHoursEarned(plan.getHours());
            saved.setNeedMakeup(false);
            assessmentRepository.save(saved);

            updateAnnualHours(dto.getMediatorId(), plan.getHours());
        } else {
            saved.setNeedMakeup(true);
            assessmentRepository.save(saved);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Long trainingId,
            @RequestParam(required = false) Long mediatorId) {
        if (trainingId != null && mediatorId != null) {
            return assessmentRepository.findByTrainingIdAndMediatorId(trainingId, mediatorId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else if (trainingId != null) {
            List<TrainingAssessment> list = assessmentRepository.findByTrainingId(trainingId);
            return ResponseEntity.ok(list);
        } else if (mediatorId != null) {
            List<TrainingAssessment> list = assessmentRepository.findByMediatorId(mediatorId);
            return ResponseEntity.ok(list);
        }
        return ResponseEntity.ok(assessmentRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return assessmentRepository.findById(id)
                .map(assessment -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("id", assessment.getId());
                    response.put("trainingId", assessment.getTrainingId());
                    response.put("mediatorId", assessment.getMediatorId());
                    response.put("score", assessment.getScore());
                    response.put("passed", assessment.isPassed());
                    response.put("hoursEarned", assessment.getHoursEarned());
                    response.put("needMakeup", assessment.isNeedMakeup());
                    response.put("createdAt", assessment.getCreatedAt());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void updateAnnualHours(Long mediatorId, int hours) {
        int currentYear = Year.now().getValue();

        Optional<Mediator> mediatorOpt = mediatorRepository.findById(mediatorId);
        if (mediatorOpt.isEmpty()) {
            return;
        }

        Mediator mediator = mediatorOpt.get();
        int requiredHours = getRequiredHours(mediator.getLevel());

        AnnualHours annualHours = annualHoursRepository.findByMediatorIdAndYear(mediatorId, currentYear)
                .orElseGet(() -> AnnualHours.builder()
                        .mediatorId(mediatorId)
                        .year(currentYear)
                        .requiredHours(requiredHours)
                        .completedHours(0)
                        .compliant(false)
                        .build());

        annualHours.setCompletedHours(annualHours.getCompletedHours() + hours);
        annualHours.setCompliant(annualHours.getCompletedHours() >= annualHours.getRequiredHours());
        annualHoursRepository.save(annualHours);
    }

    private int getRequiredHours(MediatorLevel level) {
        return switch (level) {
            case 初级 -> 24;
            case 中级 -> 32;
            case 高级 -> 40;
        };
    }
}

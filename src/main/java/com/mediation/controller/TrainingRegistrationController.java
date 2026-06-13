package com.mediation.controller;

import com.mediation.dto.TrainingRegistrationDTO;
import com.mediation.entity.TrainingPlan;
import com.mediation.entity.TrainingPlan.TrainingStatus;
import com.mediation.entity.TrainingRegistration;
import com.mediation.repository.AnnualHoursRepository;
import com.mediation.repository.MediatorRepository;
import com.mediation.repository.TrainingAssessmentRepository;
import com.mediation.repository.TrainingPlanRepository;
import com.mediation.repository.TrainingRegistrationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class TrainingRegistrationController {

    private final TrainingRegistrationRepository registrationRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final MediatorRepository mediatorRepository;
    private final AnnualHoursRepository annualHoursRepository;
    private final TrainingAssessmentRepository trainingAssessmentRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TrainingRegistrationDTO dto) {
        var trainingOpt = trainingPlanRepository.findById(dto.getTrainingId());
        if (trainingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "培训计划不存在"));
        }

        TrainingPlan training = trainingOpt.get();
        if (training.getStatus() != TrainingStatus.报名中 && training.getStatus() != TrainingStatus.进行中) {
            return ResponseEntity.badRequest().body(Map.of("error", "该培训当前不在报名中"));
        }

        if (!mediatorRepository.existsById(dto.getMediatorId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "调解员不存在"));
        }

        if (registrationRepository.existsByTrainingIdAndMediatorId(dto.getTrainingId(), dto.getMediatorId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "该调解员已报名此培训"));
        }

        TrainingRegistration registration = TrainingRegistration.builder()
                .trainingId(dto.getTrainingId())
                .mediatorId(dto.getMediatorId())
                .build();

        TrainingRegistration saved = registrationRepository.save(registration);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Long trainingId,
            @RequestParam(required = false) Long mediatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("registeredAt").descending());

        Page<TrainingRegistration> result;

        if (trainingId != null) {
            result = registrationRepository.findByTrainingId(trainingId, pageable);
        } else if (mediatorId != null) {
            result = registrationRepository.findByMediatorId(mediatorId, pageable);
        } else {
            result = registrationRepository.findAll(pageable);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/sign-in")
    public ResponseEntity<?> signIn(@PathVariable Long id) {
        var registrationOpt = registrationRepository.findById(id);
        if (registrationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TrainingRegistration registration = registrationOpt.get();

        if (registration.isSignedIn()) {
            return ResponseEntity.badRequest().body(Map.of("error", "该调解员已签到"));
        }

        var trainingOpt = trainingPlanRepository.findById(registration.getTrainingId());
        if (trainingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "培训计划不存在"));
        }

        TrainingPlan training = trainingOpt.get();
        if (training.getStatus() != TrainingStatus.进行中) {
            return ResponseEntity.badRequest().body(Map.of("error", "只有进行中的培训才能签到"));
        }

        registration.setSignInTime(LocalDateTime.now());
        registration.setSignedIn(true);
        registrationRepository.save(registration);

        return ResponseEntity.ok(registration);
    }

    @GetMapping("/sign-in-rate")
    public ResponseEntity<?> signInRate(@RequestParam Long trainingId) {
        long totalRegistered = registrationRepository.countByTrainingId(trainingId);
        long totalSignedIn = registrationRepository.countByTrainingIdAndSignedInTrue(trainingId);

        double signInRate = totalRegistered > 0 ? (totalSignedIn * 100.0 / totalRegistered) : 0.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRegistered", totalRegistered);
        result.put("totalSignedIn", totalSignedIn);
        result.put("signInRate", signInRate);

        return ResponseEntity.ok(result);
    }
}

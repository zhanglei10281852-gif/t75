package com.mediation.controller;

import com.mediation.dto.CertificateDTO;
import com.mediation.entity.AnnualHours;
import com.mediation.entity.Certificate;
import com.mediation.entity.Certificate.CertStatus;
import com.mediation.entity.TrainingAssessment;
import com.mediation.repository.AnnualHoursRepository;
import com.mediation.repository.CertificateRepository;
import com.mediation.repository.MediatorRepository;
import com.mediation.repository.TrainingAssessmentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateRepository certificateRepository;
    private final MediatorRepository mediatorRepository;
    private final AnnualHoursRepository annualHoursRepository;
    private final TrainingAssessmentRepository assessmentRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CertificateDTO dto) {
        if (!mediatorRepository.existsById(dto.getMediatorId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "调解员不存在"));
        }

        if (certificateRepository.findByMediatorId(dto.getMediatorId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "证书编号已存在"));
        }

        Certificate certificate = Certificate.builder()
                .mediatorId(dto.getMediatorId())
                .certNo(dto.getCertNo())
                .issueDate(dto.getIssueDate())
                .validYears(dto.getValidYears() != null ? dto.getValidYears() : 3)
                .build();

        Certificate saved = certificateRepository.save(certificate);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<Page<Certificate>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(certificateRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return certificateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mediator/{mediatorId}")
    public ResponseEntity<?> getByMediatorId(@PathVariable Long mediatorId) {
        return certificateRepository.findByMediatorId(mediatorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/expiring-warning")
    public ResponseEntity<List<Certificate>> expiringWarning() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(90);
        List<Certificate> certificates = certificateRepository.findByExpiryDateBetweenAndStatus(today, threshold, CertStatus.有效);
        return ResponseEntity.ok(certificates);
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<?> renew(@PathVariable Long id) {
        Optional<Certificate> certOpt = certificateRepository.findById(id);
        if (certOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Certificate oldCert = certOpt.get();
        int currentYear = Year.now().getValue();

        Optional<AnnualHours> hoursOpt = annualHoursRepository.findByMediatorIdAndYear(oldCert.getMediatorId(), currentYear);
        if (hoursOpt.isEmpty() || !hoursOpt.get().isCompliant()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "本年度学时不达标，无法续证"));
        }

        List<TrainingAssessment> passedAssessments = assessmentRepository.findByMediatorIdAndPassedTrue(oldCert.getMediatorId());
        boolean hasPassedThisYear = passedAssessments.stream()
                .anyMatch(a -> a.getCreatedAt().getYear() == currentYear);
        if (!hasPassedThisYear) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "本年度无通过考核记录，无法续证"));
        }

        if (oldCert.getStatus() == CertStatus.已过期 || oldCert.getStatus() == CertStatus.已续证) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "该证书状态不允许续证"));
        }

        Certificate newCert = Certificate.builder()
                .mediatorId(oldCert.getMediatorId())
                .certNo(oldCert.getCertNo() + "-R")
                .issueDate(LocalDate.now())
                .validYears(oldCert.getValidYears())
                .build();
        Certificate savedNewCert = certificateRepository.save(newCert);

        oldCert.setStatus(CertStatus.已续证);
        oldCert.setRenewalDate(LocalDate.now());
        certificateRepository.save(oldCert);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("oldCertificate", oldCert);
        response.put("newCertificate", savedNewCert);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/check-expiry")
    public ResponseEntity<?> checkExpiry() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(90);

        List<Certificate> expired = certificateRepository.findExpiredByDate(today, CertStatus.有效);
        for (Certificate cert : expired) {
            cert.setStatus(CertStatus.已过期);
            certificateRepository.save(cert);
        }

        List<Certificate> expiring = certificateRepository.findByExpiryDateBetweenAndStatus(today, threshold, CertStatus.有效);
        for (Certificate cert : expiring) {
            cert.setStatus(CertStatus.即将到期);
            certificateRepository.save(cert);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("expiredCount", expired.size());
        response.put("expiringCount", expiring.size());
        return ResponseEntity.ok(response);
    }
}

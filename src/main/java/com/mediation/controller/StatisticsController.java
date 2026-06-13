package com.mediation.controller;

import com.mediation.entity.Certificate;
import com.mediation.entity.Certificate.CertStatus;
import com.mediation.entity.Mediator;
import com.mediation.entity.Mediator.MediatorLevel;
import com.mediation.entity.Mediator.MediatorStatus;
import com.mediation.entity.Organization;
import com.mediation.entity.Organization.OrgType;
import com.mediation.entity.OrganizationInspection;
import com.mediation.entity.OrganizationInspection.InspectionConclusion;
import com.mediation.entity.TrainingPlan;
import com.mediation.entity.TrainingRegistration;
import com.mediation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final OrganizationRepository organizationRepository;
    private final OrganizationInspectionRepository inspectionRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingRegistrationRepository registrationRepository;
    private final TrainingAssessmentRepository assessmentRepository;
    private final AnnualHoursRepository annualHoursRepository;
    private final CertificateRepository certificateRepository;
    private final MediatorRepository mediatorRepository;

    @GetMapping("/org-distribution")
    public ResponseEntity<?> orgDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (OrgType type : OrgType.values()) {
            distribution.put(type.name(), organizationRepository.countByOrgType(type));
        }
        long total = organizationRepository.count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("distribution", distribution);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/inspection-pass-rate")
    public ResponseEntity<?> inspectionPassRate(@RequestParam(defaultValue = "2025") Integer year) {
        long total = inspectionRepository.countByYear(year);
        if (total == 0) {
            return ResponseEntity.ok(Map.of("year", year, "total", 0, "passRate", 0.0));
        }
        long qualified = inspectionRepository.countByYearAndConclusionIn(year,
                Arrays.asList(InspectionConclusion.合格, InspectionConclusion.基本合格));
        double passRate = (double) qualified / total * 100;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", year);
        result.put("total", total);
        result.put("qualified", qualified);
        result.put("passRate", Math.round(passRate * 100.0) / 100.0);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/monthly-training")
    public ResponseEntity<?> monthlyTraining(@RequestParam(defaultValue = "2025") Integer year) {
        List<Map<String, Object>> monthly = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            LocalDateTime start = LocalDateTime.of(year, m, 1, 0, 0);
            LocalDateTime end = YearMonth.of(year, m).atEndOfMonth().atTime(23, 59, 59);
            long sessionCount = trainingPlanRepository.countByTrainingTimeBetween(start, end);
            List<TrainingPlan> trainings = trainingPlanRepository.findByTrainingTimeBetween(start, end);
            long attendeeCount = 0;
            for (TrainingPlan t : trainings) {
                attendeeCount += registrationRepository.countByTrainingId(t.getId());
            }
            Map<String, Object> monthData = new LinkedHashMap<>();
            monthData.put("month", m);
            monthData.put("sessionCount", sessionCount);
            monthData.put("attendeeCount", attendeeCount);
            monthly.add(monthData);
        }
        return ResponseEntity.ok(monthly);
    }

    @GetMapping("/hours-compliance")
    public ResponseEntity<?> hoursCompliance(@RequestParam(defaultValue = "2025") Integer year) {
        long total = annualHoursRepository.countByYear(year);
        long compliant = annualHoursRepository.countCompliantByYear(year);
        long nonCompliant = annualHoursRepository.countNonCompliantByYear(year);
        double rate = total > 0 ? (double) compliant / total * 100 : 0.0;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", year);
        result.put("total", total);
        result.put("compliant", compliant);
        result.put("nonCompliant", nonCompliant);
        result.put("complianceRate", Math.round(rate * 100.0) / 100.0);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cert-expiry-warning")
    public ResponseEntity<?> certExpiryWarning() {
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(90);
        List<Certificate> expiring = certificateRepository.findByExpiryDateBetweenAndStatus(today, warningDate, CertStatus.有效);
        List<Certificate> expired = certificateRepository.findExpiredByDate(today, CertStatus.有效);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("warningCount", expiring.size());
        result.put("expiredCount", expired.size());
        result.put("expiringSoon", expiring.stream().map(c -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("certId", c.getId());
            item.put("certNo", c.getCertNo());
            item.put("mediatorId", c.getMediatorId());
            item.put("expiryDate", c.getExpiryDate());
            item.put("daysRemaining", java.time.temporal.ChronoUnit.DAYS.between(today, c.getExpiryDate()));
            return item;
        }).collect(Collectors.toList()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/org-cert-rate")
    public ResponseEntity<?> orgCertRate() {
        List<Organization> orgs = organizationRepository.findAll();
        List<Map<String, Object>> orgRates = new ArrayList<>();
        for (Organization org : orgs) {
            List<Mediator> mediators = mediatorRepository.findByOrganizationId(org.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            if (mediators.isEmpty()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("orgId", org.getId());
                item.put("orgName", org.getName());
                item.put("totalMediators", 0);
                item.put("certifiedMediators", 0);
                item.put("certRate", 0.0);
                orgRates.add(item);
                continue;
            }
            List<Long> mediatorIds = mediators.stream().map(Mediator::getId).collect(Collectors.toList());
            long certifiedCount = certificateRepository.countByMediatorIdInAndStatus(mediatorIds, CertStatus.有效);
            double certRate = (double) certifiedCount / mediators.size() * 100;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orgId", org.getId());
            item.put("orgName", org.getName());
            item.put("totalMediators", mediators.size());
            item.put("certifiedMediators", certifiedCount);
            item.put("certRate", Math.round(certRate * 100.0) / 100.0);
            orgRates.add(item);
        }
        return ResponseEntity.ok(orgRates);
    }

    @GetMapping("/overview")
    public ResponseEntity<?> overview(@RequestParam(defaultValue = "2025") Integer year) {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("orgDistribution", orgDistribution().getBody());
        overview.put("inspectionPassRate", inspectionPassRate(year).getBody());
        overview.put("hoursCompliance", hoursCompliance(year).getBody());
        overview.put("certExpiryWarning", certExpiryWarning().getBody());
        return ResponseEntity.ok(overview);
    }
}

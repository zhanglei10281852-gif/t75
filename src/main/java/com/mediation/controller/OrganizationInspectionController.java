package com.mediation.controller;

import com.mediation.dto.OrganizationInspectionDTO;
import com.mediation.entity.Organization;
import com.mediation.entity.Organization.OrgStatus;
import com.mediation.entity.OrganizationInspection;
import com.mediation.entity.OrganizationInspection.InspectionConclusion;
import com.mediation.repository.OrganizationInspectionRepository;
import com.mediation.repository.OrganizationRepository;
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
import java.util.*;

@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class OrganizationInspectionController {

    private final OrganizationInspectionRepository inspectionRepository;
    private final OrganizationRepository organizationRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrganizationInspectionDTO dto) {
        InspectionConclusion conclusion;
        try {
            conclusion = InspectionConclusion.valueOf(dto.getConclusion());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的检查结论"));
        }

        Optional<Organization> orgOpt = organizationRepository.findById(dto.getOrganizationId());
        if (orgOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "组织不存在"));
        }

        OrganizationInspection inspection = OrganizationInspection.builder()
                .organizationId(dto.getOrganizationId())
                .year(dto.getYear())
                .inspectionDate(dto.getInspectionDate())
                .inspector(dto.getInspector())
                .conclusion(conclusion)
                .problems(dto.getProblems())
                .rectificationRequirements(dto.getRectificationRequirements())
                .rectificationDeadline(dto.getRectificationDeadline())
                .rechecked(false)
                .build();

        OrganizationInspection saved = inspectionRepository.save(inspection);

        if (conclusion == InspectionConclusion.不合格) {
            Organization org = orgOpt.get();
            org.setStatus(OrgStatus.整改中);
            organizationRepository.save(org);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<Page<OrganizationInspection>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) Integer year) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<OrganizationInspection> result;

        if (organizationId != null) {
            result = inspectionRepository.findByOrganizationId(organizationId, pageable);
        } else if (year != null) {
            result = inspectionRepository.findByYear(year, pageable);
        } else {
            result = inspectionRepository.findAll(pageable);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<OrganizationInspection> inspectionOpt = inspectionRepository.findById(id);
        if (inspectionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrganizationInspection inspection = inspectionOpt.get();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", inspection.getId());
        response.put("organizationId", inspection.getOrganizationId());
        response.put("year", inspection.getYear());
        response.put("inspectionDate", inspection.getInspectionDate());
        response.put("inspector", inspection.getInspector());
        response.put("conclusion", inspection.getConclusion());
        response.put("problems", inspection.getProblems());
        response.put("rectificationRequirements", inspection.getRectificationRequirements());
        response.put("rectificationDeadline", inspection.getRectificationDeadline());
        response.put("rechecked", inspection.getRechecked());
        response.put("recheckDate", inspection.getRecheckDate());
        response.put("recheckConclusion", inspection.getRecheckConclusion());
        response.put("createdAt", inspection.getCreatedAt());

        organizationRepository.findById(inspection.getOrganizationId())
                .ifPresent(org -> response.put("organizationName", org.getName()));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/recheck")
    public ResponseEntity<?> recheck(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String recheckDateStr = (String) body.get("recheckDate");
        String recheckConclusionStr = (String) body.get("recheckConclusion");

        if (recheckDateStr == null || recheckConclusionStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "复查日期和复查结论不能为空"));
        }

        InspectionConclusion recheckConclusion;
        try {
            recheckConclusion = InspectionConclusion.valueOf(recheckConclusionStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的复查结论"));
        }

        Optional<OrganizationInspection> inspectionOpt = inspectionRepository.findById(id);
        if (inspectionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrganizationInspection inspection = inspectionOpt.get();

        if (inspection.getRechecked()) {
            return ResponseEntity.badRequest().body(Map.of("error", "该检查已完成复查"));
        }

        inspection.setRechecked(true);
        inspection.setRecheckDate(LocalDate.parse(recheckDateStr));
        inspection.setRecheckConclusion(recheckConclusion);
        inspectionRepository.save(inspection);

        if (recheckConclusion == InspectionConclusion.合格 || recheckConclusion == InspectionConclusion.基本合格) {
            organizationRepository.findById(inspection.getOrganizationId()).ifPresent(org -> {
                if (org.getStatus() == OrgStatus.整改中) {
                    org.setStatus(OrgStatus.正常运转);
                    organizationRepository.save(org);
                }
            });
        }

        return ResponseEntity.ok(inspection);
    }
}

package com.mediation.controller;

import com.mediation.dto.OrganizationDTO;
import com.mediation.entity.Organization;
import com.mediation.entity.Organization.OrgStatus;
import com.mediation.entity.Organization.OrgType;
import com.mediation.repository.MediatorRepository;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationRepository organizationRepository;
    private final MediatorRepository mediatorRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrganizationDTO dto) {
        OrgType orgType;
        try {
            orgType = OrgType.valueOf(dto.getOrgType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的组织类型，可选值：人民调解委员会/行业性调解组织/企事业单位调解组织"));
        }

        OrgStatus status = null;
        if (dto.getStatus() != null) {
            try {
                status = OrgStatus.valueOf(dto.getStatus());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的状态值，可选值：正常运转/整改中/已撤销"));
            }
        }

        Organization org = Organization.builder()
                .name(dto.getName())
                .orgType(orgType)
                .jurisdiction(dto.getJurisdiction())
                .establishDate(dto.getEstablishDate())
                .leader(dto.getLeader())
                .contactPhone(dto.getContactPhone())
                .mediatorCount(dto.getMediatorCount() != null ? dto.getMediatorCount() : 0)
                .officeAddress(dto.getOfficeAddress())
                .status(status)
                .build();

        Organization saved = organizationRepository.save(org);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<Page<Organization>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orgType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String jurisdiction,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Organization> result;

        if (orgType != null && status != null) {
            OrgType ot = OrgType.valueOf(orgType);
            OrgStatus os = OrgStatus.valueOf(status);
            result = organizationRepository.findByOrgTypeAndStatus(ot, os, pageable);
        } else if (orgType != null) {
            OrgType ot = OrgType.valueOf(orgType);
            result = organizationRepository.findByOrgType(ot, pageable);
        } else if (status != null) {
            OrgStatus os = OrgStatus.valueOf(status);
            result = organizationRepository.findByStatus(os, pageable);
        } else if (jurisdiction != null) {
            result = organizationRepository.findByJurisdiction(jurisdiction, pageable);
        } else if (keyword != null) {
            result = organizationRepository.searchByKeyword(keyword, pageable);
        } else {
            result = organizationRepository.findAll(pageable);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<Organization> orgOpt = organizationRepository.findById(id);
        if (orgOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Organization org = orgOpt.get();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", org.getId());
        response.put("name", org.getName());
        response.put("orgType", org.getOrgType());
        response.put("jurisdiction", org.getJurisdiction());
        response.put("establishDate", org.getEstablishDate());
        response.put("leader", org.getLeader());
        response.put("contactPhone", org.getContactPhone());
        response.put("mediatorCount", org.getMediatorCount());
        response.put("officeAddress", org.getOfficeAddress());
        response.put("status", org.getStatus());
        response.put("createdAt", org.getCreatedAt());
        response.put("updatedAt", org.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody OrganizationDTO dto) {
        Optional<Organization> orgOpt = organizationRepository.findById(id);
        if (orgOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrgType orgType;
        try {
            orgType = OrgType.valueOf(dto.getOrgType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的组织类型，可选值：人民调解委员会/行业性调解组织/企事业单位调解组织"));
        }

        OrgStatus status = null;
        if (dto.getStatus() != null) {
            try {
                status = OrgStatus.valueOf(dto.getStatus());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的状态值，可选值：正常运转/整改中/已撤销"));
            }
        }

        Organization org = orgOpt.get();
        org.setName(dto.getName());
        org.setOrgType(orgType);
        org.setJurisdiction(dto.getJurisdiction());
        org.setEstablishDate(dto.getEstablishDate());
        org.setLeader(dto.getLeader());
        org.setContactPhone(dto.getContactPhone());
        if (dto.getMediatorCount() != null) {
            org.setMediatorCount(dto.getMediatorCount());
        }
        org.setOfficeAddress(dto.getOfficeAddress());
        if (status != null) {
            org.setStatus(status);
        }

        Organization saved = organizationRepository.save(org);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "状态不能为空"));
        }

        OrgStatus orgStatus;
        try {
            orgStatus = OrgStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的状态值，可选值：正常运转/整改中/已撤销"));
        }

        Optional<Organization> orgOpt = organizationRepository.findById(id);
        if (orgOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Organization org = orgOpt.get();
        org.setStatus(orgStatus);
        org.setMediatorCount((int) mediatorRepository.countByOrganizationId(id));
        organizationRepository.save(org);

        return ResponseEntity.ok(org);
    }
}

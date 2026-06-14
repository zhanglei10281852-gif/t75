package com.mediation.controller;

import com.mediation.dto.BadRecordDTO;
import com.mediation.entity.BadRecord;
import com.mediation.entity.BadRecord.RecordType;
import com.mediation.repository.BadRecordRepository;
import com.mediation.repository.MediatorRepository;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bad-records")
@RequiredArgsConstructor
public class BadRecordController {

    private final BadRecordRepository badRecordRepository;
    private final MediatorRepository mediatorRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody BadRecordDTO dto) {
        if (!mediatorRepository.existsById(dto.getMediatorId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "调解员不存在"));
        }

        RecordType recordType;
        try {
            recordType = RecordType.valueOf(dto.getRecordType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的记录类型，可选值：违纪违规/工作失误/群众投诉/其他"));
        }

        BadRecord record = BadRecord.builder()
                .mediatorId(dto.getMediatorId())
                .recordType(recordType)
                .description(dto.getDescription())
                .recordDate(dto.getRecordDate())
                .handlingResult(dto.getHandlingResult())
                .revoked(false)
                .build();

        BadRecord saved = badRecordRepository.save(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<Page<BadRecord>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long mediatorId,
            @RequestParam(required = false) Boolean revoked) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<BadRecord> result;
        if (mediatorId != null) {
            result = badRecordRepository.findByMediatorId(mediatorId, pageable);
        } else if (Boolean.TRUE.equals(revoked)) {
            result = badRecordRepository.findByRevokedTrue(pageable);
        } else if (Boolean.FALSE.equals(revoked)) {
            result = badRecordRepository.findByRevokedFalse(pageable);
        } else {
            result = badRecordRepository.findAll(pageable);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return badRecordRepository.findById(id)
                .map(record -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("id", record.getId());
                    response.put("mediatorId", record.getMediatorId());
                    response.put("recordType", record.getRecordType());
                    response.put("description", record.getDescription());
                    response.put("recordDate", record.getRecordDate());
                    response.put("handlingResult", record.getHandlingResult());
                    response.put("revoked", record.isRevoked());
                    response.put("revokeDate", record.getRevokeDate());
                    response.put("revokeReason", record.getRevokeReason());
                    response.put("createdAt", record.getCreatedAt());
                    response.put("updatedAt", record.getUpdatedAt());

                    mediatorRepository.findById(record.getMediatorId())
                            .ifPresent(m -> response.put("mediatorName", m.getName()));

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mediator/{mediatorId}/active")
    public ResponseEntity<?> getActiveByMediator(@PathVariable Long mediatorId) {
        if (!mediatorRepository.existsById(mediatorId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "调解员不存在"));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activeCount", badRecordRepository.countActiveByMediatorId(mediatorId));
        result.put("records", badRecordRepository.findActiveByMediatorId(mediatorId));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/revoke")
    public ResponseEntity<?> revoke(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String revokeReason = body.get("revokeReason");
        if (revokeReason == null || revokeReason.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "撤销原因不能为空"));
        }

        Optional<BadRecord> recordOpt = badRecordRepository.findById(id);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BadRecord record = recordOpt.get();
        if (record.isRevoked()) {
            return ResponseEntity.badRequest().body(Map.of("error", "该记录已撤销"));
        }

        record.setRevoked(true);
        record.setRevokeDate(LocalDate.now());
        record.setRevokeReason(revokeReason);
        badRecordRepository.save(record);

        return ResponseEntity.ok(record);
    }
}

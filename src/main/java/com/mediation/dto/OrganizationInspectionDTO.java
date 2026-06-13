package com.mediation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OrganizationInspectionDTO {

    @NotNull(message = "组织ID不能为空")
    private Long organizationId;

    @NotNull(message = "年度不能为空")
    private Integer year;

    @NotNull(message = "检查日期不能为空")
    private LocalDate inspectionDate;

    @NotBlank(message = "检查人不能为空")
    private String inspector;

    @NotBlank(message = "检查结论不能为空")
    private String conclusion;

    private String problems;

    private String rectificationRequirements;

    private LocalDate rectificationDeadline;
}

package com.mediation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BadRecordDTO {

    @NotNull(message = "调解员ID不能为空")
    private Long mediatorId;

    @NotBlank(message = "记录类型不能为空")
    private String recordType;

    @NotBlank(message = "不良记录描述不能为空")
    private String description;

    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    private String handlingResult;
}

package com.mediation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainingPlanDTO {

    @NotBlank(message = "培训主题不能为空")
    private String topic;

    @NotBlank(message = "培训类型不能为空")
    private String trainingType;

    @NotNull(message = "培训时间不能为空")
    private LocalDateTime trainingTime;

    @NotBlank(message = "培训地点不能为空")
    private String location;

    private String instructorInfo;

    private Integer plannedCount;

    @NotNull(message = "培训学时不能为空")
    private Integer hours;
}

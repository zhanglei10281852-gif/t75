package com.mediation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrainingAssessmentDTO {

    @NotNull(message = "培训ID不能为空")
    private Long trainingId;

    @NotNull(message = "调解员ID不能为空")
    private Long mediatorId;

    @NotNull(message = "考核成绩不能为空")
    @Min(value = 0, message = "成绩不能小于0")
    @Max(value = 100, message = "成绩不能大于100")
    private Integer score;
}

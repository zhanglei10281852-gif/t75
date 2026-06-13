package com.mediation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrainingRegistrationDTO {

    @NotNull(message = "培训ID不能为空")
    private Long trainingId;

    @NotNull(message = "调解员ID不能为空")
    private Long mediatorId;
}

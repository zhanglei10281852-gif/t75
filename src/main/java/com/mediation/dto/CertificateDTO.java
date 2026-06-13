package com.mediation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CertificateDTO {

    @NotNull(message = "调解员ID不能为空")
    private Long mediatorId;

    @NotBlank(message = "证书编号不能为空")
    private String certNo;

    @NotNull(message = "发证日期不能为空")
    private LocalDate issueDate;

    private Integer validYears;
}

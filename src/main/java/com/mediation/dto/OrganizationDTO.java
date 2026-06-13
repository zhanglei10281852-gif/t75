package com.mediation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OrganizationDTO {

    @NotBlank(message = "组织名称不能为空")
    private String name;

    @NotBlank(message = "组织类型不能为空")
    private String orgType;

    @NotBlank(message = "所在辖区不能为空")
    private String jurisdiction;

    @NotNull(message = "成立日期不能为空")
    private LocalDate establishDate;

    @NotBlank(message = "负责人不能为空")
    private String leader;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private Integer mediatorCount;

    private String officeAddress;

    private String status;
}

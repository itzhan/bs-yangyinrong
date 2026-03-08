package com.hotpot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MemberLevelDTO {
    @NotBlank(message = "等级名称不能为空")
    private String name;
    @NotNull(message = "最低积分不能为空")
    private Integer minPoints;
    @NotNull(message = "折扣不能为空")
    private BigDecimal discount;
    private String description;
}

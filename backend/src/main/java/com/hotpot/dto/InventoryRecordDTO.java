package com.hotpot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InventoryRecordDTO {
    @NotNull(message = "食材不能为空")
    private Long ingredientId;
    @NotNull(message = "操作类型不能为空")
    private Integer type;
    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String remark;
}

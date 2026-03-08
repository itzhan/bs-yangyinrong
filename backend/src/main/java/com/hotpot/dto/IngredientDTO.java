package com.hotpot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class IngredientDTO {
    @NotBlank(message = "食材名称不能为空")
    private String name;
    @NotBlank(message = "单位不能为空")
    private String unit;
    private BigDecimal stockQuantity;
    private BigDecimal warningQuantity;
    private String category;
    private BigDecimal price;
    private String supplier;
    private Integer status;
}

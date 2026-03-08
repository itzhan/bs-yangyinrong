package com.hotpot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiningTableDTO {
    @NotBlank(message = "桌号不能为空")
    private String tableNumber;
    @NotNull(message = "容纳人数不能为空")
    private Integer capacity;
    private Integer status;
    private String area;
}

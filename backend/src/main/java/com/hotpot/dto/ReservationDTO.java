package com.hotpot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationDTO {
    @NotNull(message = "桌台不能为空")
    private Long tableId;
    @NotBlank(message = "预订人姓名不能为空")
    private String customerName;
    @NotBlank(message = "预订人电话不能为空")
    private String customerPhone;
    @NotNull(message = "预订时间不能为空")
    private LocalDateTime reservationTime;
    @NotNull(message = "就餐人数不能为空")
    private Integer guestCount;
    private String remark;
}

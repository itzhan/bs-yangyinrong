package com.hotpot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentDTO {
    @NotNull(message = "订单不能为空")
    private Long orderId;
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;
}

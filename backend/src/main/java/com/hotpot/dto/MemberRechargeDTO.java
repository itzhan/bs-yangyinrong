package com.hotpot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MemberRechargeDTO {
    @NotNull(message = "会员不能为空")
    private Long memberId;
    @NotNull(message = "充值金额不能为空")
    private BigDecimal amount;
    private BigDecimal giftAmount;
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;
}

package com.hotpot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("member_recharge")
public class MemberRecharge {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private BigDecimal amount;
    private BigDecimal giftAmount;
    private BigDecimal balanceAfter;
    private Integer paymentMethod;
    private Long operatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String memberName;
    @TableField(exist = false)
    private String operatorName;
}

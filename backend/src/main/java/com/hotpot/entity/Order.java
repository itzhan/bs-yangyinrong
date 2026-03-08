package com.hotpot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long tableId;
    private Long memberId;
    private Integer orderStatus;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal actualAmount;
    private Integer guestCount;
    private String remark;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String tableNumber;
    @TableField(exist = false)
    private String memberName;
    @TableField(exist = false)
    private String createdByName;
    @TableField(exist = false)
    private List<OrderItem> items;
}

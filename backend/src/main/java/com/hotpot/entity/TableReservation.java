package com.hotpot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("table_reservation")
public class TableReservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tableId;
    private Long memberId;
    private String customerName;
    private String customerPhone;
    private LocalDateTime reservationTime;
    private Integer guestCount;
    private Integer status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String tableNumber;
}

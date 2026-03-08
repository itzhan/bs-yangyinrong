package com.hotpot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ingredient")
public class Ingredient {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String unit;
    private BigDecimal stockQuantity;
    private BigDecimal warningQuantity;
    private String category;
    private BigDecimal price;
    private String supplier;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}

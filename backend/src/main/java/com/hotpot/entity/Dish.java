package com.hotpot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private Integer spicyLevel;
    private Integer isRecommended;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    /** 非数据库字段：分类名称 */
    @TableField(exist = false)
    private String categoryName;
}

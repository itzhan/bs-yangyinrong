package com.hotpot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("dish_ingredient")
public class DishIngredient {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dishId;
    private Long ingredientId;
    private BigDecimal consumptionAmount;

    @TableField(exist = false)
    private String ingredientName;
    @TableField(exist = false)
    private String unit;
}

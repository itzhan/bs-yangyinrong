package com.hotpot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderDTO {
    @NotNull(message = "桌台不能为空")
    private Long tableId;
    private Long memberId;
    @NotNull(message = "就餐人数不能为空")
    private Integer guestCount;
    private String remark;
    @NotNull(message = "菜品不能为空")
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        @NotNull(message = "菜品不能为空")
        private Long dishId;
        @NotNull(message = "数量不能为空")
        private Integer quantity;
        private String remark;
    }
}

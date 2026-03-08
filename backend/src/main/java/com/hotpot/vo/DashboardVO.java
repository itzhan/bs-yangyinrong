package com.hotpot.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    /** 今日营收 */
    private BigDecimal todayRevenue;
    /** 今日订单数 */
    private Long todayOrders;
    /** 会员总数 */
    private Long totalMembers;
    /** 低库存预警数 */
    private Long lowStockCount;
    /** 桌台使用情况: 空闲/占用/预订 数量 */
    private Map<String, Long> tableStatus;
    /** 最近7天营收趋势 */
    private List<Map<String, Object>> revenueTrend;
    /** 菜品销量排行TOP10 */
    private List<Map<String, Object>> dishSalesTop;
}

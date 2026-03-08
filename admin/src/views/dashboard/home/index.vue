<template>
  <div class="p-4 space-y-4">
    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #ff6b6b, #ee5a24)">
            <el-icon :size="28"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ dashboard?.todayRevenue?.toFixed(2) || '0.00' }}</div>
            <div class="stat-label">今日营收</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #1dd1a1, #10ac84)">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard?.todayOrders || 0 }}</div>
            <div class="stat-label">今日订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #54a0ff, #2e86de)">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard?.totalMembers || 0 }}</div>
            <div class="stat-label">会员总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: (dashboard?.lowStockCount || 0) > 0 ? 'linear-gradient(135deg, #ff6348, #c0392b)' : 'linear-gradient(135deg, #a29bfe, #6c5ce7)' }">
            <el-icon :size="28"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard?.lowStockCount || 0 }}</div>
            <div class="stat-label">库存预警</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 桌台状态 & 营收趋势 -->
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span class="card-title">桌台实时状态</span></template>
          <div class="table-status">
            <div class="status-item">
              <div class="status-dot" style="background: #52c41a"></div>
              <span>空闲</span>
              <span class="status-count">{{ dashboard?.tableStatus?.free || 0 }} 桌</span>
            </div>
            <div class="status-item">
              <div class="status-dot" style="background: #ff4d4f"></div>
              <span>占用</span>
              <span class="status-count">{{ dashboard?.tableStatus?.occupied || 0 }} 桌</span>
            </div>
            <div class="status-item">
              <div class="status-dot" style="background: #faad14"></div>
              <span>预订</span>
              <span class="status-count">{{ dashboard?.tableStatus?.reserved || 0 }} 桌</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span class="card-title">近7天营收趋势</span></template>
          <div ref="revenueChartRef" style="height: 280px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 菜品销量TOP10 -->
    <el-card shadow="hover">
      <template #header><span class="card-title">菜品销量排行 TOP10（近30天）</span></template>
      <div ref="salesChartRef" style="height: 320px"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { Money, Document, User, Warning } from '@element-plus/icons-vue'
import { fetchDashboard } from '@/api/business'
import * as echarts from 'echarts'

defineOptions({ name: 'DashboardHome' })

const dashboard = ref<Api.Dashboard.Data>()
const revenueChartRef = ref<HTMLElement>()
const salesChartRef = ref<HTMLElement>()

const loadData = async () => {
  try {
    dashboard.value = await fetchDashboard()
    await nextTick()
    renderRevenueChart()
    renderSalesChart()
  } catch (e) {
    console.error(e)
  }
}

const renderRevenueChart = () => {
  if (!revenueChartRef.value || !dashboard.value?.revenueTrend) return
  const chart = echarts.init(revenueChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>营收: ¥{c}' },
    grid: { left: 60, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: dashboard.value.revenueTrend.map(i => i.date.slice(5))
    },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [{
      type: 'line',
      data: dashboard.value.revenueTrend.map(i => i.revenue),
      smooth: true,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(255,107,107,0.3)' },
        { offset: 1, color: 'rgba(255,107,107,0.05)' }
      ])},
      lineStyle: { color: '#ff6b6b', width: 2 },
      itemStyle: { color: '#ff6b6b' }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

const renderSalesChart = () => {
  if (!salesChartRef.value || !dashboard.value?.dishSalesTop) return
  const chart = echarts.init(salesChartRef.value)
  const data = [...dashboard.value.dishSalesTop].reverse()
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 120, right: 40, top: 10, bottom: 20 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: data.map(i => i.name) },
    series: [{
      type: 'bar',
      data: data.map(i => i.sales),
      barWidth: 20,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#ff6b6b' },
          { offset: 1, color: '#ee5a24' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right', formatter: '{c}份' }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

onMounted(() => loadData())
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 0;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}
.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  color: #fff;
  flex-shrink: 0;
}
.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--el-text-color-primary); }
.stat-label { font-size: 13px; color: var(--el-text-color-secondary); margin-top: 4px; }
.card-title { font-weight: 600; font-size: 15px; }
.table-status { display: flex; flex-direction: column; gap: 20px; padding: 12px 0; }
.status-item { display: flex; align-items: center; gap: 10px; font-size: 15px; }
.status-dot { width: 12px; height: 12px; border-radius: 50%; }
.status-count { margin-left: auto; font-weight: 600; font-size: 18px; }
</style>

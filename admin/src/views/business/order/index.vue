<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="桌号">
          <el-input v-model="searchForm.tableNumber" placeholder="请输入桌号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 状态标签页 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
        </div>
      </template>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待确认" name="0" />
        <el-tab-pane label="制作中" name="1" />
        <el-tab-pane label="已完成" name="2" />
        <el-tab-pane label="已结算" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="tableNumber" label="桌号" width="80" />
        <el-table-column prop="memberName" label="会员" width="100">
          <template #default="{ row }">{{ row.memberName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="guestCount" label="人数" width="70" />
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="优惠" width="90">
          <template #default="{ row }">¥{{ row.discountAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实付" width="100">
          <template #default="{ row }">
            <span style="color:#f56c6c;font-weight:bold">¥{{ row.actualAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.orderStatus)">{{ statusLabel(row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.orderStatus === 0" type="success" link @click="handleConfirm(row.id)">确认</el-button>
            <el-button v-if="row.orderStatus === 1" type="warning" link @click="handleComplete(row.id)">完成</el-button>
            <el-popconfirm v-if="row.orderStatus <= 1" title="确认取消该订单？" @confirm="handleCancel(row.id)">
              <template #reference>
                <el-button type="danger" link>取消</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @change="loadData"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="桌号">{{ currentOrder.tableNumber }}</el-descriptions-item>
        <el-descriptions-item label="会员">{{ currentOrder.memberName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="人数">{{ currentOrder.guestCount }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">¥{{ currentOrder.discountAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ currentOrder.actualAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentOrder.orderStatus)">{{ statusLabel(currentOrder.orderStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createdAt }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin:16px 0 8px">菜品明细</h4>
      <el-table :data="currentOrder.items || []" border size="small">
        <el-table-column prop="dishName" label="菜品" />
        <el-table-column prop="price" label="单价" width="90">
          <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column prop="subtotal" label="小计" width="100">
          <template #default="{ row }">¥{{ row.subtotal?.toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { fetchOrderList, fetchOrderDetail, confirmOrder, completeOrder, cancelOrder } from '@/api/business'

const loading = ref(false)
const detailVisible = ref(false)
const activeTab = ref('')

const tableData = ref<any[]>([])
const currentOrder = ref<any>({})
const searchForm = reactive({ orderNo: '', tableNumber: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

function statusLabel(s: number) { return ['待确认', '制作中', '已完成', '已结算', '已取消'][s] || '未知' }
function statusTagType(s: number) { return (['warning', 'primary', 'success', 'info', 'danger'] as const)[s] || 'info' }

async function loadData() {
  loading.value = true
  try {
    const params: any = { ...searchForm, page: pagination.page, size: pagination.size }
    if (activeTab.value !== '') params.orderStatus = Number(activeTab.value)
    const { records, total } = await fetchOrderList(params)
    tableData.value = records
    pagination.total = total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.orderNo = ''
  searchForm.tableNumber = ''
  pagination.page = 1
  loadData()
}

function handleTabChange() {
  pagination.page = 1
  loadData()
}

async function viewDetail(row: any) {
  const data = await fetchOrderDetail(row.id)
  currentOrder.value = data
  detailVisible.value = true
}

async function handleConfirm(id: number) {
  await confirmOrder(id)
  loadData()
}

async function handleComplete(id: number) {
  await completeOrder(id)
  loadData()
}

async function handleCancel(id: number) {
  await cancelOrder(id)
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

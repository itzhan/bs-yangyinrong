<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="支付单号">
          <el-input v-model="searchForm.paymentNo" placeholder="请输入支付单号" clearable />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="全部方式" clearable>
            <el-option label="现金" :value="0" />
            <el-option label="微信" :value="1" />
            <el-option label="支付宝" :value="2" />
            <el-option label="银行卡" :value="3" />
            <el-option label="会员余额" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已退款" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>收银记录</span>
          <el-button type="primary" @click="openDialog()">新增收款</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="paymentNo" label="支付单号" width="180" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">{{ methodLabel(row.paymentMethod) }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="110">
          <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cashierName" label="收银员" width="100" />
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm v-if="row.status === 1" title="确认退款？" @confirm="handleRefund(row.id)">
              <template #reference>
                <el-button type="danger" link>退款</el-button>
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

    <!-- 新增收款对话框 -->
    <el-dialog v-model="dialogVisible" title="新增收款" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联订单" prop="orderId">
          <el-select v-model="form.orderId" filterable placeholder="请选择订单" style="width:100%">
            <el-option v-for="o in orderList" :key="o.id" :label="`${o.orderNo}（¥${o.actualAmount?.toFixed(2)}）`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="form.paymentMethod" placeholder="请选择支付方式">
            <el-option label="现金" :value="0" />
            <el-option label="微信" :value="1" />
            <el-option label="支付宝" :value="2" />
            <el-option label="银行卡" :value="3" />
            <el-option label="会员余额" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { fetchPaymentList, createPayment, refundPayment, fetchOrderList } from '@/api/business'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const tableData = ref<any[]>([])
const orderList = ref<any[]>([])
const searchForm = reactive({
  paymentNo: '',
  orderNo: '',
  paymentMethod: undefined as number | undefined,
  status: undefined as number | undefined
})
const pagination = reactive({ page: 1, size: 10, total: 0 })

const form = ref({
  orderId: undefined as number | undefined,
  paymentMethod: undefined as number | undefined,
  amount: 0
})

const rules: FormRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

function methodLabel(m: number) { return ['现金', '微信', '支付宝', '银行卡', '会员余额'][m] || '未知' }
function statusLabel(s: number) { return ['待支付', '已支付', '已退款'][s] || '未知' }
function statusTagType(s: number) { return (['warning', 'success', 'danger'] as const)[s] || 'info' }

async function loadData() {
  loading.value = true
  try {
    const { records, total } = await fetchPaymentList({ ...searchForm, page: pagination.page, size: pagination.size })
    tableData.value = records
    pagination.total = total
  } finally {
    loading.value = false
  }
}

async function loadOrders() {
  const { records } = await fetchOrderList({ orderStatus: 2, page: 1, size: 100 })
  orderList.value = records
}

function resetSearch() {
  searchForm.paymentNo = ''
  searchForm.orderNo = ''
  searchForm.paymentMethod = undefined
  searchForm.status = undefined
  pagination.page = 1
  loadData()
}

function openDialog() {
  form.value = { orderId: undefined, paymentMethod: undefined, amount: 0 }
  loadOrders()
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await createPayment(form.value)
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleRefund(id: number) {
  await refundPayment(id)
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

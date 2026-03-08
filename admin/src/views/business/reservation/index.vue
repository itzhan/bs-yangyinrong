<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.customerPhone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="待到店" :value="0" />
            <el-option label="已到店" :value="1" />
            <el-option label="已取消" :value="2" />
            <el-option label="未到" :value="3" />
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
          <span>预订列表</span>
          <el-button type="primary" @click="openDialog()">新增预订</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="tableNumber" label="桌号" width="80" />
        <el-table-column prop="customerName" label="客户姓名" width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="130" />
        <el-table-column prop="reservationTime" label="预订时间" width="170" />
        <el-table-column prop="guestCount" label="人数" width="70" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link @click="handleArrive(row.id)">到店</el-button>
            <el-popconfirm v-if="row.status === 0" title="确认取消该预订？" @confirm="handleCancel(row.id)">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑预订' : '新增预订'" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="桌台" prop="tableId">
          <el-select v-model="form.tableId" placeholder="请选择桌台">
            <el-option v-for="t in tableList" :key="t.id" :label="`${t.tableNumber}（${t.capacity}人）`" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="customerPhone">
          <el-input v-model="form.customerPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="预订时间" prop="reservationTime">
          <el-date-picker v-model="form.reservationTime" type="datetime" placeholder="选择日期时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="用餐人数" prop="guestCount">
          <el-input-number v-model="form.guestCount" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { fetchReservationList, createReservation, updateReservation, cancelReservation, arriveReservation, fetchTableList } from '@/api/business'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(0)
const formRef = ref<FormInstance>()

const tableList = ref<any[]>([])
const tableData = ref<any[]>([])
const searchForm = reactive({ customerName: '', customerPhone: '', status: undefined as number | undefined })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const form = ref({
  tableId: undefined as number | undefined,
  customerName: '',
  customerPhone: '',
  reservationTime: '',
  guestCount: 2,
  remark: ''
})

const rules: FormRules = {
  tableId: [{ required: true, message: '请选择桌台', trigger: 'change' }],
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  customerPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  reservationTime: [{ required: true, message: '请选择预订时间', trigger: 'change' }],
  guestCount: [{ required: true, message: '请输入用餐人数', trigger: 'blur' }]
}

function statusLabel(s: number) { return ['待到店', '已到店', '已取消', '未到'][s] || '未知' }
function statusTagType(s: number) { return (['warning', 'success', 'info', 'danger'] as const)[s] || 'info' }

async function loadData() {
  loading.value = true
  try {
    const { records, total } = await fetchReservationList({ ...searchForm, page: pagination.page, size: pagination.size })
    tableData.value = records
    pagination.total = total
  } finally {
    loading.value = false
  }
}

async function loadTables() {
  tableList.value = await fetchTableList()
}

function resetSearch() {
  searchForm.customerName = ''
  searchForm.customerPhone = ''
  searchForm.status = undefined
  pagination.page = 1
  loadData()
}

function openDialog(row?: any) {
  isEdit.value = !!row
  editId.value = row?.id || 0
  form.value = row
    ? { tableId: row.tableId, customerName: row.customerName, customerPhone: row.customerPhone, reservationTime: row.reservationTime, guestCount: row.guestCount, remark: row.remark || '' }
    : { tableId: undefined, customerName: '', customerPhone: '', reservationTime: '', guestCount: 2, remark: '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateReservation(editId.value, form.value)
    } else {
      await createReservation(form.value)
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleArrive(id: number) {
  await arriveReservation(id)
  loadData()
}

async function handleCancel(id: number) {
  await cancelReservation(id)
  loadData()
}

onMounted(() => {
  loadData()
  loadTables()
})
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

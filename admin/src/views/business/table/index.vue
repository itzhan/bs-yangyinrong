<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="桌号">
          <el-input v-model="searchForm.tableNumber" placeholder="请输入桌号" clearable />
        </el-form-item>
        <el-form-item label="区域">
          <el-input v-model="searchForm.area" placeholder="请输入区域" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="空闲" :value="0" />
            <el-option label="占用" :value="1" />
            <el-option label="预订" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>桌台管理（共 {{ filteredList.length }} 张）</span>
          <el-button type="primary" @click="openDialog()">新增桌台</el-button>
        </div>
      </template>

      <!-- 卡片视图 -->
      <el-row :gutter="16" v-loading="loading">
        <el-col v-for="item in filteredList" :key="item.id" :xs="12" :sm="8" :md="6" :lg="4" style="margin-bottom:16px">
          <el-card :class="['table-item', statusClass(item.status)]" shadow="hover">
            <div class="table-number">{{ item.tableNumber }}</div>
            <div class="table-info">
              <span>容纳：{{ item.capacity }}人</span>
              <span>区域：{{ item.area || '-' }}</span>
            </div>
            <el-tag :type="statusTagType(item.status)" class="table-status">
              {{ statusLabel(item.status) }}
            </el-tag>
            <div class="table-actions">
              <el-button type="primary" link size="small" @click="openDialog(item)">编辑</el-button>
              <el-dropdown @command="(cmd: number) => handleStatusChange(item.id, cmd)">
                <el-button type="warning" link size="small">状态<el-icon class="el-icon--right"><arrow-down /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="0">空闲</el-dropdown-item>
                    <el-dropdown-item :command="1">占用</el-dropdown-item>
                    <el-dropdown-item :command="2">预订</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-popconfirm title="确认删除？" @confirm="handleDelete(item.id)">
                <template #reference>
                  <el-button type="danger" link size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑桌台' : '新增桌台'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="桌号" prop="tableNumber">
          <el-input v-model="form.tableNumber" placeholder="如：A01" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="区域">
          <el-input v-model="form.area" placeholder="如：大厅、包间" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">空闲</el-radio>
            <el-radio :value="1">占用</el-radio>
            <el-radio :value="2">预订</el-radio>
          </el-radio-group>
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
import { ref, computed, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { fetchTableList, createTable, updateTable, deleteTable, updateTableStatus } from '@/api/business'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(0)
const formRef = ref<FormInstance>()

const searchForm = ref({ tableNumber: '', area: '', status: undefined as number | undefined })
const tableData = ref<any[]>([])

const form = ref({ tableNumber: '', capacity: 4, area: '', status: 0 })

const rules: FormRules = {
  tableNumber: [{ required: true, message: '请输入桌号', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }]
}

const filteredList = computed(() => {
  return tableData.value.filter(item => {
    if (searchForm.value.tableNumber && !item.tableNumber.includes(searchForm.value.tableNumber)) return false
    if (searchForm.value.area && !item.area?.includes(searchForm.value.area)) return false
    if (searchForm.value.status !== undefined && searchForm.value.status !== null && item.status !== searchForm.value.status) return false
    return true
  })
})

function statusLabel(s: number) { return ['空闲', '占用', '预订'][s] || '未知' }
function statusTagType(s: number) { return (['success', 'danger', 'warning'] as const)[s] || 'info' }
function statusClass(s: number) { return ['status-free', 'status-occupied', 'status-reserved'][s] || '' }

async function loadData() {
  loading.value = true
  try {
    tableData.value = await fetchTableList()
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.value = { tableNumber: '', area: '', status: undefined }
  loadData()
}

function openDialog(row?: any) {
  isEdit.value = !!row
  editId.value = row?.id || 0
  form.value = row
    ? { tableNumber: row.tableNumber, capacity: row.capacity, area: row.area || '', status: row.status }
    : { tableNumber: '', capacity: 4, area: '', status: 0 }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateTable(editId.value, form.value)
    } else {
      await createTable(form.value)
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(id: number, status: number) {
  await updateTableStatus(id, status)
  loadData()
}

async function handleDelete(id: number) {
  await deleteTable(id)
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.table-item { text-align: center; position: relative; }
.table-item .table-number { font-size: 24px; font-weight: bold; margin-bottom: 8px; }
.table-item .table-info { font-size: 13px; color: #999; margin-bottom: 8px; display: flex; justify-content: space-around; }
.table-item .table-status { margin-bottom: 8px; }
.table-item .table-actions { display: flex; justify-content: center; gap: 4px; }
.status-free { border-top: 3px solid #67c23a; }
.status-occupied { border-top: 3px solid #f56c6c; }
.status-reserved { border-top: 3px solid #e6a23c; }
</style>

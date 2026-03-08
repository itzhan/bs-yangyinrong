<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="食材名称">
          <el-input v-model="searchForm.name" placeholder="请输入食材名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="searchForm.category" placeholder="请输入分类" clearable />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="searchForm.supplier" placeholder="请输入供应商" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="warning" @click="loadWarning">库存预警</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 低库存预警提示 -->
    <el-alert v-if="warningList.length" type="warning" :closable="false" style="margin-bottom:16px">
      <template #title>
        <span>低库存预警（{{ warningList.length }}项）：</span>
        <el-tag v-for="w in warningList" :key="w.id" type="danger" size="small" style="margin-left:6px">
          {{ w.name }}（剩{{ w.stockQuantity }}{{ w.unit }}）
        </el-tag>
      </template>
    </el-alert>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>食材列表</span>
          <el-button type="primary" @click="openDialog()">新增食材</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="110" />
        <el-table-column prop="category" label="分类" width="90" />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="stockQuantity" label="库存" width="90">
          <template #default="{ row }">
            <el-badge v-if="row.stockQuantity <= row.warningQuantity" is-dot class="low-stock">
              <span style="color:#f56c6c;font-weight:bold">{{ row.stockQuantity }}</span>
            </el-badge>
            <span v-else>{{ row.stockQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningQuantity" label="预警值" width="80" />
        <el-table-column prop="price" label="单价" width="90">
          <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="success" link @click="openOperateDialog(row)">出入库</el-button>
            <el-button type="info" link @click="viewRecords(row)">记录</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑食材' : '新增食材'" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入食材名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如：肉类、蔬菜" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：kg、份、瓶" />
        </el-form-item>
        <el-form-item label="预警值" prop="warningQuantity">
          <el-input-number v-model="form.warningQuantity" :min="0" />
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="form.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 出入库操作对话框 -->
    <el-dialog v-model="operateDialogVisible" title="库存操作" width="500px" destroy-on-close>
      <el-form ref="operateFormRef" :model="operateForm" :rules="operateRules" label-width="80px">
        <el-form-item label="食材">
          <el-input :model-value="operateTarget" disabled />
        </el-form-item>
        <el-form-item label="操作类型" prop="type">
          <el-radio-group v-model="operateForm.type">
            <el-radio :value="0">入库</el-radio>
            <el-radio :value="1">出库</el-radio>
            <el-radio :value="2">盘点</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="operateForm.quantity" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item v-if="operateForm.type === 0" label="单价">
          <el-input-number v-model="operateForm.unitPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="operateForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="operateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleOperate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 库存记录对话框 -->
    <el-dialog v-model="recordDialogVisible" title="库存操作记录" width="700px" destroy-on-close>
      <el-table :data="recordData" v-loading="recordLoading" border size="small">
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="(['success', 'danger', 'warning'] as const)[row.type]">{{ ['入库', '出库', '盘点'][row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="unitPrice" label="单价" width="90">
          <template #default="{ row }">{{ row.unitPrice ? `¥${row.unitPrice.toFixed(2)}` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="170" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  fetchIngredientList, createIngredient, updateIngredient, deleteIngredient,
  fetchLowStockWarning, operateInventory, fetchInventoryRecords
} from '@/api/business'

const loading = ref(false)
const submitting = ref(false)
const recordLoading = ref(false)
const dialogVisible = ref(false)
const operateDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(0)
const formRef = ref<FormInstance>()
const operateFormRef = ref<FormInstance>()

const tableData = ref<any[]>([])
const warningList = ref<any[]>([])
const recordData = ref<any[]>([])
const searchForm = reactive({ name: '', category: '', supplier: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const form = ref({ name: '', category: '', unit: '', warningQuantity: 10, price: 0, supplier: '', status: 1 })
const rules: FormRules = {
  name: [{ required: true, message: '请输入食材名称', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }]
}

const operateForm = ref({ ingredientId: 0, type: 0, quantity: 0, unitPrice: 0, remark: '' })
const operateTarget = ref('')
const operateRules: FormRules = {
  type: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const { records, total } = await fetchIngredientList({ ...searchForm, page: pagination.page, size: pagination.size })
    tableData.value = records
    pagination.total = total
  } finally {
    loading.value = false
  }
}

async function loadWarning() {
  warningList.value = await fetchLowStockWarning()
}

function resetSearch() {
  searchForm.name = ''
  searchForm.category = ''
  searchForm.supplier = ''
  pagination.page = 1
  loadData()
}

function openDialog(row?: any) {
  isEdit.value = !!row
  editId.value = row?.id || 0
  form.value = row
    ? { name: row.name, category: row.category || '', unit: row.unit, warningQuantity: row.warningQuantity, price: row.price, supplier: row.supplier || '', status: row.status }
    : { name: '', category: '', unit: '', warningQuantity: 10, price: 0, supplier: '', status: 1 }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateIngredient(editId.value, form.value)
    } else {
      await createIngredient(form.value)
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  await deleteIngredient(id)
  loadData()
}

function openOperateDialog(row: any) {
  operateForm.value = { ingredientId: row.id, type: 0, quantity: 0, unitPrice: 0, remark: '' }
  operateTarget.value = `${row.name}（当前库存：${row.stockQuantity}${row.unit}）`
  operateDialogVisible.value = true
}

async function handleOperate() {
  await operateFormRef.value?.validate()
  submitting.value = true
  try {
    await operateInventory(operateForm.value)
    operateDialogVisible.value = false
    loadData()
    loadWarning()
  } finally {
    submitting.value = false
  }
}

async function viewRecords(row: any) {
  recordDialogVisible.value = true
  recordLoading.value = true
  try {
    const { records } = await fetchInventoryRecords({ ingredientId: row.id, page: 1, size: 50 })
    recordData.value = records
  } finally {
    recordLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadWarning()
})
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.low-stock { margin-right: 8px; }
</style>

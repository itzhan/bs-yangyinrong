<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.name" placeholder="请输入分类名称" clearable @clear="loadData" />
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
          <span>菜品分类列表</span>
          <el-button type="primary" @click="openDialog()">新增分类</el-button>
        </div>
      </template>
      <el-table :data="filteredList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除该分类？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
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
import { fetchCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/business'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>(0)
const formRef = ref<FormInstance>()

const searchForm = ref({ name: '' })
const tableData = ref<any[]>([])

const form = ref({
  name: '',
  sortOrder: 0,
  status: 1
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const filteredList = computed(() => {
  if (!searchForm.value.name) return tableData.value
  return tableData.value.filter(item =>
    item.name.includes(searchForm.value.name)
  )
})

async function loadData() {
  loading.value = true
  try {
    tableData.value = await fetchCategoryList()
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.value.name = ''
  loadData()
}

function openDialog(row?: any) {
  isEdit.value = !!row
  editId.value = row?.id || 0
  form.value = row
    ? { name: row.name, sortOrder: row.sortOrder, status: row.status }
    : { name: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateCategory(editId.value, form.value)
    } else {
      await createCategory(form.value)
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  await deleteCategory(id)
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

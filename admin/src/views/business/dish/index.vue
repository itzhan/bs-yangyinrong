<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="菜品名称">
          <el-input v-model="searchForm.name" placeholder="请输入菜品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable>
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="下架" :value="0" />
            <el-option label="在售" :value="1" />
            <el-option label="售罄" :value="2" />
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
          <span>菜品列表</span>
          <el-button type="primary" @click="openDialog()">新增菜品</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="菜品名称" min-width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="price" label="价格(元)" width="100">
          <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="image" label="图片" width="80">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" style="width:40px;height:40px" fit="cover" :preview-src-list="[row.image]" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="spicyLevel" label="辣度" width="80">
          <template #default="{ row }">
            {{ ['不辣', '微辣', '中辣', '特辣'][row.spicyLevel] || '不辣' }}
          </template>
        </el-table-column>
        <el-table-column prop="isRecommended" label="推荐" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.isRecommended" type="warning" size="small">推荐</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status !== 1" type="success" link @click="handleStatus(row.id, 1)">上架</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleStatus(row.id, 0)">下架</el-button>
            <el-popconfirm title="确认删除该菜品？" @confirm="handleDelete(row.id)">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜品' : '新增菜品'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="图片" prop="image">
          <el-input v-model="form.image" placeholder="请输入图片地址" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入菜品描述" />
        </el-form-item>
        <el-form-item label="辣度">
          <el-radio-group v-model="form.spicyLevel">
            <el-radio :value="0">不辣</el-radio>
            <el-radio :value="1">微辣</el-radio>
            <el-radio :value="2">中辣</el-radio>
            <el-radio :value="3">特辣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="推荐">
          <el-switch v-model="form.isRecommended" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">下架</el-radio>
            <el-radio :value="1">在售</el-radio>
            <el-radio :value="2">售罄</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
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
import { fetchDishList, createDish, updateDish, deleteDish, updateDishStatus, fetchCategoryList } from '@/api/business'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(0)
const formRef = ref<FormInstance>()

const categoryList = ref<any[]>([])
const tableData = ref<any[]>([])
const searchForm = reactive({ name: '', categoryId: undefined as number | undefined, status: undefined as number | undefined })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const form = ref({
  name: '',
  categoryId: undefined as number | undefined,
  price: 0,
  image: '',
  description: '',
  spicyLevel: 0,
  isRecommended: 0,
  status: 1,
  sortOrder: 0
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

function statusLabel(s: number) {
  return ['下架', '在售', '售罄'][s] || '未知'
}
function statusTagType(s: number) {
  return (['info', 'success', 'warning'] as const)[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const { records, total } = await fetchDishList({ ...searchForm, page: pagination.page, size: pagination.size })
    tableData.value = records
    pagination.total = total
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  categoryList.value = await fetchCategoryList()
}

function resetSearch() {
  searchForm.name = ''
  searchForm.categoryId = undefined
  searchForm.status = undefined
  pagination.page = 1
  loadData()
}

function openDialog(row?: any) {
  isEdit.value = !!row
  editId.value = row?.id || 0
  form.value = row
    ? { name: row.name, categoryId: row.categoryId, price: row.price, image: row.image || '', description: row.description || '', spicyLevel: row.spicyLevel || 0, isRecommended: row.isRecommended || 0, status: row.status, sortOrder: row.sortOrder || 0 }
    : { name: '', categoryId: undefined, price: 0, image: '', description: '', spicyLevel: 0, isRecommended: 0, status: 1, sortOrder: 0 }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateDish(editId.value, form.value)
    } else {
      await createDish(form.value)
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleStatus(id: number, status: number) {
  await updateDishStatus(id, status)
  loadData()
}

async function handleDelete(id: number) {
  await deleteDish(id)
  loadData()
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

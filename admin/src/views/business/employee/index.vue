<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部角色" clearable>
            <el-option label="管理员" value="ADMIN" />
            <el-option label="收银员" value="CASHIER" />
            <el-option label="厨房" value="KITCHEN" />
            <el-option label="库管" value="INVENTORY" />
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
          <span>员工列表</span>
          <el-button type="primary" @click="openDialog()">新增员工</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="warning" link @click="openResetPwdDialog(row)">重置密码</el-button>
            <el-popconfirm title="确认删除该员工？" @confirm="handleDelete(row.id)">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑员工' : '新增员工'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="收银员" value="CASHIER" />
            <el-option label="厨房" value="KITCHEN" />
            <el-option label="库管" value="INVENTORY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPwdVisible" title="重置密码" width="420px" destroy-on-close>
      <el-form ref="resetPwdFormRef" :model="resetPwdForm" :rules="resetPwdRules" label-width="80px">
        <el-form-item label="员工">
          <el-input :model-value="resetPwdTarget" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPwdForm.password" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { fetchEmployeeList, createEmployee, updateEmployee, deleteEmployee, resetEmployeePassword } from '@/api/business'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const resetPwdVisible = ref(false)
const isEdit = ref(false)
const editId = ref(0)
const formRef = ref<FormInstance>()
const resetPwdFormRef = ref<FormInstance>()

const tableData = ref<any[]>([])
const searchForm = reactive({ realName: '', phone: '', role: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const form = ref({ username: '', password: '', realName: '', phone: '', role: '', status: 1 })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const resetPwdForm = ref({ id: 0, password: '' })
const resetPwdTarget = ref('')
const resetPwdRules: FormRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }]
}

function roleLabel(r: string) {
  const map: Record<string, string> = { ADMIN: '管理员', CASHIER: '收银员', KITCHEN: '厨房', INVENTORY: '库管' }
  return map[r] || r
}
function roleTagType(r: string) {
  const map: Record<string, string> = { ADMIN: 'danger', CASHIER: 'primary', KITCHEN: 'warning', INVENTORY: 'success' }
  return (map[r] || 'info') as any
}

async function loadData() {
  loading.value = true
  try {
    const { records, total } = await fetchEmployeeList({ ...searchForm, page: pagination.page, size: pagination.size })
    tableData.value = records
    pagination.total = total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.realName = ''
  searchForm.phone = ''
  searchForm.role = ''
  pagination.page = 1
  loadData()
}

function openDialog(row?: any) {
  isEdit.value = !!row
  editId.value = row?.id || 0
  form.value = row
    ? { username: row.username, password: '', realName: row.realName, phone: row.phone, role: row.role, status: row.status }
    : { username: '', password: '', realName: '', phone: '', role: '', status: 1 }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      const { password, ...data } = form.value
      await updateEmployee(editId.value, data)
    } else {
      await createEmployee(form.value)
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  await deleteEmployee(id)
  loadData()
}

function openResetPwdDialog(row: any) {
  resetPwdForm.value = { id: row.id, password: '' }
  resetPwdTarget.value = `${row.realName}（${row.username}）`
  resetPwdVisible.value = true
}

async function handleResetPwd() {
  await resetPwdFormRef.value?.validate()
  submitting.value = true
  try {
    await resetEmployeePassword(resetPwdForm.value.id, resetPwdForm.value.password)
    resetPwdVisible.value = false
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

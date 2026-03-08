<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="会员号">
          <el-input v-model="searchForm.memberNo" placeholder="请输入会员号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 标签页 -->
    <el-card shadow="never" class="table-card">
      <el-tabs v-model="activeTab">
        <!-- 会员列表 -->
        <el-tab-pane label="会员列表" name="member">
          <div class="card-header" style="margin-bottom:16px">
            <span />
            <el-button type="primary" @click="openMemberDialog()">新增会员</el-button>
          </div>
          <el-table :data="tableData" v-loading="loading" border stripe>
            <el-table-column prop="memberNo" label="会员号" width="130" />
            <el-table-column prop="name" label="姓名" width="90" />
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="gender" label="性别" width="70">
              <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
            </el-table-column>
            <el-table-column prop="birthday" label="生日" width="120" />
            <el-table-column prop="levelName" label="等级" width="90">
              <template #default="{ row }">
                <el-tag>{{ row.levelName || '普通' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="余额" width="100">
              <template #default="{ row }">¥{{ row.balance?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="points" label="积分" width="80" />
            <el-table-column prop="totalConsumption" label="累计消费" width="110">
              <template #default="{ row }">¥{{ row.totalConsumption?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openMemberDialog(row)">编辑</el-button>
                <el-button type="success" link @click="openRechargeDialog(row)">充值</el-button>
                <el-popconfirm title="确认删除该会员？" @confirm="handleDelete(row.id)">
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
        </el-tab-pane>

        <!-- 等级管理 -->
        <el-tab-pane label="等级管理" name="level">
          <div class="card-header" style="margin-bottom:16px">
            <span />
            <el-button type="primary" @click="openLevelDialog()">新增等级</el-button>
          </div>
          <el-table :data="levelList" v-loading="levelLoading" border stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="等级名称" />
            <el-table-column prop="minPoints" label="最低积分" />
            <el-table-column prop="discount" label="折扣" width="90">
              <template #default="{ row }">{{ row.discount }}折</template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="openLevelDialog(row)">编辑</el-button>
                <el-popconfirm title="确认删除？" @confirm="handleDeleteLevel(row.id)">
                  <template #reference>
                    <el-button type="danger" link>删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 会员新增/编辑对话框 -->
    <el-dialog v-model="memberDialogVisible" :title="isEdit ? '编辑会员' : '新增会员'" width="550px" destroy-on-close>
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberRules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="memberForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="memberForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="memberForm.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="memberForm.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="memberForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleMemberSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog v-model="rechargeDialogVisible" title="会员充值" width="450px" destroy-on-close>
      <el-form ref="rechargeFormRef" :model="rechargeForm" :rules="rechargeRules" label-width="80px">
        <el-form-item label="会员">
          <el-input :model-value="rechargeTarget" disabled />
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="rechargeForm.amount" :min="1" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleRecharge">确定</el-button>
      </template>
    </el-dialog>

    <!-- 等级新增/编辑对话框 -->
    <el-dialog v-model="levelDialogVisible" :title="isLevelEdit ? '编辑等级' : '新增等级'" width="450px" destroy-on-close>
      <el-form ref="levelFormRef" :model="levelForm" :rules="levelRules" label-width="80px">
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="levelForm.name" placeholder="如：白银、黄金" />
        </el-form-item>
        <el-form-item label="最低积分" prop="minPoints">
          <el-input-number v-model="levelForm.minPoints" :min="0" />
        </el-form-item>
        <el-form-item label="折扣" prop="discount">
          <el-input-number v-model="levelForm.discount" :min="1" :max="10" :precision="1" :step="0.5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="levelDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleLevelSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  fetchMemberList, createMember, updateMember, deleteMember, rechargeMember,
  fetchMemberLevels, createMemberLevel, updateMemberLevel, deleteMemberLevel
} from '@/api/business'

const loading = ref(false)
const levelLoading = ref(false)
const submitting = ref(false)
const activeTab = ref('member')

// ============ 会员列表 ============
const memberDialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(0)
const memberFormRef = ref<FormInstance>()
const tableData = ref<any[]>([])
const searchForm = reactive({ memberNo: '', name: '', phone: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const memberForm = ref({ name: '', phone: '', gender: 1, birthday: '', status: 1 })
const memberRules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const { records, total } = await fetchMemberList({ ...searchForm, page: pagination.page, size: pagination.size })
    tableData.value = records
    pagination.total = total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.memberNo = ''
  searchForm.name = ''
  searchForm.phone = ''
  pagination.page = 1
  loadData()
}

function openMemberDialog(row?: any) {
  isEdit.value = !!row
  editId.value = row?.id || 0
  memberForm.value = row
    ? { name: row.name, phone: row.phone, gender: row.gender, birthday: row.birthday || '', status: row.status }
    : { name: '', phone: '', gender: 1, birthday: '', status: 1 }
  memberDialogVisible.value = true
}

async function handleMemberSubmit() {
  await memberFormRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateMember(editId.value, memberForm.value)
    } else {
      await createMember(memberForm.value)
    }
    memberDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  await deleteMember(id)
  loadData()
}

// ============ 充值 ============
const rechargeDialogVisible = ref(false)
const rechargeFormRef = ref<FormInstance>()
const rechargeForm = ref({ memberId: 0, amount: 100 })
const rechargeTarget = ref('')
const rechargeRules: FormRules = {
  amount: [{ required: true, message: '请输入充值金额', trigger: 'blur' }]
}

function openRechargeDialog(row: any) {
  rechargeForm.value = { memberId: row.id, amount: 100 }
  rechargeTarget.value = `${row.name}（${row.memberNo}）`
  rechargeDialogVisible.value = true
}

async function handleRecharge() {
  await rechargeFormRef.value?.validate()
  submitting.value = true
  try {
    await rechargeMember(rechargeForm.value)
    rechargeDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

// ============ 等级管理 ============
const levelDialogVisible = ref(false)
const isLevelEdit = ref(false)
const levelEditId = ref(0)
const levelFormRef = ref<FormInstance>()
const levelList = ref<any[]>([])
const levelForm = ref({ name: '', minPoints: 0, discount: 10 })
const levelRules: FormRules = {
  name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  minPoints: [{ required: true, message: '请输入最低积分', trigger: 'blur' }],
  discount: [{ required: true, message: '请输入折扣', trigger: 'blur' }]
}

async function loadLevels() {
  levelLoading.value = true
  try {
    levelList.value = await fetchMemberLevels()
  } finally {
    levelLoading.value = false
  }
}

function openLevelDialog(row?: any) {
  isLevelEdit.value = !!row
  levelEditId.value = row?.id || 0
  levelForm.value = row
    ? { name: row.name, minPoints: row.minPoints, discount: row.discount }
    : { name: '', minPoints: 0, discount: 10 }
  levelDialogVisible.value = true
}

async function handleLevelSubmit() {
  await levelFormRef.value?.validate()
  submitting.value = true
  try {
    if (isLevelEdit.value) {
      await updateMemberLevel(levelEditId.value, levelForm.value)
    } else {
      await createMemberLevel(levelForm.value)
    }
    levelDialogVisible.value = false
    loadLevels()
  } finally {
    submitting.value = false
  }
}

async function handleDeleteLevel(id: number) {
  await deleteMemberLevel(id)
  loadLevels()
}

onMounted(() => {
  loadData()
  loadLevels()
})
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

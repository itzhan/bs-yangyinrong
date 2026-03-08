<template>
  <div class="reservation-page">
    <div class="page-banner">
      <h1>在线预订</h1>
      <p>提前预订座位 · 到店即享尊贵体验</p>
    </div>

    <div class="reservation-container">
      <div class="form-section">
        <div class="form-card">
          <h2 class="form-title"><span class="title-icon"><ClipboardList :size="22" /></span> 填写预订信息</h2>
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="姓名" prop="customerName">
                  <el-input v-model="form.customerName" placeholder="请输入您的姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="customerPhone">
                  <el-input v-model="form.customerPhone" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用餐时间" prop="reservationTime">
                  <el-date-picker
                    v-model="form.reservationTime" type="datetime" placeholder="选择日期时间"
                    :disabled-date="(d: Date) => d.getTime() < Date.now() - 86400000"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="用餐人数" prop="guestCount">
                  <el-input-number v-model="form.guestCount" :min="1" :max="20" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="选择桌台" prop="tableId">
              <div class="table-grid">
                <div
                  v-for="t in tables" :key="t.id"
                  class="table-option" :class="{ selected: form.tableId === t.id }"
                  @click="form.tableId = t.id"
                >
                  <div class="table-icon"><Armchair :size="28" :stroke-width="1.5" /></div>
                  <div class="table-name">{{ t.tableName || t.name }}</div>
                  <div class="table-cap">{{ t.capacity }}人桌</div>
                </div>
              </div>
              <el-text v-if="tables.length === 0" type="info" size="small">暂无可预订桌台</el-text>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="如有特殊要求请备注（选填）" />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" size="large" round :loading="submitting" style="width: 100%" @click="handleSubmit">
                确认预订
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <aside class="tips-section">
        <div class="tips-card">
          <h3><Pin :size="16" style="display:inline;vertical-align:middle" /> 预订须知</h3>
          <ul>
            <li>请提前至少 <strong>2小时</strong> 预订</li>
            <li>预订成功后请按时到店，超时 <strong>30分钟</strong> 将自动取消</li>
            <li>如需取消请提前致电 <strong>028-8888-6666</strong></li>
            <li>节假日座位紧张，建议尽早预订</li>
          </ul>
        </div>
        <div class="tips-card">
          <h3><Clock :size="16" style="display:inline;vertical-align:middle" /> 营业时间</h3>
          <p>午市：11:00 - 14:00</p>
          <p>晚市：17:00 - 22:00</p>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { ClipboardList, Armchair, Pin, Clock } from 'lucide-vue-next'
import { getAvailableTables, submitReservation } from '@/api'

const formRef = ref<FormInstance>()
const tables = ref<any[]>([])
const submitting = ref(false)

const form = reactive({
  customerName: '',
  customerPhone: '',
  reservationTime: '',
  guestCount: 2,
  tableId: null as number | null,
  remark: ''
})

const rules = {
  customerName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  customerPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  reservationTime: [{ required: true, message: '请选择用餐时间', trigger: 'change' }],
  guestCount: [{ required: true, message: '请输入用餐人数', trigger: 'change' }],
  tableId: [{ required: true, message: '请选择桌台', trigger: 'change' }]
}

const fetchTables = async () => {
  try {
    const res = await getAvailableTables()
    tables.value = res.data || []
  } catch { /* ignore */ }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await submitReservation({ ...form })
    ElMessage.success('预订成功！我们将为您保留座位，请按时到店')
    formRef.value?.resetFields()
    form.tableId = null
  } catch { /* handled by interceptor */ }
  submitting.value = false
}

onMounted(fetchTables)
</script>

<style scoped lang="scss">
$red: #c0392b;
$gold: #d4a017;
$dark: #1a1a2e;

.reservation-page { background: #faf7f4; min-height: 100vh; }

.page-banner {
  background: linear-gradient(135deg, $dark, $red); padding: 120px 20px 50px; text-align: center; color: #fff;
  h1 { font-size: 36px; font-weight: 800; letter-spacing: 4px; margin-bottom: 10px; }
  p { color: rgba(255,255,255,0.7); font-size: 15px; letter-spacing: 2px; }
}

.reservation-container {
  max-width: 1100px; margin: 0 auto; padding: 40px 20px 60px;
  display: flex; gap: 30px; align-items: flex-start;
}

.form-section { flex: 1; }
.form-card {
  background: #fff; border-radius: 16px; padding: 36px; box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}
.form-title {
  font-size: 22px; font-weight: 700; color: $dark; margin-bottom: 24px;
  display: flex; align-items: center; gap: 8px;
}

/* Table Grid */
.table-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; width: 100%; }
.table-option {
  border: 2px solid #eee; border-radius: 12px; padding: 16px 12px;
  text-align: center; cursor: pointer; transition: all 0.2s;
  &:hover { border-color: $red; }
  &.selected { border-color: $red; background: rgba(192,57,43,0.06); }
}
.table-icon { font-size: 28px; margin-bottom: 6px; }
.table-name { font-size: 14px; font-weight: 600; color: $dark; }
.table-cap { font-size: 12px; color: #999; margin-top: 2px; }

/* Tips */
.tips-section { width: 280px; flex-shrink: 0; display: flex; flex-direction: column; gap: 16px; }
.tips-card {
  background: #fff; border-radius: 14px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  h3 { font-size: 16px; font-weight: 700; color: $dark; margin-bottom: 14px; }
  ul { list-style: none; padding: 0; }
  li { font-size: 14px; color: #666; line-height: 2; padding-left: 12px; position: relative;
    &::before { content: '•'; position: absolute; left: 0; color: $red; }
  }
  p { font-size: 14px; color: #666; line-height: 2; }
}

@media (max-width: 768px) {
  .reservation-container { flex-direction: column; }
  .tips-section { width: 100%; }
  .table-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>

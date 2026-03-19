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

            <!-- 用餐人数 -->
            <el-form-item label="用餐人数" prop="guestCount">
              <el-input-number v-model="form.guestCount" :min="1" :max="20" style="width: 100%" />
            </el-form-item>

            <!-- 选择日期 -->
            <el-form-item label="选择日期" prop="reservationDate">
              <div class="date-grid">
                <div
                  v-for="d in dateOptions" :key="d.value"
                  class="date-option" :class="{ selected: form.reservationDate === d.value }"
                  @click="selectDate(d.value)"
                >
                  <div class="date-weekday">{{ d.weekday }}</div>
                  <div class="date-day">{{ d.label }}</div>
                </div>
              </div>
            </el-form-item>

            <!-- 选择时间段 -->
            <el-form-item label="选择用餐时间" prop="timeSlot">
              <div class="time-section">
                <div class="time-label">午市</div>
                <div class="time-grid">
                  <div
                    v-for="t in lunchSlots" :key="t"
                    class="time-option" :class="{ selected: form.timeSlot === t, disabled: !form.reservationDate }"
                    @click="form.reservationDate && selectTimeSlot(t)"
                  >
                    {{ t }}
                  </div>
                </div>
                <div class="time-label" style="margin-top: 12px">晚市</div>
                <div class="time-grid">
                  <div
                    v-for="t in dinnerSlots" :key="t"
                    class="time-option" :class="{ selected: form.timeSlot === t, disabled: !form.reservationDate }"
                    @click="form.reservationDate && selectTimeSlot(t)"
                  >
                    {{ t }}
                  </div>
                </div>
              </div>
            </el-form-item>

            <!-- 选择桌台 -->
            <el-form-item label="选择桌型" prop="capacity">
              <div v-if="loadingAvailability" style="text-align:center;padding:20px">
                <el-text type="info">加载中...</el-text>
              </div>
              <div v-else-if="!form.reservationDate || !form.timeSlot" style="padding:10px 0">
                <el-text type="info" size="small">请先选择日期和时间段</el-text>
              </div>
              <div v-else class="table-grid">
                <div
                  v-for="item in availability" :key="item.capacity"
                  class="table-option"
                  :class="{
                    selected: form.capacity === item.capacity,
                    disabled: item.available <= 0
                  }"
                  @click="item.available > 0 && (form.capacity = item.capacity)"
                >
                  <div class="table-icon"><Armchair :size="28" :stroke-width="1.5" /></div>
                  <div class="table-name">{{ item.capacity }}人桌</div>
                  <div class="table-avail" :class="{ 'no-avail': item.available <= 0 }">
                    {{ item.available > 0 ? `剩余 ${item.available} 桌` : '已满' }}
                  </div>
                </div>
              </div>
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

    <!-- 二次确认弹窗 -->
    <el-dialog v-model="confirmVisible" title="确认预订信息" width="440px" :close-on-click-modal="false">
      <div class="confirm-info">
        <div class="confirm-row"><span class="confirm-label">预订人：</span><span>{{ form.customerName }}</span></div>
        <div class="confirm-row"><span class="confirm-label">手机号：</span><span>{{ form.customerPhone }}</span></div>
        <div class="confirm-row"><span class="confirm-label">用餐人数：</span><span>{{ form.guestCount }} 人</span></div>
        <div class="confirm-row"><span class="confirm-label">日期：</span><span>{{ form.reservationDate }}</span></div>
        <div class="confirm-row"><span class="confirm-label">时间段：</span><span>{{ form.timeSlot }}</span></div>
        <div class="confirm-row"><span class="confirm-label">桌型：</span><span>{{ form.capacity }}人桌</span></div>
        <div v-if="form.remark" class="confirm-row"><span class="confirm-label">备注：</span><span>{{ form.remark }}</span></div>
      </div>
      <template #footer>
        <el-button @click="confirmVisible = false">返回修改</el-button>
        <el-button type="danger" :loading="submitting" @click="doSubmit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { ClipboardList, Armchair, Pin, Clock } from 'lucide-vue-next'
import { getTableAvailability, submitReservation } from '@/api'

const formRef = ref<FormInstance>()
const submitting = ref(false)
const confirmVisible = ref(false)
const loadingAvailability = ref(false)
const availability = ref<{ capacity: number; total: number; reserved: number; available: number }[]>([])

const form = reactive({
  customerName: '',
  customerPhone: '',
  reservationDate: '',
  timeSlot: '',
  guestCount: 2,
  capacity: null as number | null,
  remark: ''
})

const rules = {
  customerName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  customerPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  reservationDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择用餐时间', trigger: 'change' }],
  guestCount: [{ required: true, message: '请输入用餐人数', trigger: 'change' }],
  capacity: [{ required: true, message: '请选择桌型', trigger: 'change' }]
}

// ===== 日期选项（未来7天）=====
const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const dateOptions = computed(() => {
  const list = []
  const now = new Date()
  for (let i = 0; i < 7; i++) {
    const d = new Date(now)
    d.setDate(d.getDate() + i)
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const value = `${d.getFullYear()}-${m}-${day}`
    list.push({
      value,
      label: i === 0 ? '今天' : i === 1 ? '明天' : `${m}-${day}`,
      weekday: weekdays[d.getDay()]
    })
  }
  return list
})

// ===== 时间段 =====
const lunchSlots = ['11:00', '12:00', '13:00']
const dinnerSlots = ['17:00', '18:00', '19:00', '20:00', '21:00']

// ===== 选择日期 =====
const selectDate = (val: string) => {
  form.reservationDate = val
  // 切换日期后重置桌型选择并刷新可用性
  form.capacity = null
  if (form.timeSlot) {
    fetchAvailability()
  }
}

// ===== 选择时间段 =====
const selectTimeSlot = (val: string) => {
  form.timeSlot = val
  form.capacity = null
  fetchAvailability()
}

// ===== 获取可用性 =====
const fetchAvailability = async () => {
  if (!form.reservationDate || !form.timeSlot) return
  loadingAvailability.value = true
  try {
    const res = await getTableAvailability({ date: form.reservationDate, timeSlot: form.timeSlot })
    availability.value = res.data || []
  } catch { /* ignore */ }
  loadingAvailability.value = false
}

// ===== 提交（先弹窗确认）=====
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  confirmVisible.value = true
}

// ===== 确认提交 =====
const doSubmit = async () => {
  submitting.value = true
  try {
    await submitReservation({
      customerName: form.customerName,
      customerPhone: form.customerPhone,
      guestCount: form.guestCount,
      reservationDate: form.reservationDate,
      timeSlot: form.timeSlot,
      capacity: form.capacity,
      remark: form.remark
    })
    ElMessage.success('预订成功！我们将为您保留座位，请按时到店')
    confirmVisible.value = false
    formRef.value?.resetFields()
    form.capacity = null
    availability.value = []
  } catch { /* handled by interceptor */ }
  submitting.value = false
}
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

/* Date Grid */
.date-grid { display: flex; gap: 10px; flex-wrap: wrap; width: 100%; }
.date-option {
  flex: 1; min-width: 70px; border: 2px solid #eee; border-radius: 12px; padding: 12px 8px;
  text-align: center; cursor: pointer; transition: all 0.2s;
  &:hover { border-color: $red; }
  &.selected { border-color: $red; background: rgba(192,57,43,0.06); }
}
.date-weekday { font-size: 12px; color: #999; margin-bottom: 4px; }
.date-day { font-size: 15px; font-weight: 600; color: $dark; }

/* Time Grid */
.time-section { width: 100%; }
.time-label { font-size: 13px; color: #999; margin-bottom: 8px; font-weight: 600; }
.time-grid { display: flex; gap: 10px; flex-wrap: wrap; }
.time-option {
  padding: 10px 18px; border: 2px solid #eee; border-radius: 10px;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s;
  &:hover:not(.disabled) { border-color: $red; }
  &.selected { border-color: $red; background: rgba(192,57,43,0.06); color: $red; }
  &.disabled { opacity: 0.4; cursor: not-allowed; }
}

/* Table Grid */
.table-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; width: 100%; }
.table-option {
  border: 2px solid #eee; border-radius: 12px; padding: 16px 12px;
  text-align: center; cursor: pointer; transition: all 0.2s;
  &:hover:not(.disabled) { border-color: $red; }
  &.selected { border-color: $red; background: rgba(192,57,43,0.06); }
  &.disabled { opacity: 0.4; cursor: not-allowed; background: #f5f5f5; }
}
.table-icon { font-size: 28px; margin-bottom: 6px; }
.table-name { font-size: 14px; font-weight: 600; color: $dark; }
.table-avail { font-size: 12px; margin-top: 4px; color: #52c41a; font-weight: 500; }
.table-avail.no-avail { color: #999; }

/* Confirm Dialog */
.confirm-info { padding: 8px 0; }
.confirm-row {
  display: flex; padding: 10px 0; border-bottom: 1px solid #f0f0f0; font-size: 15px;
  &:last-child { border-bottom: none; }
}
.confirm-label { color: #999; width: 90px; flex-shrink: 0; }

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
  .date-grid { flex-wrap: wrap; }
  .date-option { min-width: 60px; }
}
</style>

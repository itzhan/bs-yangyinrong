<template>
  <div class="member-page">
    <div class="page-banner">
      <h1>会员中心</h1>
      <p>尊享会员权益 · 每一分消费都有回报</p>
    </div>

    <div class="member-container">
      <!-- Member Info Card -->
      <div class="info-card">
        <div class="info-left">
          <div class="avatar">{{ memberInfo.name?.charAt(0) || '会' }}</div>
          <div class="info-text">
            <h2>{{ memberInfo.name || '会员' }}</h2>
            <p class="member-no">会员编号：{{ memberInfo.memberNo || '-' }}</p>
            <el-tag :type="levelType" effect="dark" round>{{ memberInfo.level || '普通会员' }}</el-tag>
          </div>
        </div>
        <div class="info-stats">
          <div class="stat-item">
            <span class="stat-value">¥{{ memberInfo.balance?.toFixed(2) || '0.00' }}</span>
            <span class="stat-label">账户余额</span>
          </div>
          <div class="stat-divider" />
          <div class="stat-item">
            <span class="stat-value">{{ memberInfo.points || 0 }}</span>
            <span class="stat-label">积分</span>
          </div>
          <div class="stat-divider" />
          <div class="stat-item">
            <span class="stat-value">¥{{ memberInfo.totalConsumption?.toFixed(2) || '0.00' }}</span>
            <span class="stat-label">累计消费</span>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" class="record-tabs">
        <el-tab-pane label="消费记录" name="orders">
          <div v-if="orders.length === 0" class="empty-state">
            <el-empty description="暂无消费记录" />
          </div>
          <div v-else class="record-list">
            <div v-for="order in orders" :key="order.id" class="record-card">
              <div class="record-header">
                <span class="record-no">订单号：{{ order.orderNo }}</span>
                <el-tag size="small" :type="order.status === '已完成' ? 'success' : 'info'">{{ order.status }}</el-tag>
              </div>
              <div v-if="order.items?.length" class="order-items">
                <span v-for="(item, i) in order.items" :key="i" class="order-item">
                  {{ item.dishName || item.name }} × {{ item.quantity }}
                </span>
              </div>
              <div class="record-footer">
                <span class="record-time">{{ order.createTime }}</span>
                <span class="record-amount">¥{{ order.totalAmount?.toFixed(2) }}</span>
              </div>
            </div>
          </div>
          <div v-if="orderTotal > orderPageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="orderPage" :page-size="orderPageSize" :total="orderTotal"
              layout="prev, pager, next" background small @current-change="fetchOrders"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="充值记录" name="recharges">
          <div v-if="recharges.length === 0" class="empty-state">
            <el-empty description="暂无充值记录" />
          </div>
          <div v-else class="record-list">
            <div v-for="r in recharges" :key="r.id" class="record-card recharge-card">
              <div class="record-header">
                <span class="record-label">充值</span>
                <el-tag size="small" type="success">成功</el-tag>
              </div>
              <div class="record-footer">
                <span class="record-time">{{ r.createTime || r.rechargeTime }}</span>
                <span class="record-amount plus">+¥{{ r.amount?.toFixed(2) }}</span>
              </div>
            </div>
          </div>
          <div v-if="rechargeTotal > rechargePageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="rechargePage" :page-size="rechargePageSize" :total="rechargeTotal"
              layout="prev, pager, next" background small @current-change="fetchRecharges"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="预订记录" name="reservations">
          <div v-if="reservations.length === 0" class="empty-state">
            <el-empty description="暂无预订记录" />
          </div>
          <div v-else class="record-list">
            <div v-for="r in reservations" :key="r.id" class="record-card reservation-card">
              <div class="record-header">
                <span class="record-no"><Armchair :size="14" style="display:inline;vertical-align:middle" /> 桌台：{{ r.tableNumber }}</span>
                <el-tag size="small" :type="reservationTagType(r.status)">{{ r.statusText }}</el-tag>
              </div>
              <div class="reservation-info">
                <span><Calendar :size="14" style="display:inline;vertical-align:middle" /> {{ formatDate(r.reservationTime) }}</span>
                <span><Users :size="14" style="display:inline;vertical-align:middle" /> {{ r.guestCount }}人</span>
                <span v-if="r.remark"><FileText :size="14" style="display:inline;vertical-align:middle" /> {{ r.remark }}</span>
              </div>
              <div class="record-footer">
                <span class="record-time">预订于 {{ formatDate(r.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div v-if="reservationTotal > reservationPageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="reservationPage" :page-size="reservationPageSize" :total="reservationTotal"
              layout="prev, pager, next" background small @current-change="fetchReservations"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Armchair, Calendar, Users, FileText } from 'lucide-vue-next'
import { getMemberInfo, getMyOrders, getMyRecharges, getMyReservations } from '@/api'

const memberInfo = ref<any>({})
const activeTab = ref('orders')

// Orders
const orders = ref<any[]>([])
const orderPage = ref(1)
const orderPageSize = 8
const orderTotal = ref(0)

// Recharges
const recharges = ref<any[]>([])
const rechargePage = ref(1)
const rechargePageSize = 10
const rechargeTotal = ref(0)

// Reservations
const reservations = ref<any[]>([])
const reservationPage = ref(1)
const reservationPageSize = 10
const reservationTotal = ref(0)

const levelType = computed(() => {
  const lvl = memberInfo.value.level
  if (lvl?.includes('金') || lvl?.includes('VIP')) return 'warning'
  if (lvl?.includes('银')) return ''
  return 'info'
})

const reservationTagType = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: '', 2: 'success', 3: 'info', 4: 'danger' }
  return map[status] ?? 'info'
}

const pad2 = (n: number) => String(n).padStart(2, '0')

const toDate = (v: any): Date | null => {
  if (!v) return null
  if (v instanceof Date) return Number.isNaN(v.getTime()) ? null : v
  if (typeof v === 'number') {
    const date = new Date(v)
    return Number.isNaN(date.getTime()) ? null : date
  }
  if (typeof v === 'string') {
    const text = v.trim()
    if (!text) return null
    if (text.includes('T') || text.includes('-') || text.includes('/')) {
      const date = new Date(text.replace(' ', 'T'))
      return Number.isNaN(date.getTime()) ? null : date
    }
    if (/^\d+$/.test(text)) {
      const date = new Date(Number(text))
      return Number.isNaN(date.getTime()) ? null : date
    }
    return null
  }
  if (Array.isArray(v) && v.length >= 3) {
    const [y, m, d, hh = 0, mm = 0, ss = 0] = v
    const date = new Date(Number(y), Number(m) - 1, Number(d), Number(hh), Number(mm), Number(ss))
    return Number.isNaN(date.getTime()) ? null : date
  }
  return null
}

const formatDate = (v: any) => {
  if (!v) return '-'
  if (typeof v === 'string') return v.replace('T', ' ').substring(0, 16)
  const date = toDate(v)
  if (!date) return '-'
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())} ${pad2(date.getHours())}:${pad2(date.getMinutes())}`
}

const normalizeReservation = (r: any) => {
  const status = Number(r?.status ?? 0)
  const statusTextMap: Record<number, string> = {
    0: '待确认',
    1: '已确认',
    2: '已到店',
    3: '已完成',
    4: '已取消'
  }
  return {
    ...r,
    id: r?.id,
    tableNumber: r?.tableNumber ?? r?.tableNo ?? '-',
    reservationTime: r?.reservationTime ?? r?.reservation_time,
    guestCount: r?.guestCount ?? r?.guest_count ?? 0,
    status,
    statusText: r?.statusText ?? r?.status_text ?? statusTextMap[status] ?? '未知',
    remark: r?.remark ?? '',
    createdAt: r?.createdAt ?? r?.created_at ?? r?.createTime ?? r?.create_time
  }
}

const fetchMemberInfo = async () => {
  try {
    const res = await getMemberInfo()
    memberInfo.value = res.data || {}
  } catch { /* ignore */ }
}

const fetchOrders = async () => {
  try {
    const res = await getMyOrders({ page: orderPage.value, size: orderPageSize })
    const d = res.data
    orders.value = d?.records || d || []
    orderTotal.value = d?.total || 0
  } catch { /* ignore */ }
}

const fetchRecharges = async () => {
  try {
    const res = await getMyRecharges({ page: rechargePage.value, size: rechargePageSize })
    const d = res.data
    recharges.value = d?.records || d || []
    rechargeTotal.value = d?.total || 0
  } catch { /* ignore */ }
}

const fetchReservations = async () => {
  try {
    const res = await getMyReservations({ page: reservationPage.value, size: reservationPageSize })
    const d = res?.data
    const list = Array.isArray(d) ? d : (Array.isArray(d?.records) ? d.records : [])
    reservations.value = list.map(normalizeReservation)
    reservationTotal.value = Number(d?.total ?? reservations.value.length)
  } catch { /* ignore */ }
}

watch(activeTab, (tab) => {
  if (tab === 'orders' && orders.value.length === 0) fetchOrders()
  else if (tab === 'recharges' && recharges.value.length === 0) fetchRecharges()
  else if (tab === 'reservations') fetchReservations()
})

onMounted(() => {
  fetchMemberInfo()
  fetchOrders()
})
</script>

<style scoped lang="scss">
$red: #c0392b;
$gold: #d4a017;
$dark: #1a1a2e;

.member-page { background: #faf7f4; min-height: 100vh; }

.page-banner {
  background: linear-gradient(135deg, $dark, $red); padding: 120px 20px 50px; text-align: center; color: #fff;
  h1 { font-size: 36px; font-weight: 800; letter-spacing: 4px; margin-bottom: 10px; }
  p { color: rgba(255,255,255,0.7); font-size: 15px; letter-spacing: 2px; }
}

.member-container { max-width: 900px; margin: 0 auto; padding: 30px 20px 60px; }

/* Info Card */
.info-card {
  background: linear-gradient(135deg, $dark 0%, #2d1b1b 60%, $red 100%);
  border-radius: 20px; padding: 32px 36px; color: #fff;
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2); margin-bottom: 28px;
}
.info-left { display: flex; align-items: center; gap: 20px; }
.avatar {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, $gold, #b8860b);
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: 800; color: #fff;
}
.info-text h2 { font-size: 22px; font-weight: 700; margin-bottom: 4px; }
.member-no { font-size: 13px; color: rgba(255,255,255,0.6); margin-bottom: 8px; }
.info-stats { display: flex; align-items: center; gap: 0; }
.stat-item { text-align: center; padding: 0 24px; }
.stat-value { display: block; font-size: 22px; font-weight: 800; color: $gold; margin-bottom: 4px; }
.stat-label { font-size: 12px; color: rgba(255,255,255,0.6); }
.stat-divider { width: 1px; height: 40px; background: rgba(255,255,255,0.15); }

/* Tabs */
.record-tabs {
  background: #fff; border-radius: 16px; padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
:deep(.el-tabs__item) { font-size: 16px; font-weight: 600; }
:deep(.el-tabs__active-bar) { background: $red; }
:deep(.el-tabs__item.is-active) { color: $red; }

/* Record Cards */
.record-list { display: flex; flex-direction: column; gap: 12px; }
.record-card {
  border: 1px solid #f0ebe5; border-radius: 12px; padding: 16px 20px; transition: all 0.2s;
  &:hover { border-color: rgba(192,57,43,0.2); box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
}
.record-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.record-no { font-size: 14px; font-weight: 600; color: $dark; }
.record-label { font-size: 14px; font-weight: 600; color: $dark; }
.order-items { margin-bottom: 8px; }
.order-item {
  display: inline-block; font-size: 13px; color: #888; margin-right: 12px;
  padding: 2px 8px; background: #faf7f4; border-radius: 4px;
}
.record-footer { display: flex; justify-content: space-between; align-items: center; }
.record-time { font-size: 13px; color: #bbb; }
.record-amount { font-size: 18px; font-weight: 800; color: $red; }
.record-amount.plus { color: #27ae60; }

.empty-state { padding: 40px 0; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }

/* Reservation */
.reservation-info {
  display: flex; gap: 20px; font-size: 14px; color: #666; margin-bottom: 8px;
  span { display: flex; align-items: center; gap: 4px; }
}

@media (max-width: 768px) {
  .info-card { flex-direction: column; gap: 20px; text-align: center; }
  .info-left { flex-direction: column; }
  .info-stats { gap: 0; }
  .stat-item { padding: 0 16px; }
}
</style>

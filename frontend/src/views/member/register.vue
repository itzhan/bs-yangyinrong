<template>
  <div class="register-page">
    <div class="register-bg" />
    <div class="register-card">
      <div class="card-header">
        <span class="brand-icon"><Soup :size="48" :stroke-width="1.5" /></span>
        <h1>川味红锅</h1>
        <p>会员注册</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入您的姓名" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号（将作为登录账号）" maxlength="11" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="danger" size="large" round :loading="loading" style="width: 100%" @click="handleRegister">
            立即注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="card-footer">
        <span>已有账号？</span>
        <router-link to="/member/login" class="link-gold">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { Soup } from 'lucide-vue-next'
import { memberRegister } from '@/api'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ name: '', phone: '', gender: 1 })

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await memberRegister(form)
    ElMessage.success('注册成功！初始密码为手机号后6位，请登录')
    router.push('/member/login')
  } catch { /* handled by interceptor */ }
  loading.value = false
}
</script>

<style scoped lang="scss">
$red: #c0392b;
$gold: #d4a017;
$dark: #1a1a2e;

.register-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden;
}
.register-bg {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, $dark 0%, #2d1b1b 50%, $red 100%);
  &::before {
    content: ''; position: absolute; inset: 0;
    background: radial-gradient(circle at 80% 30%, rgba(212,160,23,0.1) 0%, transparent 50%),
                radial-gradient(circle at 20% 70%, rgba(192,57,43,0.15) 0%, transparent 50%);
  }
}
.register-card {
  position: relative; background: rgba(255,255,255,0.97); border-radius: 20px;
  padding: 48px 40px 36px; width: 420px; max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.card-header {
  text-align: center; margin-bottom: 32px;
  .brand-icon { font-size: 48px; display: block; margin-bottom: 8px; }
  h1 { font-size: 28px; font-weight: 800; color: $dark; letter-spacing: 4px; margin-bottom: 4px; }
  p { color: $red; font-size: 15px; font-weight: 500; }
}
.card-footer {
  text-align: center; margin-top: 16px; font-size: 14px; color: #999;
}
.link-gold { color: $gold; font-weight: 600; margin-left: 6px; &:hover { text-decoration: underline; } }

:deep(.el-button--danger) {
  background: linear-gradient(135deg, $red, #e74c3c) !important;
  border: none !important; font-size: 16px; letter-spacing: 6px;
}
</style>

<template>
  <div class="login-page">
    <div class="login-bg" />
    <div class="login-card">
      <div class="card-header">
        <span class="brand-icon"><Soup :size="48" :stroke-width="1.5" /></span>
        <h1>川味红锅</h1>
        <p>会员登录</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码（手机后6位）" prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" size="large" round :loading="loading" style="width: 100%" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="card-footer">
        <span>还不是会员？</span>
        <router-link to="/member/register" class="link-gold">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { Soup } from 'lucide-vue-next'
import { memberLogin } from '@/api'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ phone: '', password: '' })

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await memberLogin(form)
    const data = res.data
    localStorage.setItem('member_token', data.token)
    localStorage.setItem('member_name', data.name || data.memberName || '')
    ElMessage.success('登录成功，欢迎回来！')
    const redirect = (route.query.redirect as string) || '/member'
    router.push(redirect)
  } catch { /* handled by interceptor */ }
  loading.value = false
}
</script>

<style scoped lang="scss">
$red: #c0392b;
$gold: #d4a017;
$dark: #1a1a2e;

.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden;
}
.login-bg {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, $dark 0%, #2d1b1b 50%, $red 100%);
  &::before {
    content: ''; position: absolute; inset: 0;
    background: radial-gradient(circle at 20% 50%, rgba(212,160,23,0.1) 0%, transparent 50%),
                radial-gradient(circle at 80% 50%, rgba(192,57,43,0.15) 0%, transparent 50%);
  }
}
.login-card {
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

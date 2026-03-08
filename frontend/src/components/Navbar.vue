<template>
  <header class="navbar" :class="{ scrolled: isScrolled }">
    <div class="navbar-inner">
      <router-link to="/" class="logo">
        <span class="logo-icon"><Soup :size="28" :stroke-width="2" /></span>
        <span class="logo-text">川味红锅</span>
      </router-link>
      <nav class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/menu" class="nav-link">菜品菜单</router-link>
        <router-link to="/reservation" class="nav-link">在线预订</router-link>
        <router-link to="/about" class="nav-link">关于我们</router-link>
      </nav>
      <div class="nav-right">
        <template v-if="memberName">
          <router-link to="/member" class="member-link">
            <el-icon><User /></el-icon>
            <span>{{ memberName }}</span>
          </router-link>
          <el-button text @click="logout">退出</el-button>
        </template>
        <template v-else>
          <router-link to="/member/login">
            <el-button type="primary" round size="small">会员登录</el-button>
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { User } from '@element-plus/icons-vue'
import { Soup } from 'lucide-vue-next'

const router = useRouter()
const isScrolled = ref(false)
const memberName = ref(localStorage.getItem('member_name') || '')

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}
const logout = () => {
  localStorage.removeItem('member_token')
  localStorage.removeItem('member_name')
  memberName.value = ''
  router.push('/')
}

onMounted(() => window.addEventListener('scroll', handleScroll))
onUnmounted(() => window.removeEventListener('scroll', handleScroll))
</script>

<style scoped lang="scss">
.navbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 1000;
  background: transparent; transition: all 0.3s;
  &.scrolled { background: rgba(26, 26, 46, 0.95); backdrop-filter: blur(10px); box-shadow: 0 2px 20px rgba(0,0,0,0.2); }
}
.navbar-inner {
  max-width: 1200px; margin: 0 auto; padding: 0 20px;
  display: flex; align-items: center; height: 70px; position: relative;
}
.logo { display: flex; align-items: center; gap: 8px; }
.logo-icon { font-size: 28px; }
.logo-text { font-size: 22px; font-weight: 700; color: #fff; letter-spacing: 2px; }
.nav-links { display: flex; gap: 32px; margin-left: 60px; }
.nav-link {
  color: rgba(255,255,255,0.8); font-size: 15px; padding: 8px 0; transition: color 0.3s;
  &:hover, &.router-link-active { color: #f1c40f; }
}
.nav-right { margin-left: auto; display: flex; align-items: center; gap: 12px; }
.member-link { display: flex; align-items: center; gap: 6px; color: #fff; font-size: 14px; }
</style>

<template>
  <footer class="footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <span class="brand-icon"><Soup :size="36" :stroke-width="2" /></span>
        <span class="brand-name">川味红锅</span>
        <p class="brand-slogan">传承经典，匠心煮义</p>
      </div>
      <div class="footer-info">
        <div class="info-item">
          <h4>营业时间</h4>
          <p>午市 11:00 - 14:00</p>
          <p>晚市 17:00 - 22:00</p>
        </div>
        <div class="info-item">
          <h4>联系方式</h4>
          <p>电话：028-8888-6666</p>
          <p>地址：成都市锦江区春熙路88号</p>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2026 川味红锅 版权所有 | 毕业设计作品</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { Soup } from 'lucide-vue-next'
</script>

<style scoped lang="scss">
.footer { background: #1a1a2e; color: rgba(255,255,255,0.7); padding: 50px 0 20px; }
.footer-inner { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
.footer-brand { text-align: center; margin-bottom: 30px; }
.brand-icon { font-size: 36px; }
.brand-name { font-size: 24px; font-weight: 700; color: #d4a017; margin-left: 8px; }
.brand-slogan { margin-top: 8px; color: rgba(255,255,255,0.5); }
.footer-info { display: flex; justify-content: center; gap: 80px; padding: 20px 0; border-top: 1px solid rgba(255,255,255,0.1); }
.info-item h4 { color: #f1c40f; margin-bottom: 8px; font-size: 15px; }
.info-item p { font-size: 14px; line-height: 1.8; }
.footer-bottom { text-align: center; padding-top: 20px; margin-top: 20px; border-top: 1px solid rgba(255,255,255,0.1); font-size: 13px; color: rgba(255,255,255,0.4); }
</style>

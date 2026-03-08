<template>
  <div class="about-page">
    <div class="page-banner">
      <h1>关于我们</h1>
      <p>一锅红汤 · 一段故事 · 一份传承</p>
    </div>

    <!-- Story -->
    <section class="section story-section">
      <div class="section-inner">
        <div class="story-content">
          <span class="section-tag">— 品牌故事 —</span>
          <h2>始于匠心，忠于味道</h2>
          <p>
            川味红锅创立于巴蜀之地，源自一位老火锅匠人对传统味道的执着追求。
            我们坚持选用上等牛油、数十种天然香料，经八小时文火慢熬，只为还原那一口地道的老成都味道。
          </p>
          <p>
            从街边小店到如今的品牌连锁，变的是规模，不变的是那锅翻滚的红汤和对品质的坚守。
            每一位走进川味红锅的食客，都是我们最珍贵的家人。
          </p>
        </div>
        <div class="story-image">
          <div class="image-frame">
            <img :src="storyImg" alt="传承三代的老味道" class="story-photo" />
          </div>
        </div>
      </div>
    </section>

    <!-- Advantages -->
    <section class="section advantages-section">
      <div class="section-header">
        <span class="section-tag">— 我们的优势 —</span>
        <h2>四大核心理念</h2>
      </div>
      <div class="adv-grid">
        <div v-for="adv in advantages" :key="adv.title" class="adv-card">
          <div class="adv-icon"><component :is="adv.icon" :size="44" :stroke-width="1.5" /></div>
          <h3>{{ adv.title }}</h3>
          <p>{{ adv.desc }}</p>
        </div>
      </div>
    </section>

    <!-- Contact & Info -->
    <section class="section contact-section">
      <div class="section-header">
        <span class="section-tag">— 联系我们 —</span>
        <h2>期待您的光临</h2>
      </div>
      <div class="contact-grid">
        <div class="contact-card">
          <div class="contact-icon"><MapPin :size="40" :stroke-width="1.5" /></div>
          <h3>餐厅地址</h3>
          <p>成都市锦江区春熙路88号</p>
          <p class="sub-text">地铁2号线春熙路站B出口步行3分钟</p>
        </div>
        <div class="contact-card">
          <div class="contact-icon"><Phone :size="40" :stroke-width="1.5" /></div>
          <h3>预订电话</h3>
          <p>028-8888-6666</p>
          <p class="sub-text">欢迎来电预订，我们为您保留座位</p>
        </div>
        <div class="contact-card">
          <div class="contact-icon"><Clock :size="40" :stroke-width="1.5" /></div>
          <h3>营业时间</h3>
          <p>午市：11:00 - 14:00</p>
          <p>晚市：17:00 - 22:00</p>
          <p class="sub-text">全年无休，节假日正常营业</p>
        </div>
        <div class="contact-card">
          <div class="contact-icon"><MessageCircle :size="40" :stroke-width="1.5" /></div>
          <h3>在线服务</h3>
          <p>关注公众号"川味红锅"</p>
          <p class="sub-text">获取最新优惠活动与会员专属权益</p>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta-section">
      <h2>品正宗川味，就来川味红锅</h2>
      <p>在线预订座位，到店即享尊贵体验</p>
      <div class="cta-actions">
        <el-button type="danger" size="large" round @click="$router.push('/reservation')">立即预订</el-button>
        <el-button size="large" round class="btn-gold" @click="$router.push('/menu')">浏览菜单</el-button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { type Component } from 'vue'
import { Beef, Flame, Sparkles, Landmark, MapPin, Phone, Clock, MessageCircle } from 'lucide-vue-next'
import storyImg from '@/assets/hotpot-story.png'

const advantages: { icon: Component; title: string; desc: string }[] = [
  { icon: Beef, title: '食材甄选', desc: '每日直采新鲜食材，与优质牧场、菜农建立长期合作，从源头把控品质' },
  { icon: Flame, title: '古法工艺', desc: '传承三代秘方，精选二十余种天然香料，文火慢熬8小时，浓缩地道川味精华' },
  { icon: Sparkles, title: '品质服务', desc: '专业培训的服务团队，从迎宾到送客，每个细节都体现我们的用心与诚意' },
  { icon: Landmark, title: '文化传承', desc: '融合川蜀文化元素的国潮风装潢，让您在品味美食的同时，感受巴蜀文化的魅力' }
]
</script>

<style scoped lang="scss">
$red: #c0392b;
$gold: #d4a017;
$dark: #1a1a2e;

.about-page { background: #faf7f4; }

.page-banner {
  background: linear-gradient(135deg, $dark, $red); padding: 120px 20px 50px; text-align: center; color: #fff;
  h1 { font-size: 36px; font-weight: 800; letter-spacing: 4px; margin-bottom: 10px; }
  p { color: rgba(255,255,255,0.7); font-size: 15px; letter-spacing: 2px; }
}

.section { max-width: 1100px; margin: 0 auto; padding: 70px 20px; }
.section-header { text-align: center; margin-bottom: 48px; }
.section-tag { color: $gold; font-size: 14px; letter-spacing: 4px; }
.section-header h2, .story-content h2 { font-size: 32px; font-weight: 800; color: $dark; margin: 10px 0; }

/* Story */
.story-section .section-inner { display: flex; gap: 50px; align-items: center; }
.story-content { flex: 1; }
.story-content p { font-size: 15px; color: #666; line-height: 2; margin-top: 16px; }
.story-image { width: 360px; flex-shrink: 0; }
.image-frame { border-radius: 16px; overflow: hidden; border: 3px solid rgba(212,160,23,0.3); }
.story-photo {
  width: 100%; height: 300px; object-fit: cover; display: block;
  transition: transform 0.5s;
  &:hover { transform: scale(1.05); }
}

/* Advantages */
.advantages-section { background: #fff; max-width: 100%; margin: 0; padding: 70px 20px; }
.adv-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; max-width: 1100px; margin: 0 auto; }
.adv-card {
  text-align: center; padding: 36px 20px; border-radius: 16px;
  background: #faf7f4; transition: all 0.3s;
  &:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(192,57,43,0.08); }
}
.adv-icon { font-size: 44px; margin-bottom: 14px; }
.adv-card h3 { font-size: 18px; font-weight: 700; color: $dark; margin-bottom: 10px; }
.adv-card p { font-size: 14px; color: #888; line-height: 1.7; }

/* Contact */
.contact-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.contact-card {
  background: #fff; border-radius: 16px; padding: 32px 20px; text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); transition: all 0.3s;
  &:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(192,57,43,0.1); }
}
.contact-icon { font-size: 40px; margin-bottom: 14px; }
.contact-card h3 { font-size: 16px; font-weight: 700; color: $dark; margin-bottom: 10px; }
.contact-card p { font-size: 14px; color: #555; line-height: 1.8; }
.sub-text { font-size: 12px !important; color: #aaa !important; margin-top: 6px; }

/* CTA */
.cta-section {
  background: linear-gradient(135deg, $red, #a93226); padding: 70px 20px; text-align: center;
  h2 { color: #fff; font-size: 30px; font-weight: 800; margin-bottom: 10px; }
  p { color: rgba(255,255,255,0.7); font-size: 16px; margin-bottom: 28px; }
}
.cta-actions { display: flex; gap: 16px; justify-content: center; }
.btn-gold {
  background: linear-gradient(135deg, $gold, #b8860b) !important;
  color: #fff !important; border: none !important;
  &:hover { opacity: 0.9; }
}

@media (max-width: 768px) {
  .story-section .section-inner { flex-direction: column; }
  .story-image { width: 100%; }
  .adv-grid, .contact-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>

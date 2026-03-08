<template>
  <div class="home-page">
    <!-- Hero Banner -->
    <section class="hero">
      <div class="hero-overlay" />
      <div class="hero-content">
        <h1 class="hero-title">川味红锅</h1>
        <p class="hero-slogan">传承经典 · 匠心煮义 · 百年川味</p>
        <p class="hero-desc">精选上等食材，古法熬制锅底，还原地道川渝火锅味道</p>
        <div class="hero-actions">
          <el-button type="danger" size="large" round @click="$router.push('/menu')">浏览菜单</el-button>
          <el-button size="large" round class="btn-gold" @click="$router.push('/reservation')">立即预订</el-button>
        </div>
      </div>
      <div class="hero-scroll-hint">
        <el-icon class="bounce"><ArrowDown /></el-icon>
      </div>
    </section>

    <!-- Featured Dishes -->
    <section class="section featured">
      <div class="section-header">
        <span class="section-tag">— 招牌推荐 —</span>
        <h2 class="section-title">镇店之宝</h2>
        <p class="section-subtitle">主厨精心甄选，食客口碑之选</p>
      </div>
      <div class="dish-grid">
        <div v-for="dish in featuredDishes" :key="dish.id" class="dish-card" @click="$router.push('/menu')">
          <div class="dish-img-wrap">
            <img :src="dish.image || defaultImg" :alt="dish.name" />
            <span class="dish-badge">推荐</span>
          </div>
          <div class="dish-info">
            <h3>{{ dish.name }}</h3>
            <p class="dish-desc">{{ dish.description || '经典川味，回味无穷' }}</p>
            <div class="dish-meta">
              <span class="dish-price">¥{{ dish.price }}</span>
              <span class="dish-spicy">
                <Flame v-for="n in (dish.spicyLevel || 0)" :key="n" :size="14" color="#e74c3c" />
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="section features">
      <div class="section-header">
        <span class="section-tag">— 我们的优势 —</span>
        <h2 class="section-title">匠心品质</h2>
      </div>
      <div class="features-grid">
        <div v-for="f in features" :key="f.title" class="feature-card">
          <div class="feature-icon"><component :is="f.icon" :size="48" :stroke-width="1.5" /></div>
          <h3>{{ f.title }}</h3>
          <p>{{ f.desc }}</p>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta-section">
      <div class="cta-content">
        <h2>今晚，来一顿地道的川味火锅？</h2>
        <p>提前预订，尊享专属座位</p>
        <el-button type="danger" size="large" round @click="$router.push('/reservation')">在线预订座位</el-button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, type Component } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { Leaf, Flame, Lamp, Crown } from 'lucide-vue-next'
import { getDishes } from '@/api'

const defaultImg = 'https://img.freepik.com/free-photo/top-view-table-full-delicious-food-composition_23-2149141352.jpg'

const featuredDishes = ref<any[]>([])

const features: { icon: Component; title: string; desc: string }[] = [
  { icon: Leaf, title: '新鲜食材', desc: '每日清晨直采，从农场到餐桌不超过6小时，锁住最原始的鲜味' },
  { icon: Flame, title: '古法锅底', desc: '传承三代秘方，精选十余种香料，文火慢熬8小时浓缩精华' },
  { icon: Lamp, title: '国潮雅座', desc: '融合川蜀文化元素，营造温馨舒适的就餐氛围，宾至如归' },
  { icon: Crown, title: '会员尊享', desc: '注册即享会员权益，积分兑换、生日特权、专属折扣等你来' }
]

onMounted(async () => {
  try {
    const res = await getDishes({ recommend: true, size: 6 })
    featuredDishes.value = res.data?.records || res.data || []
  } catch { /* ignore */ }
})
</script>

<style scoped lang="scss">
$red: #c0392b;
$gold: #d4a017;
$dark: #1a1a2e;

.home-page { color: #333; }

/* Hero */
.hero {
  position: relative; height: 100vh; min-height: 600px;
  display: flex; align-items: center; justify-content: center;
  background: url('https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=1920') center/cover no-repeat;
  overflow: hidden;
}
.hero-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, rgba(26,26,46,0.85) 0%, rgba(192,57,43,0.6) 100%);
}
.hero-content { position: relative; text-align: center; color: #fff; padding: 0 20px; }
.hero-title {
  font-size: 72px; font-weight: 900; letter-spacing: 12px;
  background: linear-gradient(135deg, #fff 30%, $gold);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  text-shadow: none; margin-bottom: 16px;
}
.hero-slogan { font-size: 20px; letter-spacing: 6px; color: $gold; margin-bottom: 12px; }
.hero-desc { font-size: 16px; color: rgba(255,255,255,0.7); margin-bottom: 36px; }
.hero-actions { display: flex; gap: 16px; justify-content: center; }
.btn-gold {
  background: linear-gradient(135deg, $gold, #b8860b) !important;
  color: #fff !important; border: none !important;
  &:hover { opacity: 0.9; }
}
.hero-scroll-hint {
  position: absolute; bottom: 40px; left: 50%; transform: translateX(-50%);
  color: rgba(255,255,255,0.5); font-size: 28px;
}
.bounce { animation: bounceAnim 2s infinite; }
@keyframes bounceAnim {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(12px); }
}

/* Sections */
.section { max-width: 1200px; margin: 0 auto; padding: 80px 20px; }
.section-header { text-align: center; margin-bottom: 50px; }
.section-tag { color: $gold; font-size: 14px; letter-spacing: 4px; }
.section-title { font-size: 36px; font-weight: 800; color: $dark; margin: 10px 0; }
.section-subtitle { color: #888; font-size: 15px; }

/* Dish Grid */
.dish-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 28px; }
.dish-card {
  background: #fff; border-radius: 16px; overflow: hidden; cursor: pointer;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06); transition: all 0.3s;
  &:hover { transform: translateY(-6px); box-shadow: 0 12px 32px rgba(192,57,43,0.12); }
}
.dish-img-wrap {
  position: relative; height: 200px; overflow: hidden;
  img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
  .dish-card:hover & img { transform: scale(1.08); }
}
.dish-badge {
  position: absolute; top: 12px; right: 12px;
  background: linear-gradient(135deg, $gold, #b8860b); color: #fff;
  padding: 4px 14px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.dish-info { padding: 18px 20px; }
.dish-info h3 { font-size: 18px; font-weight: 700; color: $dark; margin-bottom: 6px; }
.dish-desc { font-size: 13px; color: #999; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.dish-meta { display: flex; align-items: center; justify-content: space-between; }
.dish-price { font-size: 22px; font-weight: 800; color: $red; }
.dish-spicy { font-size: 14px; }

/* Features */
.features { background: #fdf6f0; margin: 0; max-width: 100%; padding: 80px 20px; }
.features-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 28px; max-width: 1200px; margin: 0 auto; }
.feature-card {
  background: #fff; border-radius: 16px; padding: 40px 24px; text-align: center;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04); transition: all 0.3s;
  &:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(192,57,43,0.1); }
}
.feature-icon { font-size: 48px; margin-bottom: 16px; }
.feature-card h3 { font-size: 18px; font-weight: 700; color: $dark; margin-bottom: 10px; }
.feature-card p { font-size: 14px; color: #888; line-height: 1.7; }

/* CTA */
.cta-section {
  background: linear-gradient(135deg, $red, #a93226); padding: 80px 20px; text-align: center;
}
.cta-content h2 { color: #fff; font-size: 32px; font-weight: 800; margin-bottom: 12px; }
.cta-content p { color: rgba(255,255,255,0.7); font-size: 16px; margin-bottom: 28px; }

@media (max-width: 768px) {
  .hero-title { font-size: 42px; letter-spacing: 6px; }
  .dish-grid { grid-template-columns: 1fr; }
  .features-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>

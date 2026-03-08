<template>
  <div class="menu-page">
    <div class="page-banner">
      <h1>菜品菜单</h1>
      <p>精选食材 · 地道川味 · 总有一款让你心动</p>
    </div>

    <div class="menu-container">
      <!-- Search -->
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索菜品名称..." size="large" clearable @clear="fetchDishes" @keyup.enter="fetchDishes">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <div class="menu-body">
        <!-- Category Sidebar -->
        <aside class="category-sidebar">
          <div
            v-for="cat in categories" :key="cat.id"
            class="cat-item" :class="{ active: activeCat === cat.id }"
            @click="selectCategory(cat.id)"
          >
            <span class="cat-name">{{ cat.name }}</span>
            <span class="cat-count">{{ cat.dishCount || '' }}</span>
          </div>
        </aside>

        <!-- Dish Grid -->
        <main class="dish-main">
          <div v-if="loading" class="loading-wrap">
            <el-skeleton :rows="4" animated />
          </div>
          <div v-else-if="dishes.length === 0" class="empty-wrap">
            <el-empty description="暂无菜品" />
          </div>
          <div v-else class="dish-grid">
            <div v-for="dish in dishes" :key="dish.id" class="dish-card">
              <div class="dish-img">
                <img :src="dish.image || defaultImg" :alt="dish.name" />
                <span v-if="dish.recommend" class="recommend-badge"><Trophy :size="14" /> 推荐</span>
              </div>
              <div class="dish-body">
                <div class="dish-header">
                  <h3>{{ dish.name }}</h3>
                  <span class="dish-price">¥{{ dish.price }}</span>
                </div>
                <p class="dish-desc">{{ dish.description || '经典川味佳品' }}</p>
                <div class="dish-footer">
                  <span class="spicy-level" :title="`辣度：${dish.spicyLevel || 0}级`">
                    <Flame v-for="n in (dish.spicyLevel || 0)" :key="n" :size="14" color="#e74c3c" />
                    <span v-if="!dish.spicyLevel" class="not-spicy">不辣</span>
                  </span>
                  <el-tag v-if="dish.categoryName" size="small" type="info" effect="plain">{{ dish.categoryName }}</el-tag>
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="total > pageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="page" :page-size="pageSize" :total="total"
              layout="prev, pager, next" background @current-change="fetchDishes"
            />
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { Flame, Trophy } from 'lucide-vue-next'
import { getCategories, getDishes } from '@/api'

const defaultImg = 'https://img.freepik.com/free-photo/top-view-table-full-delicious-food-composition_23-2149141352.jpg'

const categories = ref<any[]>([])
const dishes = ref<any[]>([])
const activeCat = ref<number | null>(null)
const keyword = ref('')
const page = ref(1)
const pageSize = 12
const total = ref(0)
const loading = ref(false)

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = [{ id: null, name: '全部菜品' }, ...(res.data || [])]
  } catch { /* ignore */ }
}

const fetchDishes = async () => {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize }
    if (activeCat.value) params.categoryId = activeCat.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const res = await getDishes(params)
    const d = res.data
    dishes.value = d?.records || d || []
    total.value = d?.total || 0
  } catch { /* ignore */ }
  loading.value = false
}

const selectCategory = (id: number | null) => {
  activeCat.value = id
  page.value = 1
  fetchDishes()
}

onMounted(() => {
  fetchCategories()
  fetchDishes()
})
</script>

<style scoped lang="scss">
$red: #c0392b;
$gold: #d4a017;
$dark: #1a1a2e;

.menu-page { background: #faf7f4; min-height: 100vh; }

.page-banner {
  background: linear-gradient(135deg, $dark 0%, $red 100%);
  padding: 120px 20px 50px; text-align: center; color: #fff;
  h1 { font-size: 36px; font-weight: 800; letter-spacing: 4px; margin-bottom: 10px; }
  p { color: rgba(255,255,255,0.7); font-size: 15px; letter-spacing: 2px; }
}

.menu-container { max-width: 1200px; margin: 0 auto; padding: 30px 20px 60px; }

.search-bar { margin-bottom: 24px; max-width: 480px; }

.menu-body { display: flex; gap: 28px; }

/* Sidebar */
.category-sidebar {
  width: 180px; flex-shrink: 0; background: #fff; border-radius: 12px;
  padding: 12px 0; box-shadow: 0 2px 12px rgba(0,0,0,0.04); align-self: flex-start;
  position: sticky; top: 90px;
}
.cat-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; cursor: pointer; transition: all 0.2s; font-size: 15px; color: #555;
  &:hover { background: #fdf6f0; color: $red; }
  &.active {
    background: linear-gradient(135deg, $red, #e74c3c); color: #fff; font-weight: 600;
    .cat-count { color: rgba(255,255,255,0.8); }
  }
}
.cat-count { font-size: 12px; color: #bbb; }

/* Dish Grid */
.dish-main { flex: 1; min-width: 0; }
.dish-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 22px; }
.dish-card {
  background: #fff; border-radius: 14px; overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); transition: all 0.3s;
  &:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(192,57,43,0.1); }
}
.dish-img {
  position: relative; height: 180px; overflow: hidden;
  img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s; }
  .dish-card:hover & img { transform: scale(1.06); }
}
.recommend-badge {
  position: absolute; top: 10px; left: 10px;
  background: linear-gradient(135deg, $gold, #b8860b); color: #fff;
  padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.dish-body { padding: 16px; }
.dish-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }
.dish-header h3 { font-size: 16px; font-weight: 700; color: $dark; flex: 1; margin-right: 8px; }
.dish-price { font-size: 20px; font-weight: 800; color: $red; white-space: nowrap; }
.dish-desc { font-size: 13px; color: #999; margin-bottom: 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.dish-footer { display: flex; align-items: center; justify-content: space-between; }
.spicy-level { font-size: 14px; }
.not-spicy { color: #ccc; font-size: 13px; }

.loading-wrap, .empty-wrap { padding: 60px 0; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }

@media (max-width: 768px) {
  .menu-body { flex-direction: column; }
  .category-sidebar { width: 100%; position: static; display: flex; overflow-x: auto; padding: 8px; gap: 8px; }
  .cat-item { white-space: nowrap; padding: 10px 16px; border-radius: 8px; }
  .dish-grid { grid-template-columns: 1fr; }
}
</style>

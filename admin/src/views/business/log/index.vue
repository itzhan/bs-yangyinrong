<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operatorName" placeholder="请输入操作人" clearable />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="searchForm.module" placeholder="请输入模块" clearable />
        </el-form-item>
        <el-form-item label="操作">
          <el-input v-model="searchForm.action" placeholder="请输入操作" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="module" label="模块" width="110">
          <template #default="{ row }">
            <el-tag>{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="120" />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="createdAt" label="操作时间" width="170" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { fetchLogList } from '@/api/business'

const loading = ref(false)
const tableData = ref<any[]>([])
const searchForm = reactive({ operatorName: '', module: '', action: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })

async function loadData() {
  loading.value = true
  try {
    const { records, total } = await fetchLogList({ ...searchForm, page: pagination.page, size: pagination.size })
    tableData.value = records
    pagination.total = total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.operatorName = ''
  searchForm.module = ''
  searchForm.action = ''
  pagination.page = 1
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

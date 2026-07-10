<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

interface SalaryItem {
  id: number; itemName: string; itemType: string
  isActive: number; sortOrder: number; description: string
}

const loading = ref(false)
const tableData = ref<SalaryItem[]>([])
const page = ref(1)
const pageSize = ref(15)
const total = ref(0)
const pagedData = computed(() => {
  total.value = tableData.value.length
  const start = (page.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

async function loadData() {
  loading.value = true
  try { const res = await request.get('/api/salary-item'); tableData.value = res.data.data || [] } finally { loading.value = false }
}

async function toggleActive(row: SalaryItem) {
  try {
    await request.put('/api/salary-item', { ...row, isActive: row.isActive === 1 ? 0 : 1 })
    ElMessage.success(row.isActive === 1 ? '已停用' : '已启用')
    loadData()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '操作失败') }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <el-alert type="warning" show-icon :closable="false" style="margin-bottom: 12px; flex-shrink: 0;" title="系统自动计算项（SYSTEM）禁止手动修改；手动录入项（MANUAL）可在月度工资中修改金额" />
    <el-card shadow="never" style="border: none;">
      <div style="flex: 1; overflow: auto;">
        <el-table :data="pagedData" v-loading="loading" border stripe>
        <el-table-column prop="sortOrder" label="序号" min-width="60" align="center" />
        <el-table-column label="工资项名称" min-width="120" align="center">
          <template #default="{ row }">
            <span class="cfg-name">{{ row.itemName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" min-width="140" align="center">
          <template #default="{ row }">
            <span :class="row.itemType === 'SYSTEM' ? 'cfg-type-sys' : 'cfg-type-manual'">{{ row.itemType === 'SYSTEM' ? '系统自动' : '手动录入' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80" align="center">
          <template #default="{ row }">
            <el-switch :model-value="row.isActive === 1" @change="toggleActive(row)" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" align="center" show-overflow-tooltip />
      </el-table>
      </div>
      <div style="display: flex; justify-content: center; padding: 14px 0 4px; flex-shrink: 0;">
        <el-pagination :current-page="page" :page-size="pageSize" :total="total" :page-sizes="[10, 15, 20, 50]" layout="total, sizes, prev, pager, next, jumper" @current-change="page = $event" @size-change="pageSize = $event; page = 1" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page-container { height: calc(100vh - 100px); display: flex; flex-direction: column; }
.page-container :deep(.el-card) { flex: 1; display: flex; flex-direction: column; }
.page-container :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; overflow: hidden; }

.cfg-name { color: #1e4f8a; font-weight: 700; }
.cfg-type-sys { color: #1a3a6c; font-weight: 600; }
.cfg-type-manual { color: #909399; font-weight: 600; }
</style>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

interface SalaryItem {
  id: number; itemName: string; itemCode: string; itemType: string
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
    <el-card shadow="hover">
      <template #header>
        <span style="font-weight: 600">工资项配置列表</span>
        <el-alert type="warning" show-icon :closable="false" style="margin-top: 10px" title="注意：系统自动计算项（SYSTEM）禁止手动修改金额，仅可启用/停用；手动录入项（MANUAL）可在月度工资中修改金额" />
      </template>
      <div style="flex: 1; overflow: auto;">
        <el-table :data="pagedData" v-loading="loading" border stripe>
        <el-table-column prop="sortOrder" label="序号" width="60" align="center" />
        <el-table-column prop="itemName" label="工资项名称" width="130" />
        <el-table-column prop="itemCode" label="编码" width="160" />
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.itemType === 'SYSTEM' ? 'primary' : 'success'" size="small">
              {{ row.itemType === 'SYSTEM' ? '系统自动计算' : '手动录入' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isActive === 1"
              @change="toggleActive(row)"
              active-text="启用" inactive-text="停用"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
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
</style>

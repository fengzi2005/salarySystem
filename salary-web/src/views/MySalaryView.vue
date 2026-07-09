<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'

const authStore = useAuthStore()
const loading = ref(false)
const tableData = ref<any[]>([])
const page = ref(1)
const pageSize = ref(15)
const total = ref(0)
const pagedData = computed(() => {
  total.value = tableData.value.length
  const start = (page.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})
const year = ref(new Date().getFullYear())
const month = ref<number | null>(null)

async function loadData() {
  if (!authStore.employeeId) return
  loading.value = true
  try {
    const res = await request.get('/api/salary/my', {
      params: { year: year.value, month: month.value }
    })
    tableData.value = res.data.data || []
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="toolbar">
      <h3 style="margin: 0; color: #1a3a6c">我的工资明细</h3>
      <div style="margin-left: auto; display: flex; align-items: center; gap: 8px">
        <el-input-number v-model="year" :min="2020" :max="2099" style="width: 110px" />
        <span>年</span>
        <el-select v-model="month" placeholder="全部月份" clearable style="width: 100px">
          <el-option v-for="m in 12" :key="m" :label="`${m}月`" :value="m" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      </div>
    </div>

    <el-card shadow="hover">
      <div style="flex: 1; overflow: auto;">
        <el-table :data="pagedData" v-loading="loading" border stripe :empty-text="authStore.employeeId ? '暂无工资数据' : '当前账号未关联员工信息'">
        <el-table-column prop="salaryYear" label="年份" width="80" />
        <el-table-column prop="salaryMonth" label="月份" width="60" />
        <el-table-column prop="baseSalary" label="基本工资" width="100" align="right" />
        <el-table-column prop="positionSalary" label="岗位工资" width="100" align="right" />
        <el-table-column prop="titleSalary" label="职级工资" width="100" align="right" />
        <el-table-column prop="seniorityPay" label="工龄工资" width="100" align="right" />
        <el-table-column prop="fullAttendanceBonus" label="全勤奖" width="90" align="right" />
        <el-table-column prop="projectBonus" label="项目奖金" width="90" align="right" />
        <el-table-column prop="leaveDeduction" label="事假扣款" width="90" align="right" />
        <el-table-column prop="lateDeduction" label="迟到扣款" width="90" align="right" />
        <el-table-column prop="absentDeduction" label="旷工扣款" width="90" align="right" />
        <el-table-column prop="taxDeduction" label="个税扣除" width="90" align="right" />
        <el-table-column label="应发合计" width="110" align="right">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #409eff">{{ row.grossSalary }}</span>
          </template>
        </el-table-column>
        <el-table-column label="扣款合计" width="110" align="right">
          <template #default="{ row }">
            <span style="color: #f56c6c">{{ row.totalDeduction }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实发工资" width="120" align="right">
          <template #default="{ row }">
            <span style="font-weight: 700; font-size: 16px; color: #67c23a">¥{{ row.netSalary }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'CONFIRMED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'CONFIRMED' ? '已确认' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
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
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-shrink: 0; }
</style>

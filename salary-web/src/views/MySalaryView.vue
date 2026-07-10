<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'

const authStore = useAuthStore()
const loading = ref(false)
const tableData = ref<any[]>([])
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const pagedData = computed(() => {
  total.value = tableData.value.length
  const start = (page.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

const now = new Date()
const searchMonth = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)

const myDetailItems = [
  { label: '基本工资', key: 'base_salary', cls: 'card-value' },
  { label: '岗位工资', key: 'position_salary', cls: 'card-value' },
  { label: '职级工资', key: 'title_salary', cls: 'card-value' },
  { label: '工龄工资', key: 'seniority_pay', cls: 'card-value' },
  { label: '全勤奖金', key: 'full_attendance_bonus', cls: 'card-bonus' },
  { label: '项目奖金', key: 'project_bonus', cls: 'card-bonus' },
  { label: '其他加项', key: 'other_additions', cls: 'card-value' },
  { label: '事假扣款', key: 'leave_deduction', cls: 'card-deduct' },
  { label: '迟到扣款', key: 'late_deduction', cls: 'card-deduct' },
  { label: '旷工扣款', key: 'absent_deduction', cls: 'card-deduct' },
  { label: '个税扣除', key: 'tax_deduction', cls: 'card-deduct' },
  { label: '其他减项', key: 'other_deductions', cls: 'card-deduct' },
]

async function loadData() {
  if (!authStore.employeeId) return
  loading.value = true
  try {
    const [y, m] = searchMonth.value.split('-').map(Number)
    const res = await request.get('/api/salary/my', { params: { year: y, month: m } })
    tableData.value = res.data.data || []
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="toolbar">
      <span class="page-title">我的工资明细</span>
      <div style="margin-left: auto; display: flex; align-items: center; gap: 8px">
        <el-date-picker v-model="searchMonth" type="month" format="YYYY年M月" value-format="YYYY-MM" style="width: 160px" @change="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
    </div>

    <div class="card-list" v-loading="loading">
      <div v-if="pagedData.length === 0 && !loading" style="text-align: center; color: #909399; padding: 80px 0; font-size: 15px;">
        {{ authStore.employeeId ? '暂无工资数据' : '当前账号未关联员工信息' }}
      </div>
      <div v-for="row in pagedData" :key="row.id" class="salary-card">
        <div class="card-head">
          <span class="card-month">{{ row.salary_year }}年{{ row.salary_month }}月</span>
          <span :class="row.status === 'CONFIRMED' ? 'my-confirmed' : 'my-draft'">{{ row.status === 'CONFIRMED' ? '已确认' : '草稿' }}</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="12" v-for="item in myDetailItems" :key="item.label" class="card-item">
            <span class="card-label">{{ item.label }}</span>
            <span :class="item.cls">¥{{ (row[item.key] || 0) }}</span>
          </el-col>
        </el-row>
        <div class="card-footer">
          <div class="card-sum gross"><span>应发合计</span><span>¥{{ row.gross_salary }}</span></div>
          <div class="card-sum deduct"><span>扣款合计</span><span>¥{{ row.total_deduction }}</span></div>
          <div class="card-sum net"><span>实发工资</span><span>¥{{ row.net_salary }}</span></div>
        </div>
      </div>
    </div>

    <div style="display: flex; justify-content: center; padding: 14px 0 4px; flex-shrink: 0;" v-if="pagedData.length > 0">
      <el-pagination :current-page="page" :page-size="pageSize" :total="total" :page-sizes="[10, 12, 20, 50]" layout="total, sizes, prev, pager, next" @current-change="page = $event" @size-change="pageSize = $event; page = 1" />
    </div>
  </div>
</template>

<style scoped>
.page-container { height: calc(100vh - 100px); display: flex; flex-direction: column; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-shrink: 0; }
.page-title { font-size: 20px; font-weight: 700; color: #1a3a6c; }

.card-list { flex: 1; overflow-y: auto; }

.salary-card {
  background: #fff;
  border-radius: 10px;
  padding: 18px 20px 14px;
  margin-bottom: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.card-head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px; padding-bottom: 10px;
  border-bottom: 2px solid #e8ecf1;
}
.card-month { font-size: 17px; font-weight: 700; color: #1a3a6c; }

.card-item {
  display: flex; align-items: center; gap: 12px;
  padding: 7px 10px; margin: 2px 0;
  background: #fafbfc; border-radius: 6px;
}
.card-label { color: #606266; font-size: 14px; min-width: 70px; }

.card-value { color: #303133; font-weight: 500; font-size: 14px; }
.card-bonus { color: #67c23a; font-weight: 600; font-size: 14px; }
.card-deduct { color: #f56c6c; font-weight: 600; font-size: 14px; }

.card-footer { display: flex; gap: 10px; margin-top: 12px; }
.card-sum { flex: 1; text-align: center; padding: 12px 6px; border-radius: 8px; display: flex; flex-direction: column; gap: 4px; font-size: 13px; color: #909399; }
.card-sum span:last-child { font-size: 17px; font-weight: 700; }
.card-sum.gross { background: #ecf5ff; } .card-sum.gross span:last-child { color: #409eff; }
.card-sum.deduct { background: #fef0f0; } .card-sum.deduct span:last-child { color: #f56c6c; }
.card-sum.net { background: #f0f9eb; } .card-sum.net span:last-child { color: #67c23a; font-size: 19px; }

.my-confirmed { color: #67c23a; font-weight: 600; }
.my-draft { color: #e6a23c; font-weight: 600; }
</style>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'
import * as echarts from 'echarts'

const authStore = useAuthStore()
const loading = ref(false)
const tableData = ref<any[]>([])
const scope = ref('self')
const selfEmployeeId = ref<number | null>(null)
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

async function loadData(warn = false) {
  if (!searchMonth.value) { if (warn) ElMessage.warning('请选择年份和月份'); return }
  if (!authStore.employeeId) return
  loading.value = true
  try {
    const [y, m] = searchMonth.value.split('-').map(Number)
    const res = await request.get('/api/salary/my', { params: { year: y, month: m } })
    const data = res.data.data
    scope.value = data.scope || 'self'
    selfEmployeeId.value = data.selfEmployeeId || null
    let records = data.records || []
    // 管理人员：自己置顶
    if (scope.value === 'department' && selfEmployeeId.value) {
      records = [
        ...records.filter((r: any) => r.employee_id === selfEmployeeId.value),
        ...records.filter((r: any) => r.employee_id !== selfEmployeeId.value)
      ]
    }
    tableData.value = records
    await nextTick()
    if (records.length > 0) renderPieChart(records[0])
  } finally { loading.value = false }
}

function renderPieChart(row: any) {
  const el = document.getElementById('chart-my-pie')
  if (!el) return
  const chart = echarts.init(el)
  const pieData = myDetailItems
    .filter(item => Number(row[item.key] || 0) > 0)
    .map(item => ({ name: item.label, value: Number(row[item.key] || 0) }))
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { fontSize: 16 } },
    series: [{
      type: 'pie', radius: ['45%', '75%'], center: ['35%', '50%'],
      data: pieData,
      label: { fontSize: 14, formatter: '{b}\n¥{c}' }
    }]
  })
}

onMounted(() => loadData())
</script>

<template>
  <div class="page-container">
    <div class="toolbar">
      <span class="page-title">{{ scope === 'self' ? '我的薪资明细' : scope === 'department' ? '本部门薪资' : '全校薪资' }}</span>
      <div style="margin-left: auto; display: flex; align-items: center; gap: 8px">
        <el-date-picker v-model="searchMonth" type="month" format="YYYY年M月" value-format="YYYY-MM" clearable style="width: 160px" @change="loadData()" @clear="tableData = []" />
        <el-button type="primary" @click="loadData(true)">查询</el-button>
      </div>
    </div>

    <div class="card-list" v-loading="loading">
      <div v-if="tableData.length === 0 && !loading" style="text-align: center; color: #909399; padding: 80px 0; font-size: 15px;">
        {{ authStore.employeeId ? '暂无工资数据' : '当前账号未关联员工信息' }}
      </div>
      <div v-for="row in tableData" :key="row.id" class="salary-card" :class="{ 'self-card': row.employee_id === selfEmployeeId }">
        <div class="card-head">
          <span class="card-month">{{ row.employee_name || '' }} {{ row.salary_year }}年{{ row.salary_month }}月</span>
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
      <div v-if="tableData.length > 0" class="pie-card">
        <div class="pie-title">{{ tableData[0]?.salary_year }}年{{ tableData[0]?.salary_month }}月 薪资构成</div>
        <div id="chart-my-pie" style="height: 400px;"></div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.page-container { height: calc(100vh - 100px); display: flex; flex-direction: column; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-shrink: 0; }
.page-title { font-size: 22px; font-weight: 700; color: #1a3a6c; }

.card-list { flex: 1; overflow-y: auto; }

.salary-card {
  background: #fff;
  border-radius: 12px;
  padding: 22px 24px 18px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.card-head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px; padding-bottom: 12px;
  border-bottom: 2px solid #e8ecf1;
}
.card-month { font-size: 20px; font-weight: 700; color: #1a3a6c; }

.card-item {
  display: flex; align-items: center; gap: 16px;
  padding: 10px 14px; margin: 3px 0;
  background: #fafbfc; border-radius: 8px;
}
.card-label { color: #606266; font-size: 16px; min-width: 80px; }

.card-value { color: #303133; font-weight: 500; font-size: 16px; }
.card-bonus { color: #67c23a; font-weight: 600; font-size: 16px; }
.card-deduct { color: #f56c6c; font-weight: 600; font-size: 16px; }

.card-footer { display: flex; gap: 14px; margin-top: 16px; }
.card-sum { flex: 1; text-align: center; padding: 16px 8px; border-radius: 10px; display: flex; flex-direction: column; gap: 6px; font-size: 15px; color: #909399; }
.card-sum span:last-child { font-size: 20px; font-weight: 700; }
.card-sum.gross { background: #ecf5ff; } .card-sum.gross span:last-child { color: #409eff; }
.card-sum.deduct { background: #fef0f0; } .card-sum.deduct span:last-child { color: #f56c6c; }
.card-sum.net { background: #f0f9eb; } .card-sum.net span:last-child { color: #67c23a; font-size: 22px; }

.my-confirmed { color: #67c23a; font-weight: 600; }
.my-draft { color: #e6a23c; font-weight: 600; }

.self-card {
  border: 2px solid #1e4f8a;
  background: #f8fafd;
}

.pie-card {
  background: #fff; border-radius: 12px; padding: 22px 24px;
  margin-top: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.pie-title { font-size: 18px; font-weight: 700; color: #1a3a6c; margin-bottom: 12px; }
</style>

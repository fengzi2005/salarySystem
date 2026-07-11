<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const loading = ref(false)
function onTabChange(tab: any) {
  if (tab === 'structure') loadStructure()
  if (tab === 'stats') loadStats()
}

const activeTab = ref('structure')
const now = new Date()
const reportMonth = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)
const statsMonth = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)
const annualYear = ref<any>(null)

const structureData = ref<any[]>([])
const statsData = ref<any[]>([])
const employeeId = ref<number | null>(null)
const annualData = ref<any[]>([])
const employeeOptions = ref<any[]>([])

async function loadStructure(warn = false) {
  if (!reportMonth.value) { structureData.value = []; if (chart1) { chart1.dispose(); chart1 = null; } if (warn) ElMessage.warning('请选择月份'); return }
  loading.value = true
  try {
    const [y, m] = reportMonth.value.split('-').map(Number)
    const res = await request.get('/api/salary/report/structure', { params: { year: y, month: m } })
    structureData.value = res.data.data || []
    await nextTick()
    renderStructureChart()
  } finally { loading.value = false }
}

async function loadStats(warn = false) {
  if (!statsMonth.value) { statsData.value = []; if (chart2) { chart2.dispose(); chart2 = null; } if (warn) ElMessage.warning('请选择月份'); return }
  loading.value = true
  try {
    const [y, m] = statsMonth.value.split('-').map(Number)
    const res = await request.get('/api/salary/report/dept-stats', { params: { year: y, month: m } })
    statsData.value = res.data.data || []
    await nextTick()
    renderStatsChart()
  } finally { loading.value = false }
}

async function loadAnnual(warn = false) {
  if (!employeeId.value || !annualYear.value) {
    annualData.value = []
    if (chart3) { chart3.dispose(); chart3 = null }
    if (warn) {
      if (!employeeId.value && !annualYear.value) ElMessage.warning('请选择员工和年份')
      else if (!employeeId.value) ElMessage.warning('请选择员工')
      else ElMessage.warning('请选择年份')
    }
    return
  }
  loading.value = true
  try {
    // 查询年度汇总
    const res = await request.get(`/api/salary/report/annual/${employeeId.value}`, { params: { year: Number(annualYear.value) } })
    annualData.value = res.data.data || []
    // 查询每月明细用于折线图
    const monthly = await request.get('/api/salary/employee/' + employeeId.value, { params: { year: Number(annualYear.value) } })
    await nextTick()
    renderAnnualChart(monthly.data.data || [])
  } finally { loading.value = false }
}

let chart1: any, chart2: any, chart3: any

function renderStructureChart() {
  const el = document.getElementById('chart-structure')
  if (!el || structureData.value.length === 0) return
  if (chart1) chart1.dispose()
  chart1 = echarts.init(el)
  chart1.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { fontSize: 15 } },
    series: [{
      type: 'pie', radius: ['45%', '75%'], center: ['35%', '50%'],
      data: structureData.value.map((d: any) => ({ name: d.dept_name, value: Number(d.total_net_salary) })),
      label: { formatter: '{b}\n{d}%' }
    }]
  })
}

function renderStatsChart() {
  const el = document.getElementById('chart-stats')
  if (!el || statsData.value.length === 0) return
  if (chart2) chart2.dispose()
  chart2 = echarts.init(el)
  chart2.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均工资', '最高工资', '最低工资'], bottom: 0, textStyle: { fontSize: 13 }, padding: [10, 0, 0, 0] },
    grid: { left: 40, right: 20, top: 10, bottom: 60 },
    xAxis: { type: 'category', data: statsData.value.map((d: any) => d.dept_name), axisLabel: { fontSize: 11, rotate: 20 } },
    yAxis: { type: 'value', axisLabel: { fontSize: 11 } },
    series: [
      { type: 'bar', name: '平均工资', data: statsData.value.map((d: any) => Number(d.avg_salary)), color: '#1a3a6c', barGap: '10%' },
      { type: 'bar', name: '最高工资', data: statsData.value.map((d: any) => Number(d.max_salary)), color: '#3b7dd8' },
      { type: 'bar', name: '最低工资', data: statsData.value.map((d: any) => Number(d.min_salary)), color: '#7eb8da' }
    ]
  })
}

function renderAnnualChart(monthly: any[]) {
  const el = document.getElementById('chart-annual')
  if (!el) return
  if (chart3) chart3.dispose()
  chart3 = echarts.init(el)
  const data = new Array(12).fill(null)
  monthly.forEach((m: any) => {
    const idx = (m.salary_month || 1) - 1
    data[idx] = Number(m.net_salary)
  })
  chart3.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'line', data, smooth: true,
      lineStyle: { color: '#4a90d9' },
      itemStyle: { color: '#2c5fa1' }
    }]
  })
}

onMounted(async () => {
  const empRes = await request.get('/api/employee/active')
  employeeOptions.value = empRes.data.data || []
  loadStructure()
})
</script>

<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" type="card" @tab-change="onTabChange">
      <!-- 薪资结构分析 -->
      <el-tab-pane label="薪资结构分析" name="structure">
        <div class="toolbar">
          <el-date-picker v-model="reportMonth" type="month" format="YYYY年M月" value-format="YYYY-MM" style="width: 150px" clearable @change="loadStructure()" @clear="structureData = []; if (chart1) { chart1.dispose(); chart1 = null }" />
          <el-button type="primary" :icon="Search" @click="loadStructure(true)" style="margin-left: 8px">查询</el-button>
        </div>
        <el-table :data="structureData" v-loading="loading" border stripe style="width: 100%">
          <el-table-column label="部门" min-width="120" align="center" show-overflow-tooltip>
            <template #default="{ row }"><span class="rpt-dept">{{ row.dept_name }}</span></template>
          </el-table-column>
          <el-table-column prop="employee_count" label="人数" min-width="60" align="center" />
          <el-table-column label="应发合计" min-width="130" align="center">
            <template #default="{ row }"><span class="rpt-gross">¥{{ row.total_gross_salary }}</span></template>
          </el-table-column>
          <el-table-column label="实发合计" min-width="130" align="center">
            <template #default="{ row }"><span class="rpt-net">¥{{ row.total_net_salary }}</span></template>
          </el-table-column>
          <el-table-column label="占比" min-width="70" align="center">
            <template #default="{ row }">{{ row.salary_ratio }}%</template>
          </el-table-column>
        </el-table>
        <div id="chart-structure" style="height: 350px; margin-top: 20px;"></div>
      </el-tab-pane>

      <!-- 部门薪资统计 -->
      <el-tab-pane label="部门薪资统计" name="stats">
        <div class="toolbar">
          <el-date-picker v-model="statsMonth" type="month" format="YYYY年M月" value-format="YYYY-MM" style="width: 150px" clearable @change="loadStats()" @clear="statsData = []; if (chart2) { chart2.dispose(); chart2 = null }" />
          <el-button type="primary" :icon="Search" @click="loadStats" style="margin-left: 8px">查询</el-button>
        </div>
        <el-table :data="statsData" v-loading="loading" border stripe style="width: 100%">
          <el-table-column label="部门" min-width="120" align="center">
            <template #default="{ row }"><span class="rpt-dept">{{ row.dept_name }}</span></template>
          </el-table-column>
          <el-table-column prop="employee_count" label="人数" width="60" align="center" />
          <el-table-column label="最高" min-width="100" align="center">
            <template #default="{ row }"><span class="rpt-warn">¥{{ row.max_salary }}</span></template>
          </el-table-column>
          <el-table-column label="最低" min-width="100" align="center">
            <template #default="{ row }"><span>¥{{ row.min_salary }}</span></template>
          </el-table-column>
          <el-table-column label="平均" min-width="100" align="center">
            <template #default="{ row }"><span class="rpt-avg">¥{{ row.avg_salary }}</span></template>
          </el-table-column>
          <el-table-column label="总额" min-width="110" align="center">
            <template #default="{ row }"><span class="rpt-total">¥{{ row.total_salary }}</span></template>
          </el-table-column>
        </el-table>
        <div id="chart-stats" style="height: 350px; margin-top: 20px;"></div>
      </el-tab-pane>

      <!-- 员工年度统计 -->
      <el-tab-pane label="员工年度统计" name="annual">
        <div class="toolbar">
          <el-select v-model="employeeId" placeholder="选择员工" filterable style="width: 200px" clearable @change="loadAnnual()" @clear="annualData = []">
            <el-option v-for="e in employeeOptions" :key="e.id" :label="`${e.emp_no} - ${e.name}`" :value="e.id" />
          </el-select>
          <el-date-picker v-model="annualYear" type="year" format="YYYY年" value-format="YYYY" placeholder="选择年份" style="width: 130px; margin-left: 8px" clearable @change="loadAnnual()" @clear="annualData = []" />
          <el-button type="primary" :icon="Search" @click="loadAnnual" style="margin-left: 8px">查询</el-button>
        </div>
        <el-table :data="annualData" v-loading="loading" border stripe style="width: 100%">
          <el-table-column label="姓名" min-width="80" align="center">
            <template #default="{ row }"><span class="rpt-name">{{ row.employee_name }}</span></template>
          </el-table-column>
          <el-table-column label="工号" min-width="100" align="center">
            <template #default="{ row }"><span class="rpt-empno">{{ row.emp_no }}</span></template>
          </el-table-column>
          <el-table-column prop="dept_name" label="部门" min-width="100" align="center" />
          <el-table-column prop="salary_year" label="年份" width="70" align="center" />
          <el-table-column prop="month_count" label="月数" width="60" align="center" />
          <el-table-column label="月均" min-width="100" align="center">
            <template #default="{ row }"><span>¥{{ row.avg_monthly_salary }}</span></template>
          </el-table-column>
          <el-table-column label="最高月" min-width="100" align="center">
            <template #default="{ row }"><span class="rpt-warn">¥{{ row.max_monthly_salary }}</span></template>
          </el-table-column>
          <el-table-column label="最低月" min-width="100" align="center">
            <template #default="{ row }"><span>¥{{ row.min_monthly_salary }}</span></template>
          </el-table-column>
          <el-table-column label="年度总工资" min-width="130" align="center">
            <template #default="{ row }"><span class="rpt-annual">¥{{ row.total_annual_salary }}</span></template>
          </el-table-column>
        </el-table>
        <div id="chart-annual" style="height: 350px; margin-top: 20px;"></div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.page-container { height: calc(100vh - 100px); display: flex; flex-direction: column; }
.page-container :deep(.el-tabs) { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.page-container :deep(.el-tabs__header) { flex-shrink: 0; }
.page-container :deep(.el-tabs__content) { flex: 1; overflow-y: auto; overflow-x: hidden; }
.page-container :deep(.el-tab-pane) { }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-shrink: 0; }

.rpt-dept { color: #1e4f8a; font-weight: 700; }
.rpt-name { color: #1e4f8a; font-weight: 700; }
.rpt-empno { font-weight: 700; color: #303133; }
.rpt-gross { color: #409eff; font-weight: 600; }
.rpt-net { color: #67c23a; font-weight: 700; }
.rpt-warn { color: #e6a23c; font-weight: 600; }
.rpt-avg { color: #1e4f8a; font-weight: 600; }
.rpt-total { color: #409eff; font-weight: 700; font-size: 15px; }
.rpt-annual { color: #67c23a; font-weight: 700; font-size: 16px; }
</style>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const activeTab = ref('structure')
const year = ref(new Date().getFullYear())
const month = ref(new Date().getMonth())

const structureData = ref<any[]>([])
const statsData = ref<any[]>([])
const employeeId = ref<number | null>(null)
const annualYear = ref(new Date().getFullYear())
const annualData = ref<any[]>([])
const employeeOptions = ref<any[]>([])

async function loadStructure() {
  loading.value = true
  try {
    const res = await request.get('/api/salary/report/structure', { params: { year: year.value, month: month.value } })
    structureData.value = res.data.data || []
  } finally { loading.value = false }
}

async function loadStats() {
  loading.value = true
  try {
    const res = await request.get('/api/salary/report/dept-stats', { params: { year: year.value, month: month.value } })
    statsData.value = res.data.data || []
  } finally { loading.value = false }
}

async function loadAnnual() {
  if (!employeeId.value) return
  loading.value = true
  try {
    const res = await request.get(`/api/salary/report/annual/${employeeId.value}`, { params: { year: annualYear.value } })
    annualData.value = res.data.data || []
  } finally { loading.value = false }
}

onMounted(async () => {
  const empRes = await request.get('/api/employee/active')
  employeeOptions.value = empRes.data.data || []
  loadStructure()
})
</script>

<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" type="border-card" @tab-change="(tab: string) => {
      if (tab === 'structure') loadStructure()
      if (tab === 'stats') loadStats()
    }">
      <!-- 薪资结构分析 -->
      <el-tab-pane label="薪资结构分析" name="structure">
        <div class="toolbar">
          <el-input-number v-model="year" :min="2020" :max="2099" style="width: 110px" />
          <span style="margin: 0 4px">年</span>
          <el-input-number v-model="month" :min="1" :max="12" style="width: 80px" />
          <span style="margin: 0 4px">月</span>
          <el-button type="primary" :icon="Search" @click="loadStructure">查询</el-button>
        </div>
        <el-table :data="structureData" v-loading="loading" border stripe>
          <el-table-column prop="deptName" label="部门" width="150" />
          <el-table-column prop="employeeCount" label="人数" width="70" align="center" />
          <el-table-column prop="totalBaseSalary" label="基本工资总额" align="right" />
          <el-table-column prop="totalPositionSalary" label="岗位工资总额" align="right" />
          <el-table-column prop="totalTitleSalary" label="职级工资总额" align="right" />
          <el-table-column prop="totalSeniorityPay" label="工龄工资总额" align="right" />
          <el-table-column prop="totalAttendanceBonus" label="全勤奖总额" align="right" />
          <el-table-column prop="totalProjectBonus" label="项目奖金总额" align="right" />
          <el-table-column prop="totalGrossSalary" label="应发合计" align="right">
            <template #default="{ row }">
              <span style="font-weight: 600">{{ row.totalGrossSalary }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalNetSalary" label="实发合计" align="right">
            <template #default="{ row }">
              <span style="font-weight: 700; color: #67c23a">{{ row.totalNetSalary }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="salaryRatio" label="占比(%)" width="90" align="center" />
        </el-table>
      </el-tab-pane>

      <!-- 部门薪资统计 -->
      <el-tab-pane label="部门薪资统计" name="stats">
        <div class="toolbar">
          <el-input-number v-model="year" :min="2020" :max="2099" style="width: 110px" />
          <span style="margin: 0 4px">年</span>
          <el-input-number v-model="month" :min="1" :max="12" style="width: 80px" />
          <span style="margin: 0 4px">月</span>
          <el-button type="primary" :icon="Search" @click="loadStats">查询</el-button>
        </div>
        <el-table :data="statsData" v-loading="loading" border stripe>
          <el-table-column prop="deptName" label="部门" width="150" />
          <el-table-column prop="employeeCount" label="人数" width="70" align="center" />
          <el-table-column prop="maxSalary" label="最高工资" align="right" />
          <el-table-column prop="minSalary" label="最低工资" align="right" />
          <el-table-column prop="avgSalary" label="平均工资" align="right" />
          <el-table-column prop="totalSalary" label="工资总额" align="right">
            <template #default="{ row }">
              <span style="font-weight: 700; color: #409eff">{{ row.totalSalary }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 员工年度统计 -->
      <el-tab-pane label="员工年度统计" name="annual">
        <div class="toolbar">
          <el-select v-model="employeeId" placeholder="选择员工" filterable style="width: 200px">
            <el-option v-for="e in employeeOptions" :key="e.id" :label="`${e.empNo} - ${e.name}`" :value="e.id" />
          </el-select>
          <el-input-number v-model="annualYear" :min="2020" :max="2099" style="width: 110px; margin-left: 8px" />
          <span style="margin: 0 4px">年</span>
          <el-button type="primary" :icon="Search" @click="loadAnnual">查询</el-button>
        </div>
        <el-table :data="annualData" v-loading="loading" border stripe>
          <el-table-column prop="employeeName" label="姓名" width="100" />
          <el-table-column prop="empNo" label="工号" width="100" />
          <el-table-column prop="deptName" label="部门" width="120" />
          <el-table-column prop="salaryYear" label="年份" width="80" />
          <el-table-column prop="monthCount" label="已发月数" width="90" align="center" />
          <el-table-column prop="avgMonthlySalary" label="月均工资" align="right" />
          <el-table-column prop="maxMonthlySalary" label="最高月工资" align="right" />
          <el-table-column prop="minMonthlySalary" label="最低月工资" align="right" />
          <el-table-column prop="totalAnnualSalary" label="年度总工资" align="right">
            <template #default="{ row }">
              <span style="font-weight: 700; font-size: 15px; color: #67c23a">¥{{ row.totalAnnualSalary }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.page-container { height: calc(100vh - 100px); display: flex; flex-direction: column; }
.page-container :deep(.el-tabs) { flex: 1; display: flex; flex-direction: column; }
.page-container :deep(.el-tabs__content) { flex: 1; overflow: auto; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-shrink: 0; }
</style>

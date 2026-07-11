<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'
import * as echarts from 'echarts'
import { OfficeBuilding, User, Money, Clock, DataAnalysis, Setting, Document } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const structureData = ref<any[]>([])
const statsData = ref<any[]>([])
const loadingReport = ref(false)
const mySalary = ref<any>(null)
const myTrend = ref<any[]>([])

const dispMonth = ref(`${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, '0')}`)
const queryYear = ref(authStore.isAdmin ? new Date().getFullYear() : 2025)

async function loadReports() {
  loadingReport.value = true
  try {
    const [, m] = dispMonth.value.split('-').map(Number)
    const y = queryYear.value
    if (authStore.isAdmin) {
      const [s1, s2] = await Promise.all([
        request.get('/api/salary/report/structure', { params: { year: y, month: m } }),
        request.get('/api/salary/report/dept-stats', { params: { year: y, month: m } })
      ])
      structureData.value = s1.data.data || []
      statsData.value = s2.data.data || []
    } else {
      try {
        let records: any[] = []
        const s3 = await request.get('/api/salary/my', { params: { year: y, month: m } })
        records = s3.data.data?.records || []
        // 当月未计算则自动计算
        if (records.length === 0 && authStore.employeeId) {
          await request.post('/api/salary/calculate', null, { params: { year: y, month: m } })
          const retry = await request.get('/api/salary/my', { params: { year: y, month: m } })
          records = retry.data.data?.records || []
        }
        if (records.length > 0) mySalary.value = records[0]
        const trendRes = await request.get('/api/salary/my', { params: { year: queryYear.value } })
        myTrend.value = (trendRes.data.data?.records || []).sort((a: any, b: any) => a.salary_month - b.salary_month)
        setTimeout(() => { if (myTrend.value.length > 0) renderCharts() }, 200)
      } catch (e) { /* ignore */ }
    }
  } catch (e) { /* ignore */ }
  finally { loadingReport.value = false }
}

function renderCharts() {
  const el = document.getElementById('chart-my-trend')
  const el2 = document.getElementById('chart-my-bar')
  if (!el) return
  const chart = echarts.init(el); let chart2: any = null; if (el2) chart2 = echarts.init(el2)
  const months = ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月']
  const netData = new Array(12).fill(null); const grossData = new Array(12).fill(null)
  myTrend.value.forEach((m: any) => { const idx = (m.salary_month || 1) - 1; netData[idx] = Number(m.net_salary); grossData[idx] = Number(m.gross_salary) })
  chart.setOption({ grid: { left:40,right:20,top:10,bottom:20 }, tooltip:{trigger:'axis'}, xAxis:{type:'category',data:months,axisLabel:{fontSize:10}}, yAxis:{type:'value',axisLabel:{fontSize:10}}, series:[{type:'line',data:netData,smooth:true,lineStyle:{color:'#4a90d9',width:2},itemStyle:{color:'#2c5fa1'},areaStyle:{color:new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(74,144,217,0.25)'},{offset:1,color:'rgba(74,144,217,0.02)'}])}}] })
  if (chart2) chart2.setOption({ grid:{left:40,right:20,top:10,bottom:20}, tooltip:{trigger:'axis'}, xAxis:{type:'category',data:months,axisLabel:{fontSize:10}}, yAxis:{type:'value',axisLabel:{fontSize:10}}, series:[{type:'bar',data:grossData,name:'应发',color:'#5ba0f0'},{type:'bar',data:netData,name:'实发',color:'#73c74c'}] })
}

onMounted(() => { loadReports() })
</script>

<template>
  <div :class="authStore.isAdmin ? 'dashboard' : 'emp-dashboard'">
    <!-- ========== 管理员视图 ========== -->
    <template v-if="authStore.isAdmin">
      <div class="welcome-card">
        <div class="welcome-text"><h2>欢迎回来，{{ authStore.employeeName || authStore.username }}</h2><p>角色：系统管理员</p></div>
        <div class="welcome-date">{{ new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }) }}</div>
      </div>
      <el-row :gutter="16" class="quick-actions">
        <el-col :span="4" v-for="a in [{t:'部门管理',i:OfficeBuilding,p:'/department',c:'#3b7dd8'},{t:'员工管理',i:User,p:'/employee',c:'#67c23a'},{t:'工资管理',i:Money,p:'/salary',c:'#e6a23c'},{t:'考勤管理',i:Clock,p:'/attendance',c:'#f56c6c'},{t:'统计报表',i:DataAnalysis,p:'/reports',c:'#1e4f8a'},{t:'工资项配置',i:Setting,p:'/salary-config',c:'#409eff'}]" :key="a.p">
          <div class="action-card" @click="router.push(a.p)" :style="{borderTopColor:a.c}"><el-icon :size="26" :color="a.c"><component :is="a.i"/></el-icon><span class="action-title">{{ a.t }}</span></div>
        </el-col>
      </el-row>
      <el-row :gutter="16" style="flex:1;" v-loading="loadingReport">
        <el-col :span="12" style="height:100%"><el-card shadow="hover" class="report-card"><template #header><div class="report-header"><span class="report-title">薪资结构分析</span><span class="report-subtitle">{{ dispMonth.replace('-','年')+'月' }}</span></div></template>
          <el-table :data="structureData" stripe size="small" empty-text="暂无数据"><el-table-column label="部门" min-width="100" align="center"><template #default="{row}"><span style="color:#1e4f8a;font-weight:600">{{row.dept_name}}</span></template></el-table-column><el-table-column prop="employee_count" label="人数" min-width="50" align="center"/><el-table-column label="应发" min-width="110" align="center"><template #default="{row}"><span style="font-weight:600;color:#409eff">¥{{Number(row.total_gross_salary).toLocaleString()}}</span></template></el-table-column><el-table-column label="实发" min-width="110" align="center"><template #default="{row}"><span style="font-weight:700;color:#67c23a">¥{{Number(row.total_net_salary).toLocaleString()}}</span></template></el-table-column><el-table-column label="占比" min-width="60" align="center"><template #default="{row}">{{row.salary_ratio}}%</template></el-table-column></el-table></el-card></el-col>
        <el-col :span="12" style="height:100%"><el-card shadow="hover" class="report-card"><template #header><div class="report-header"><span class="report-title">部门薪资统计</span><span class="report-subtitle">{{ dispMonth.replace('-','年')+'月' }}</span></div></template>
          <el-table :data="statsData" stripe size="small" empty-text="暂无数据"><el-table-column label="部门" min-width="100" align="center"><template #default="{row}"><span style="color:#1e4f8a;font-weight:600">{{row.dept_name}}</span></template></el-table-column><el-table-column prop="employee_count" label="人数" min-width="50" align="center"/><el-table-column label="最高" min-width="100" align="center"><template #default="{row}"><span style="color:#e6a23c">¥{{Number(row.max_salary).toLocaleString()}}</span></template></el-table-column><el-table-column label="最低" min-width="100" align="center"><template #default="{row}">¥{{Number(row.min_salary).toLocaleString()}}</template></el-table-column><el-table-column label="平均" min-width="100" align="center"><template #default="{row}"><span style="font-weight:600;color:#1e4f8a">¥{{Number(row.avg_salary).toLocaleString()}}</span></template></el-table-column></el-table></el-card></el-col>
      </el-row>
    </template>

    <!-- ========== 非管理员视图 ========== -->
    <template v-else>
      <div class="welcome-card"><div class="welcome-text"><h2>欢迎回来，{{ authStore.employeeName || authStore.username }}</h2><p>角色：{{ authStore.positionName || '员工' }}</p></div><div class="welcome-date">{{ new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }) }}</div></div>
      <div v-loading="loadingReport" style="flex:1;display:flex;flex-direction:column;gap:14px">
        <div v-if="mySalary" class="my-overview">
          <div class="overview-item"><span class="ov-label">{{ dispMonth.replace('-','年') }}月 实发工资</span><span class="ov-net">¥{{ Number(mySalary.net_salary||0).toLocaleString() }}</span></div>
          <el-divider direction="vertical" style="height:50px"/>
          <div class="overview-item"><span class="ov-label">应发</span><span class="ov-gross">¥{{ Number(mySalary.gross_salary||0).toLocaleString() }}</span></div>
          <el-divider direction="vertical" style="height:50px"/>
          <div class="overview-item"><span class="ov-label">扣款</span><span class="ov-deduct">¥{{ Number(mySalary.total_deduction||0).toLocaleString() }}</span></div>
          <el-divider direction="vertical" style="height:50px"/>
          <div class="overview-item"><span class="ov-label">状态</span><span :class="mySalary.status==='CONFIRMED'?'my-confirmed':'my-draft'">{{ mySalary.status==='CONFIRMED'?'已确认':'草稿' }}</span></div>
          <el-divider direction="vertical" style="height:50px"/>
          <div class="overview-item action-card" @click="router.push('/my-salary')" style="border-top-color:#3b7dd8;cursor:pointer"><el-icon :size="24" color="#3b7dd8"><component :is="Document"/></el-icon><span class="action-title">薪资明细</span></div>
        </div>
        <div v-else style="text-align:center;color:#909399;padding:30px 0">暂无工资数据，请联系管理员</div>
        <div v-if="myTrend.length>0" style="display:flex;gap:14px;flex:1">
          <div class="trend-card" style="flex:1"><div class="trend-title">{{ queryYear }}年 薪资趋势</div><div id="chart-my-trend" style="flex:1;min-height:320px"/></div>
          <div class="trend-card" style="flex:1"><div class="trend-title">{{ queryYear }}年 应发/实发对比</div><div id="chart-my-bar" style="flex:1;min-height:320px"/></div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.dashboard { padding-right:12px; padding-bottom:8px }
.emp-dashboard { min-height:100%; display:flex; flex-direction:column; padding-right:12px; padding-bottom:8px }
.welcome-card { background:linear-gradient(135deg,#1e4f8a,#0f2d54); border-radius:12px; padding:28px 32px; color:#fff; display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; flex-shrink:0 }
.welcome-text h2 { font-size:20px; font-weight:600; margin-bottom:6px }
.welcome-text p { font-size:13px; opacity:.85 }
.welcome-date { font-size:14px; opacity:.9 }
.quick-actions { margin-bottom:20px; flex-shrink:0 }
.action-card { background:#fff; border-radius:10px; border-top:3px solid; padding:14px 8px; text-align:center; cursor:pointer; transition:all .3s ease; box-shadow:0 2px 8px rgba(0,0,0,.04) }
.action-card:hover { transform:translateY(-3px); box-shadow:0 4px 16px rgba(0,0,0,.1) }
.action-title { display:block; margin-top:8px; font-size:13px; color:#606266; font-weight:500 }
.report-card { height:100%; display:flex; flex-direction:column }
.report-card :deep(.el-card__body) { flex:1; overflow:auto; padding:12px 16px }
.report-card :deep(.el-table) { width:100%!important }
.report-header { display:flex; justify-content:space-between; align-items:center }
.report-title { font-weight:600; color:#303133 }
.report-subtitle { font-size:12px; color:#909399 }
.my-overview { display:flex; align-items:center; justify-content:space-around; background:#fff; border-radius:12px; padding:24px; box-shadow:0 2px 8px rgba(0,0,0,.04); flex-wrap:wrap; gap:8px }
.my-overview .action-card { padding:16px 14px; min-width:90px }
.overview-item { text-align:center }
.ov-label { display:block; font-size:13px; color:#909399; margin-bottom:8px }
.ov-net { font-size:32px; font-weight:700; color:#67c23a }
.ov-gross { font-size:22px; font-weight:600; color:#409eff }
.ov-deduct { font-size:18px; font-weight:600; color:#f56c6c }
.my-confirmed { color:#67c23a; font-weight:600; font-size:18px }
.my-draft { color:#e6a23c; font-weight:600; font-size:18px }
.trend-card { background:#fff; border-radius:12px; padding:16px 20px; box-shadow:0 2px 8px rgba(0,0,0,.04); flex:1; display:flex; flex-direction:column }
.trend-title { font-size:15px; font-weight:600; color:#1a3a6c; margin-bottom:8px }
</style>

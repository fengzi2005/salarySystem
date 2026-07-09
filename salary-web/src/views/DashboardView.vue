<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'
import {
  OfficeBuilding, User, Money, Clock,
  DataAnalysis, Setting
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const structureData = ref<any[]>([])
const statsData = ref<any[]>([])
const loadingReport = ref(false)

const now = new Date()
const reportYear = now.getMonth() === 0 ? now.getFullYear() - 1 : now.getFullYear()
const reportMonth = now.getMonth() === 0 ? 12 : now.getMonth()

/** 快捷功能卡片 */
const quickActions = [
  { title: '部门管理', icon: OfficeBuilding, path: '/department', color: '#3b7dd8' },
  { title: '员工管理', icon: User, path: '/employee', color: '#67c23a' },
  { title: '工资管理', icon: Money, path: '/salary', color: '#e6a23c' },
  { title: '考勤管理', icon: Clock, path: '/attendance', color: '#f56c6c' },
  { title: '统计报表', icon: DataAnalysis, path: '/reports', color: '#1e4f8a' },
  { title: '工资项配置', icon: Setting, path: '/salary-config', color: '#409eff' }
]

async function loadReports() {
  loadingReport.value = true
  try {
    const [s1, s2] = await Promise.all([
      request.get('/api/salary/report/structure', { params: { year: reportYear, month: reportMonth } }),
      request.get('/api/salary/report/dept-stats', { params: { year: reportYear, month: reportMonth } })
    ])
    structureData.value = s1.data.data || []
    statsData.value = s2.data.data || []
  } catch (e) {
    // 后端未启动或无数据时忽略
  } finally {
    loadingReport.value = false
  }
}

onMounted(loadReports)
</script>

<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-text">
        <h2>欢迎回来，{{ authStore.employeeName || authStore.username }}</h2>
        <p>角色：{{ authStore.isAdmin ? '系统管理员' : authStore.isManager ? '管理人员' : '普通员工' }}</p>
      </div>
      <div class="welcome-date">
        {{ new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }) }}
      </div>
    </div>

    <!-- 快捷功能入口 -->
    <el-row :gutter="16" class="quick-actions">
      <el-col :span="4" v-for="action in quickActions" :key="action.path">
        <div
          class="action-card"
          @click="router.push(action.path)"
          :style="{ borderTopColor: action.color }"
        >
          <el-icon :size="26" :color="action.color">
            <component :is="action.icon" />
          </el-icon>
          <span class="action-title">{{ action.title }}</span>
        </div>
      </el-col>
    </el-row>

    <!-- 两个主要报表 -->
    <el-row :gutter="20" v-loading="loadingReport">
      <!-- 薪资结构分析 -->
      <el-col :span="12">
        <el-card shadow="hover" class="report-card">
          <template #header>
            <div class="report-header">
              <span class="report-title">薪资结构分析</span>
              <span class="report-subtitle">{{ reportYear }}年{{ reportMonth }}月</span>
            </div>
          </template>
          <el-table :data="structureData" stripe size="small" :empty-text="'暂无数据（请先计算月度工资）'">
            <el-table-column prop="deptName" label="部门" width="110" />
            <el-table-column prop="employeeCount" label="人数" width="55" align="center" />
            <el-table-column prop="totalGrossSalary" label="应发合计" sortable align="right">
              <template #default="{ row }">
                <span style="font-weight: 600; color: #409eff">¥{{ Number(row.totalGrossSalary).toLocaleString() }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalNetSalary" label="实发合计" sortable align="right">
              <template #default="{ row }">
                <span style="font-weight: 700; color: #67c23a">¥{{ Number(row.totalNetSalary).toLocaleString() }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="salaryRatio" label="占比" width="65" align="center">
              <template #default="{ row }">{{ row.salaryRatio }}%</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 部门薪资统计 -->
      <el-col :span="12">
        <el-card shadow="hover" class="report-card">
          <template #header>
            <div class="report-header">
              <span class="report-title">部门薪资统计</span>
              <span class="report-subtitle">{{ reportYear }}年{{ reportMonth }}月</span>
            </div>
          </template>
          <el-table :data="statsData" stripe size="small" :empty-text="'暂无数据（请先计算月度工资）'">
            <el-table-column prop="deptName" label="部门" width="110" />
            <el-table-column prop="employeeCount" label="人数" width="55" align="center" />
            <el-table-column prop="maxSalary" label="最高" sortable align="right">
              <template #default="{ row }">¥{{ Number(row.maxSalary).toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="minSalary" label="最低" sortable align="right">
              <template #default="{ row }">¥{{ Number(row.minSalary).toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="avgSalary" label="平均" sortable align="right">
              <template #default="{ row }">
                <span style="font-weight: 600">¥{{ Number(row.avgSalary).toLocaleString() }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1300px;
}

.welcome-card {
  background: linear-gradient(135deg, #1e4f8a, #0f2d54);
  border-radius: 12px;
  padding: 28px 32px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.welcome-text h2 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 6px;
}

.welcome-text p {
  font-size: 13px;
  opacity: 0.85;
}

.welcome-date {
  font-size: 14px;
  opacity: 0.9;
}

.quick-actions {
  margin-bottom: 20px;
}

.action-card {
  background: #fff;
  border-radius: 10px;
  border-top: 3px solid;
  padding: 20px 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.action-title {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.report-card {
  height: 100%;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-title {
  font-weight: 600;
  color: #303133;
}

.report-subtitle {
  font-size: 12px;
  color: #909399;
}
</style>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Money, Check } from '@element-plus/icons-vue'
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
const searchForm = reactive({ year: new Date().getFullYear(), month: new Date().getMonth(), deptId: null as number | null, employeeName: '', empNo: '' })
const deptOptions = ref<any[]>([])
const editingRow = ref<any>(null)
const editingDialog = ref(false)
const manualForm = reactive({ projectBonus: 0, taxDeduction: 0, otherAdditions: 0, otherDeductions: 0 })

async function loadData() {
  loading.value = true
  try {
    const params: any = { year: searchForm.year, month: searchForm.month }
    if (searchForm.deptId) params.deptId = searchForm.deptId
    if (searchForm.employeeName) params.employeeName = searchForm.employeeName
    if (searchForm.empNo) params.empNo = searchForm.empNo
    const res = await request.get('/api/salary/query', { params })
    tableData.value = res.data.data || []
  } finally { loading.value = false }
}

async function handleCalculate() {
  try {
    await request.post('/api/salary/calculate', null, {
      params: { year: searchForm.year, month: searchForm.month }
    })
    ElMessage.success('月度工资计算完成！')
    loadData()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '计算失败') }
}

async function handleConfirm() {
  try {
    await request.post('/api/salary/confirm', null, {
      params: { year: searchForm.year, month: searchForm.month }
    })
    ElMessage.success('工资已确认，数据已锁定！')
    loadData()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '确认失败') }
}

function handleEdit(row: any) {
  editingRow.value = row
  manualForm.projectBonus = row.projectBonus || 0
  manualForm.taxDeduction = row.taxDeduction || 0
  manualForm.otherAdditions = row.otherAdditions || 0
  manualForm.otherDeductions = row.otherDeductions || 0
  editingDialog.value = true
}

async function handleSaveManual() {
  try {
    await request.put(`/api/salary/manual/${editingRow.value.id}`, manualForm)
    ElMessage.success('手动项更新成功，汇总已自动重算')
    editingDialog.value = false
    loadData()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '更新失败') }
}

onMounted(async () => {
  const deptRes = await request.get('/api/department')
  deptOptions.value = deptRes.data.data || []
  loadData()
})
</script>

<template>
  <div class="page-container">
    <!-- 操作工具栏 -->
    <div class="toolbar">
      <el-input-number v-model="searchForm.year" :min="2020" :max="2099" style="width: 110px" />
      <span style="margin: 0 4px">年</span>
      <el-input-number v-model="searchForm.month" :min="1" :max="12" style="width: 80px" />
      <span style="margin: 0 4px">月</span>
      <el-select v-model="searchForm.deptId" placeholder="选择部门" clearable style="width: 150px; margin-left: 8px">
        <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id" />
      </el-select>
      <el-input v-model="searchForm.employeeName" placeholder="姓名" clearable style="width: 120px; margin-left: 8px" />
      <el-input v-model="searchForm.empNo" placeholder="工号" clearable style="width: 120px; margin-left: 8px" />
      <el-button type="primary" :icon="Search" @click="loadData" style="margin-left: 8px">查询</el-button>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      <div style="flex: 1" />
      <el-button type="warning" :icon="Money" @click="handleCalculate">自动计算工资</el-button>
      <el-button type="success" :icon="Check" @click="handleConfirm" v-if="authStore.isAdmin">确认归档</el-button>
    </div>

    <!-- 工资表格 -->
    <el-card shadow="hover">
      <div style="flex: 1; overflow: auto;">
        <el-table :data="pagedData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="empNo" label="工号" width="100" fixed />
        <el-table-column prop="employeeName" label="姓名" width="80" fixed />
        <el-table-column prop="deptName" label="部门" width="110" />
        <el-table-column prop="positionName" label="岗位" width="110" />
        <el-table-column prop="baseSalary" label="基本工资" width="95" align="right" />
        <el-table-column prop="positionSalary" label="岗位工资" width="95" align="right" />
        <el-table-column prop="titleSalary" label="职级工资" width="95" align="right" />
        <el-table-column prop="seniorityPay" label="工龄工资" width="95" align="right" />
        <el-table-column prop="fullAttendanceBonus" label="全勤奖" width="80" align="right" />
        <el-table-column prop="leaveDeduction" label="事假扣款" width="90" align="right" />
        <el-table-column prop="lateDeduction" label="迟到扣款" width="90" align="right" />
        <el-table-column prop="absentDeduction" label="旷工扣款" width="90" align="right" />
        <el-table-column prop="projectBonus" label="项目奖金" width="90" align="right" />
        <el-table-column prop="taxDeduction" label="个税扣除" width="90" align="right" />
        <el-table-column prop="grossSalary" label="应发合计" width="100" align="right" fixed="right">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #409eff">{{ row.grossSalary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalDeduction" label="扣款合计" width="100" align="right" fixed="right">
          <template #default="{ row }">
            <span style="color: #f56c6c">{{ row.totalDeduction }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="netSalary" label="实发工资" width="100" align="right" fixed="right">
          <template #default="{ row }">
            <span style="font-weight: 700; color: #67c23a; font-size: 15px">{{ row.netSalary }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-tag :type="row.status === 'CONFIRMED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'CONFIRMED' ? '已确认' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'CONFIRMED' && authStore.isAdmin" type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <span v-else style="color: #c0c4cc; font-size: 12px">--</span>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <div style="display: flex; justify-content: center; padding: 14px 0 4px; flex-shrink: 0;">
        <el-pagination :current-page="page" :page-size="pageSize" :total="total" :page-sizes="[10, 15, 20, 50]" layout="total, sizes, prev, pager, next, jumper" @current-change="page = $event" @size-change="pageSize = $event; page = 1" />
      </div>
    </el-card>

    <!-- 手动项编辑弹窗 -->
    <el-dialog v-model="editingDialog" title="编辑手动录入项" width="400px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="项目奖金">
          <el-input-number v-model="manualForm.projectBonus" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="个税扣除">
          <el-input-number v-model="manualForm.taxDeduction" :min="0" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="其他加项">
          <el-input-number v-model="manualForm.otherAdditions" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="其他减项">
          <el-input-number v-model="manualForm.otherDeductions" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editingDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveManual">保存（自动重算）</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { height: calc(100vh - 100px); display: flex; flex-direction: column; }
.page-container :deep(.el-card) { flex: 1; display: flex; flex-direction: column; }
.page-container :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-wrap: wrap; gap: 6px; flex-shrink: 0; }
</style>

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
const now = new Date()
const searchMonth = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)
const searchForm = reactive({ deptId: null as number | null, employeeName: '', empNo: '' })
const detailItems = [
  { label: '基本工资', key: 'base_salary', cls: 'detail-value' },
  { label: '岗位工资', key: 'position_salary', cls: 'detail-value' },
  { label: '职级工资', key: 'title_salary', cls: 'detail-value' },
  { label: '工龄工资', key: 'seniority_pay', cls: 'detail-value' },
  { label: '全勤奖金', key: 'full_attendance_bonus', cls: 'detail-bonus' },
  { label: '事假扣款', key: 'leave_deduction', cls: 'detail-deduct' },
  { label: '迟到扣款', key: 'late_deduction', cls: 'detail-deduct' },
  { label: '旷工扣款', key: 'absent_deduction', cls: 'detail-deduct' },
  { label: '项目奖金', key: 'project_bonus', cls: 'detail-bonus' },
  { label: '个税扣除', key: 'tax_deduction', cls: 'detail-deduct' },
  { label: '其他加项', key: 'other_additions', cls: 'detail-value' },
  { label: '其他减项', key: 'other_deductions', cls: 'detail-deduct' },
]

const deptOptions = ref<any[]>([])
const editingRow = ref<any>(null)
const editingDialog = ref(false)
const detailRow = ref<any>(null)
const detailDialog = ref(false)
const manualForm = reactive({ projectBonus: 0, taxDeduction: 0, otherAdditions: 0, otherDeductions: 0 })

async function loadData() {
  if (!searchMonth.value) {
    tableData.value = []
    ElMessage.warning('请选择月份')
    return
  }
  loading.value = true
  try {
    const [y, m] = searchMonth.value.split('-').map(Number)
    const params: any = { year: y, month: m }
    if (searchForm.deptId) params.deptId = searchForm.deptId
    if (searchForm.employeeName) params.employeeName = searchForm.employeeName
    if (searchForm.empNo) params.empNo = searchForm.empNo
    const res = await request.get('/api/salary/query', { params })
    tableData.value = res.data.data || []
  } finally { loading.value = false }
}

async function handleCalculate() {
  if (!searchMonth.value) { tableData.value = []; ElMessage.warning('请选择月份'); return }
  try {
    const [y, m] = searchMonth.value.split('-').map(Number)
    await request.post('/api/salary/calculate', null, { params: { year: y, month: m } })
    ElMessage.success('月度工资计算完成！')
    loadData()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '计算失败') }
}

async function handleConfirm() {
  if (!searchMonth.value) { tableData.value = []; ElMessage.warning('请选择月份'); return }
  try {
    const [y, m] = searchMonth.value.split('-').map(Number)
    await request.post('/api/salary/confirm', null, { params: { year: y, month: m } })
    ElMessage.success('工资已确认，数据已锁定！')
    loadData()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '确认失败') }
}

function handleEdit(row: any) {
  editingRow.value = row
  manualForm.projectBonus = row.project_bonus || 0
  manualForm.taxDeduction = row.tax_deduction || 0
  manualForm.otherAdditions = row.other_additions || 0
  manualForm.otherDeductions = row.other_deductions || 0
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
      <el-date-picker v-model="searchMonth" type="month" placeholder="选择月份" format="YYYY年M月" value-format="YYYY-MM" clearable style="width: 150px" @change="loadData" @clear="tableData = []" />
      <el-select v-model="searchForm.deptId" placeholder="选择部门" clearable style="width: 150px; margin-left: 8px" @change="loadData" @clear="loadData">
        <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id" />
      </el-select>
      <el-input v-model="searchForm.employeeName" placeholder="姓名" clearable style="width: 120px; margin-left: 8px" @clear="loadData" @change="loadData" />
      <el-input v-model="searchForm.empNo" placeholder="工号" clearable style="width: 120px; margin-left: 8px" @clear="loadData" @change="loadData" />
      <el-button :icon="Refresh" @click="loadData" style="margin-left: 4px" />
      <div style="flex: 1" />
      <el-button type="warning" :icon="Money" @click="handleCalculate">自动计算工资</el-button>
      <el-button type="success" :icon="Check" @click="handleConfirm" v-if="authStore.isAdmin">确认归档</el-button>
    </div>

    <!-- 工资表格 -->
    <el-card shadow="never" style="border: none;">
      <div style="flex: 1; overflow: auto;">
        <el-table :data="pagedData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column label="工号" min-width="90" align="center">
          <template #default="{ row }"><span class="sal-empno">{{ row.emp_no }}</span></template>
        </el-table-column>
        <el-table-column label="姓名" min-width="70" align="center">
          <template #default="{ row }"><span class="sal-name">{{ row.employee_name }}</span></template>
        </el-table-column>
        <el-table-column prop="dept_name" label="部门" min-width="100" align="center" show-overflow-tooltip />
        <el-table-column prop="position_name" label="岗位" min-width="100" align="center" show-overflow-tooltip />
        <el-table-column label="应发合计" min-width="100" align="center">
          <template #default="{ row }"><span class="sal-gross">¥{{ row.gross_salary }}</span></template>
        </el-table-column>
        <el-table-column label="扣款合计" min-width="90" align="center">
          <template #default="{ row }"><span class="sal-deduct">¥{{ row.total_deduction }}</span></template>
        </el-table-column>
        <el-table-column label="实发工资" min-width="100" align="center">
          <template #default="{ row }"><span class="sal-net">¥{{ row.net_salary }}</span></template>
        </el-table-column>
        <el-table-column label="状态" min-width="75" align="center">
          <template #default="{ row }">
            <span :class="row.status === 'CONFIRMED' ? 'sal-confirmed' : 'sal-draft'">{{ row.status === 'CONFIRMED' ? '已确认' : '草稿' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="110" align="center">
          <template #default="{ row }">
            <el-button link class="sal-view" @click="detailRow = row; detailDialog = true">详情</el-button>
            <el-button v-if="row.status !== 'CONFIRMED' && authStore.isAdmin" link class="sal-edit" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 工资详情弹窗 -->
    <el-dialog v-model="detailDialog" width="680px" destroy-on-close>
      <template #header>
        <div class="detail-title-bar">
          <span class="detail-title-name">{{ detailRow?.employee_name }}</span>
          <span class="detail-title-info">{{ detailRow?.emp_no }} | {{ detailRow?.dept_name }} | {{ detailRow?.position_name }}</span>
        </div>
      </template>
      <div v-if="detailRow" class="detail-wrap">
        <div class="detail-section-title">薪酬明细</div>
        <el-row :gutter="20">
          <el-col :span="12" v-for="item in detailItems" :key="item.label" class="detail-item">
            <span class="detail-label">{{ item.label }}</span>
            <span :class="item.cls || 'detail-value'">¥{{ (detailRow[item.key] || 0) }}</span>
          </el-col>
        </el-row>
        <div class="detail-summary">
          <div class="detail-sum-item gross">
            <span>应发合计</span>
            <span>¥{{ detailRow.gross_salary }}</span>
          </div>
          <div class="detail-sum-item deduct">
            <span>扣款合计</span>
            <span>¥{{ detailRow.total_deduction }}</span>
          </div>
          <div class="detail-sum-item net">
            <span>实发工资</span>
            <span>¥{{ detailRow.net_salary }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { height: calc(100vh - 100px); display: flex; flex-direction: column; }
.page-container :deep(.el-card) { flex: 1; display: flex; flex-direction: column; }
.page-container :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-wrap: wrap; gap: 6px; flex-shrink: 0; }
.toolbar :deep(.el-input__inner) { font-size: 15px; }

.sal-empno { font-weight: 700; color: #303133; }
.sal-name { color: #1e4f8a; font-weight: 700; }
.sal-gross { color: #409eff; font-weight: 600; }
.sal-deduct { color: #f56c6c; font-weight: 600; }
.sal-net { color: #67c23a; font-weight: 700; font-size: 15px; }
.sal-confirmed { color: #67c23a; font-weight: 600; }
.sal-draft { color: #e6a23c; font-weight: 600; }
.sal-view { color: #e8789a !important; font-size: 14px !important; }
.sal-edit { color: #409eff !important; font-size: 14px !important; }

.detail-title-bar { }
.detail-title-name { font-size: 22px; font-weight: 700; color: #1a3a6c; margin-right: 12px; }
.detail-title-info { font-size: 13px; color: #909399; }

.detail-section-title { font-size: 16px; font-weight: 600; color: #1a3a6c; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 2px solid #e8ecf1; }

.detail-item { display: flex; align-items: center; gap: 16px; padding: 10px 12px; margin: 2px 0; background: #fafbfc; border-radius: 6px; }
.detail-label { color: #606266; font-size: 15px; }
.detail-value { color: #303133; font-weight: 500; font-size: 15px; }
.detail-bonus { color: #67c23a; font-weight: 600; font-size: 15px; }
.detail-deduct { color: #f56c6c; font-weight: 600; font-size: 15px; }

.detail-summary { display: flex; gap: 10px; margin-top: 16px; }
.detail-sum-item { flex: 1; text-align: center; padding: 14px 8px; border-radius: 8px; display: flex; flex-direction: column; gap: 6px; font-size: 13px; }
.detail-sum-item.gross { background: #ecf5ff; color: #409eff; }
.detail-sum-item.gross span:last-child { font-size: 18px; font-weight: 700; }
.detail-sum-item.deduct { background: #fef0f0; color: #f56c6c; }
.detail-sum-item.deduct span:last-child { font-size: 18px; font-weight: 700; }
.detail-sum-item.net { background: #f0f9eb; color: #67c23a; }
.detail-sum-item.net span:last-child { font-size: 20px; font-weight: 700; }
</style>

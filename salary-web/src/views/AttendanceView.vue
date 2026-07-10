<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Delete, Edit, Refresh, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Attendance {
  id?: number; employee_id: number; attendance_year: number; attendance_month: number
  leave_days: number; late_times: number; absent_days: number
  is_full_attendance: number; leave_deduction: number; late_deduction: number
  absent_deduction: number; full_attendance_bonus: number
  employee_name?: string; emp_no?: string; dept_name?: string
}

const loading = ref(false)
const tableData = ref<Attendance[]>([])
const page = ref(1)
const pageSize = ref(15)
const total = ref(0)
const pagedData = computed(() => {
  total.value = tableData.value.length
  const start = (page.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})
const startDate = ref('2025-01')
const endDate = ref('2025-12')

const dialogVisible = ref(false)
const dialogTitle = ref('录入考勤')
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)
const employeeOptions = ref<any[]>([])

const form = reactive({
  employeeId: null as number | null, attendanceYear: 2025, attendanceMonth: 6,
  leaveDays: 0, lateTimes: 0, absentDays: 0
})

const rules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const [sy, sm] = startDate.value.split('-').map(Number)
    const [ey, em] = endDate.value.split('-').map(Number)
    const res = await request.get('/api/attendance/month', {
      params: { startYear: sy, startMonth: sm, endYear: ey, endMonth: em }
    })
    tableData.value = res.data.data || []
  } finally { loading.value = false }
}

async function loadEmployees() {
  const res = await request.get('/api/employee/active')
  employeeOptions.value = res.data.data || []
}

function handleAdd() {
  dialogTitle.value = '录入考勤'; editingId.value = null
  const [y, m] = endDate.value.split('-').map(Number)
  Object.assign(form, { employeeId: null, attendanceYear: y, attendanceMonth: m, leaveDays: 0, lateTimes: 0, absentDays: 0 })
  dialogVisible.value = true
}
function handleEdit(row: Attendance) {
  dialogTitle.value = '编辑考勤'; editingId.value = row.id!
  form.employeeId = row.employee_id
  form.attendanceYear = row.attendance_year
  form.attendanceMonth = row.attendance_month
  form.leaveDays = row.leave_days
  form.lateTimes = row.late_times
  form.absentDays = row.absent_days
  dialogVisible.value = true
}
async function handleDelete(row: Attendance) {
  try {
    await ElMessageBox.confirm('确定要删除该考勤记录吗？', '删除确认', { type: 'warning' })
    await request.delete(`/api/attendance/${row.id}`)
    ElMessage.success('删除成功'); loadData()
  } catch { /* cancelled */ }
}
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    const data = { ...form }
    if (editingId.value) {
      await request.put('/api/attendance', { ...data, id: editingId.value })
      ElMessage.success('更新成功（扣款/奖金已自动重算）')
    } else {
      await request.post('/api/attendance', data)
      ElMessage.success('新增成功（扣款/奖金已自动计算）')
    }
    dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '操作失败') }
}

onMounted(() => { loadData(); loadEmployees() })
</script>

<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">录入考勤</el-button>
      <el-date-picker v-model="startDate" type="month" placeholder="开始月份" format="YYYY年M月" value-format="YYYY-MM" style="width: 150px; margin-left: 10px" />
      <span style="margin: 0 8px; color: #909399">至</span>
      <el-date-picker v-model="endDate" type="month" placeholder="结束月份" format="YYYY年M月" value-format="YYYY-MM" style="width: 150px" />

      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      <div style="flex: 1" />
      <span style="background: #f4f4f5; color: #606266; font-size: 16px; padding: 6px 14px; border-radius: 4px; white-space: nowrap;">事假100元/天 | 迟到50元/次 | 旷工300元/天 | 全勤奖300元/月</span>
    </div>

    <el-card shadow="never" style="border: none;">
      <div style="flex: 1; overflow: auto;">
        <el-table :data="pagedData" v-loading="loading" border stripe>
        <el-table-column label="工号" min-width="90" align="center">
          <template #default="{ row }">
            <span class="att-empno">{{ row.emp_no }}</span>
          </template>
        </el-table-column>
        <el-table-column label="姓名" min-width="70" align="center">
          <template #default="{ row }">
            <span class="att-name">{{ row.employee_name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="dept_name" label="部门" min-width="100" align="center" show-overflow-tooltip />
        <el-table-column label="事假" min-width="60" align="center">
          <template #default="{ row }">
            <span :class="row.leave_days > 0 ? 'att-warn' : ''">{{ row.leave_days }}天</span>
          </template>
        </el-table-column>
        <el-table-column label="迟到" min-width="60" align="center">
          <template #default="{ row }">
            <span :class="row.late_times > 0 ? 'att-warn' : ''">{{ row.late_times }}次</span>
          </template>
        </el-table-column>
        <el-table-column label="旷工" min-width="60" align="center">
          <template #default="{ row }">
            <span :class="row.absent_days > 0 ? 'att-danger' : ''">{{ row.absent_days }}天</span>
          </template>
        </el-table-column>
        <el-table-column label="全勤" min-width="60" align="center">
          <template #default="{ row }">
            <span :class="row.is_full_attendance === 1 ? 'att-ok' : 'att-bad'">{{ row.is_full_attendance === 1 ? '是' : '否' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="事假扣款" min-width="85" align="center">
          <template #default="{ row }">
            <span :class="row.leave_deduction > 0 ? 'att-money' : 'att-zero'">¥{{ row.leave_deduction }}</span>
          </template>
        </el-table-column>
        <el-table-column label="迟到扣款" min-width="85" align="center">
          <template #default="{ row }">
            <span :class="row.late_deduction > 0 ? 'att-money' : 'att-zero'">¥{{ row.late_deduction }}</span>
          </template>
        </el-table-column>
        <el-table-column label="旷工扣款" min-width="85" align="center">
          <template #default="{ row }">
            <span :class="row.absent_deduction > 0 ? 'att-money' : 'att-zero'">¥{{ row.absent_deduction }}</span>
          </template>
        </el-table-column>
        <el-table-column label="全勤奖金" min-width="85" align="center">
          <template #default="{ row }">
            <span :class="row.full_attendance_bonus > 0 ? 'att-bonus' : 'att-zero'">¥{{ row.full_attendance_bonus }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="100" align="center">
          <template #default="{ row }">
            <el-button link class="att-action edit-btn" @click="handleEdit(row)">编辑</el-button>
            <el-button link class="att-action del-btn" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <div style="display: flex; justify-content: center; padding: 14px 0 4px; flex-shrink: 0;">
        <el-pagination :current-page="page" :page-size="pageSize" :total="total" :page-sizes="[10, 15, 20, 50]" layout="total, sizes, prev, pager, next, jumper" @current-change="page = $event" @size-change="pageSize = $event; page = 1" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="440px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="form.employeeId" placeholder="选择员工" filterable style="width: 100%">
            <el-option v-for="e in employeeOptions" :key="e.id" :label="`${e.emp_no} - ${e.name}`" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年份">
              <el-input-number v-model="form.attendanceYear" :min="2020" :max="2099" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="月份">
              <el-input-number v-model="form.attendanceMonth" :min="1" :max="12" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="事假天数">
          <el-input-number v-model="form.leaveDays" :min="0" :max="31" style="width: 100%" />
        </el-form-item>
        <el-form-item label="迟到次数">
          <el-input-number v-model="form.lateTimes" :min="0" :max="99" style="width: 100%" />
        </el-form-item>
        <el-form-item label="旷工天数">
          <el-input-number v-model="form.absentDays" :min="0" :max="31" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { height: calc(100vh - 100px); display: flex; flex-direction: column; }
.page-container :deep(.el-card) { flex: 1; display: flex; flex-direction: column; }
.page-container :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-wrap: wrap; gap: 8px; flex-shrink: 0; }
.toolbar :deep(.el-input__inner) { font-size: 15px; }

.att-empno { font-weight: 700; color: #303133; }
.att-name { color: #1e4f8a; font-weight: 600; }
.att-warn { color: #e6a23c; font-weight: 600; }
.att-danger { color: #f56c6c; font-weight: 600; }
.att-ok { color: #67c23a; font-weight: 600; }
.att-bad { color: #f56c6c; font-weight: 600; }
.att-money { color: #f56c6c; font-weight: 500; }
.att-bonus { color: #67c23a; font-weight: 600; }
.att-zero { color: #c0c4cc; }
.att-action { font-size: 15px !important; }
.edit-btn { color: #409eff !important; }
.del-btn { color: #f56c6c !important; }
</style>

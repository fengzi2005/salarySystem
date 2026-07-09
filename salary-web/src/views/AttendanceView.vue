<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Delete, Edit, Refresh, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Attendance {
  id?: number; employeeId: number; attendanceYear: number; attendanceMonth: number
  leaveDays: number; lateTimes: number; absentDays: number
  isFullAttendance: number; leaveDeduction: number; lateDeduction: number
  absentDeduction: number; fullAttendanceBonus: number
  employeeName?: string; empNo?: string; deptName?: string
}

const loading = ref(false)
const tableData = ref<Attendance[]>([])
const year = ref(new Date().getFullYear())
const month = ref(new Date().getMonth() + 1)

const dialogVisible = ref(false)
const dialogTitle = ref('录入考勤')
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)
const employeeOptions = ref<any[]>([])

const form = reactive({
  employeeId: null as number | null, attendanceYear: year.value, attendanceMonth: month.value,
  leaveDays: 0, lateTimes: 0, absentDays: 0
})

const rules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/api/attendance/month', { params: { year: year.value, month: month.value } })
    tableData.value = res.data.data || []
  } finally { loading.value = false }
}

async function loadEmployees() {
  const res = await request.get('/api/employee/active')
  employeeOptions.value = res.data.data || []
}

function handleAdd() {
  dialogTitle.value = '录入考勤'; editingId.value = null
  Object.assign(form, { employeeId: null, attendanceYear: year.value, attendanceMonth: month.value, leaveDays: 0, lateTimes: 0, absentDays: 0 })
  dialogVisible.value = true
}
function handleEdit(row: Attendance) {
  dialogTitle.value = '编辑考勤'; editingId.value = row.id!
  form.employeeId = row.employeeId
  form.attendanceYear = row.attendanceYear
  form.attendanceMonth = row.attendanceMonth
  form.leaveDays = row.leaveDays
  form.lateTimes = row.lateTimes
  form.absentDays = row.absentDays
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
      <el-input-number v-model="year" :min="2020" :max="2099" style="width: 100px; margin-left: 10px" />
      <span style="margin: 0 4px; color: #606266">年</span>
      <el-input-number v-model="month" :min="1" :max="12" style="width: 80px" />
      <span style="margin: 0 4px; color: #606266">月</span>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      <el-alert type="info" show-icon :closable="false" style="margin-left: 16px; flex: 1" title="说明：事假100元/天 | 迟到50元/次 | 旷工300元/天 | 全勤奖300元/月（触发器自动计算）" />
    </div>

    <el-card shadow="hover">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="empNo" label="工号" width="100" />
        <el-table-column prop="employeeName" label="姓名" width="90" />
        <el-table-column prop="deptName" label="部门" width="110" />
        <el-table-column prop="leaveDays" label="事假(天)" width="90" align="center" />
        <el-table-column prop="lateTimes" label="迟到(次)" width="90" align="center" />
        <el-table-column prop="absentDays" label="旷工(天)" width="90" align="center" />
        <el-table-column label="全勤" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isFullAttendance ? 'success' : 'danger'" size="small">
              {{ row.isFullAttendance ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="leaveDeduction" label="事假扣款" width="100" align="right" />
        <el-table-column prop="lateDeduction" label="迟到扣款" width="100" align="right" />
        <el-table-column prop="absentDeduction" label="旷工扣款" width="100" align="right" />
        <el-table-column prop="fullAttendanceBonus" label="全勤奖金" width="100" align="right" />
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="440px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="form.employeeId" placeholder="选择员工" filterable style="width: 100%">
            <el-option v-for="e in employeeOptions" :key="e.id" :label="`${e.empNo} - ${e.name}`" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年份">
              <el-input-number v-model="form.attendanceYear" :min="2020" :max="2099" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="月份">
              <el-input-number v-model="form.attendanceMonth" :min="1" :max="12" style="width: 100%" />
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
.page-container { max-width: 1400px; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
</style>

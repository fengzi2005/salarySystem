<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Search, Delete, Edit, Refresh, View } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Employee {
  employee_id?: number
  emp_no: string
  employee_name: string
  gender: number
  dept_name: string
  dept_id: number
  position_name: string
  position_id: number
  title_name: string
  title_id: number
  title_salary: number
  entry_date: string
  base_salary: number
  phone: string
  email: string
  status: number
  service_years: number
}

const loading = ref(false)
const tableData = ref<Employee[]>([])
const sortedData = computed(() => {
  return [...tableData.value].sort((a, b) => {
    const titleDiff = (b.title_salary || 0) - (a.title_salary || 0)
    if (titleDiff !== 0) return titleDiff
    return (b.base_salary || 0) - (a.base_salary || 0)
  })
})
const searchKeyword = ref('')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)
const deptOptions = ref<any[]>([])
const positionOptions = ref<any[]>([])
const titleOptions = ref<any[]>([])

const form = reactive({
  empNo: '', name: '', gender: 1, departmentId: null as number | null,
  positionId: null as number | null, titleId: null as number | null,
  entryDate: '', baseSalary: 0, phone: '', email: '', status: 1
})

const rules = {
  empNo: [
    { required: true, message: '请输入员工编号', trigger: 'blur' },
    { pattern: /^FS\d{7}$/, message: '工号格式必须为FS+7位数字，如FS2025001', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  entryDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    if (searchKeyword.value) {
      const res = await request.get('/api/employee/search', {
        params: { keyword: searchKeyword.value }
      })
      tableData.value = res.data.data || []
      total.value = tableData.value.length
    } else {
      const res = await request.get('/api/employee/list', {
        params: { page: page.value, pageSize: pageSize.value }
      })
      const data = res.data.data
      tableData.value = data.records || []
      total.value = data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handlePageChange(p: number) {
  page.value = p
  loadData()
}

function handleSizeChange(s: number) {
  pageSize.value = s
  page.value = 1
  loadData()
}

async function loadOptions() {
  const [deptRes, posRes, titleRes] = await Promise.all([
    request.get('/api/department'),
    request.get('/api/position'),
    request.get('/api/title')
  ])
  deptOptions.value = deptRes.data.data || []
  positionOptions.value = posRes.data.data || []
  titleOptions.value = titleRes.data.data || []
}

function handleAdd() {
  dialogTitle.value = '新增员工'
  editingId.value = null
  Object.assign(form, { empNo: '', name: '', gender: 1, departmentId: null, positionId: null, titleId: null, entryDate: '', baseSalary: 0, phone: '', email: '', status: 1 })
  loadOptions()
  dialogVisible.value = true
}

async function handleEdit(row: Employee) {
  dialogTitle.value = '编辑员工'
  editingId.value = row.employee_id
  try {
    const res = await request.get(`/api/employee/${row.employee_id}`)
    const d = res.data.data
    Object.assign(form, {
      empNo: d.empNo, name: d.name, gender: d.gender,
      departmentId: d.departmentId, positionId: d.positionId, titleId: d.titleId,
      entryDate: d.entryDate, baseSalary: d.baseSalary, phone: d.phone || '',
      email: d.email || '', status: d.status
    })
  } catch { /* fallback */ }
  dialogVisible.value = true
}

async function handleDelete(row: Employee) {
  try {
    await ElMessageBox.confirm(`确定要删除员工 "${row.employee_name}" 吗？`, '删除确认', { type: 'warning' })
    await request.delete(`/api/employee/${row.employee_id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch { /* cancelled */ }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    const data = { ...form, id: editingId.value }
    if (editingId.value) {
      await request.put('/api/employee', data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/api/employee', data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

onMounted(() => { loadData(); loadOptions() })
</script>

<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增员工</el-button>
      <el-input v-model="searchKeyword" placeholder="搜索姓名/编号" prefix-icon="Search" clearable style="width: 200px; margin-left: 10px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" :icon="Search" @click="loadData" style="margin-left: 8px">搜索</el-button>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </div>

    <el-card shadow="never" style="border: none; padding-top: 8px;">
      <div style="flex: 1; overflow: auto;">
        <el-table :data="sortedData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column label="工号" width="100" align="center">
          <template #default="{ row }">
            <span class="empno-cell">{{ row.emp_no }}</span>
          </template>
        </el-table-column>
        <el-table-column label="姓名" width="70" align="center">
          <template #default="{ row }">
            <span class="name-cell">{{ row.employee_name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="55" align="center">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="dept_name" label="部门" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="position_name" label="岗位" width="136" align="center" show-overflow-tooltip />
        <el-table-column prop="title_name" label="职称" width="100" align="center" show-overflow-tooltip />
        <el-table-column prop="entry_date" label="入职日期" width="105" align="center" />
        <el-table-column label="基本工资" width="95" align="center">
          <template #default="{ row }">
            <span class="salary-cell">¥{{ row.base_salary }}</span>
          </template>
        </el-table-column>
        <el-table-column label="工龄" width="65" align="center">
          <template #default="{ row }">
            <span class="year-cell">{{ row.service_years }}年</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="120" align="center" />
        <el-table-column prop="email" label="邮箱" min-width="144" align="center" show-overflow-tooltip />
        <el-table-column label="状态" width="65" align="center">
          <template #default="{ row }">
            <span :class="row.status === 1 ? 'status-active' : 'status-inactive'">{{ row.status === 1 ? '在职' : '离职' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link class="emp-action-btn edit-btn" @click="handleEdit(row)">编辑</el-button>
            <el-button link class="emp-action-btn del-btn" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <div style="display: flex; justify-content: center; padding: 16px 0 4px; flex-shrink: 0;" v-if="!searchKeyword">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          :page-sizes="[10, 12, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工号" prop="empNo">
              <el-input v-model="form.empNo" placeholder="如 FS2025001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="员工姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">在职</el-radio>
                <el-radio :value="0">离职</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门" prop="departmentId">
              <el-select v-model="form.departmentId" placeholder="选择部门" style="width: 100%">
                <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="positionId">
              <el-select v-model="form.positionId" placeholder="选择岗位" clearable style="width: 100%">
                <el-option v-for="p in positionOptions" :key="p.id" :label="`${p.positionName} (¥${p.positionSalary})`" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="职称" prop="titleId">
              <el-select v-model="form.titleId" placeholder="选择职称" clearable style="width: 100%">
                <el-option v-for="t in titleOptions" :key="t.id" :label="`${t.titleName} (¥${t.titleSalary})`" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="entryDate">
              <el-date-picker v-model="form.entryDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="基本工资" prop="baseSalary">
              <el-input-number v-model="form.baseSalary" :min="0" :max="99999" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话">
              <el-input v-model="form.phone" placeholder="联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="电子邮箱" />
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
.page-container {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.page-container :deep(.el-card) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.page-container :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-shrink: 0; }

.status-active {
  color: #67c23a;
  font-weight: 600;
}

.status-inactive {
  color: #f56c6c;
  font-weight: 600;
}

.emp-action-btn { }

.edit-btn {
  color: #409eff !important;
}

.del-btn {
  color: #f56c6c !important;
}

.empno-cell {
  font-weight: 700;
  color: #303133;
}

.name-cell {
  color: #1e4f8a;
  font-weight: 600;
}

.salary-cell {
  color: #e6a23c;
  font-weight: 600;
}

.year-cell {
  color: #409eff;
  font-weight: 500;
}
</style>

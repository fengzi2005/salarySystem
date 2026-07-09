<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Delete, Edit, Refresh, CircleCheck } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface SeniorityRule {
  id?: number; ruleName: string; ruleType: string; firstYearAmount: number
  incrementPerYear: number; effectiveDate: string; expireDate: string
  isActive: number
}

const loading = ref(false)
const tableData = ref<SeniorityRule[]>([])
const page = ref(1)
const pageSize = ref(15)
const total = ref(0)
const pagedData = computed(() => {
  total.value = tableData.value.length
  const start = (page.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})
const dialogVisible = ref(false)
const dialogTitle = ref('新增规则')
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)
const form = reactive<SeniorityRule>({
  ruleName: '', ruleType: 'OLD', firstYearAmount: 300, incrementPerYear: 300,
  effectiveDate: '', expireDate: '', isActive: 0
})
const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  firstYearAmount: [{ required: true, message: '请输入第一年金额', trigger: 'blur' }],
  incrementPerYear: [{ required: true, message: '请输入年递增金额', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try { const res = await request.get('/api/seniority-rule'); tableData.value = res.data.data || [] } finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增规则'; editingId.value = null
  Object.assign(form, { ruleName: '', ruleType: 'OLD', firstYearAmount: 300, incrementPerYear: 300, effectiveDate: '', expireDate: '', isActive: 0 })
  dialogVisible.value = true
}
function handleEdit(row: SeniorityRule) {
  dialogTitle.value = '编辑规则'; editingId.value = row.id!
  Object.assign(form, row)
  dialogVisible.value = true
}
async function handleDelete(row: SeniorityRule) {
  try {
    await ElMessageBox.confirm(`确定要删除规则 "${row.ruleName}" 吗？`, '删除确认', { type: 'warning' })
    await request.delete(`/api/seniority-rule/${row.id}`)
    ElMessage.success('删除成功'); loadData()
  } catch { /* cancelled */ }
}
async function handleEnable(row: SeniorityRule) {
  try {
    await ElMessageBox.confirm(
      `启用规则 "${row.ruleName}" 后，工龄工资将按此规则计算。旧规则将自动停用。确定继续？`,
      '切换规则确认', { type: 'warning' }
    )
    await request.put(`/api/seniority-rule/${row.id}/enable`)
    ElMessage.success('规则已生效，工龄工资自动使用新规则')
    loadData()
  } catch { /* cancelled */ }
}
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await request.put('/api/seniority-rule', { ...form, id: editingId.value })
      ElMessage.success('更新成功')
    } else {
      await request.post('/api/seniority-rule', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '操作失败') }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增规则</el-button>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      <el-alert type="info" show-icon :closable="false" style="margin-left: 16px; flex: 1" title="规则说明：系统同一时间只有一个规则生效。新规则生效后，工资计算时将自动按新规则匹配工龄工资。" />
    </div>

    <el-card shadow="hover">
      <div style="flex: 1; overflow: auto;">
        <el-table :data="pagedData" v-loading="loading" border stripe>
        <el-table-column prop="ruleName" label="规则名称" width="180" />
        <el-table-column label="规则类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.ruleType === 'NEW' ? 'success' : 'info'" size="small">
              {{ row.ruleType === 'NEW' ? '新规则' : '旧规则' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="firstYearAmount" label="第一年金额(元)" width="140" align="right" />
        <el-table-column prop="incrementPerYear" label="年递增(元)" width="110" align="right" />
        <el-table-column label="计算示例" min-width="220">
          <template #default="{ row }">
            <span style="color: #909399; font-size: 12px">
              工龄1年={{ row.firstYearAmount }}元，
              2年={{ row.firstYearAmount + row.incrementPerYear }}元，
              3年={{ row.firstYearAmount + row.incrementPerYear * 2 }}元...
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="110" />
        <el-table-column prop="expireDate" label="失效日期" width="110" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'" size="small">
              {{ row.isActive ? '生效中' : '未启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button v-if="!row.isActive" type="success" link size="small" :icon="CircleCheck" @click="handleEnable(row)">
              启用
            </el-button>
            <el-button type="primary" link size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)" v-if="!row.isActive">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <div style="display: flex; justify-content: center; padding: 14px 0 4px; flex-shrink: 0;">
        <el-pagination :current-page="page" :page-size="pageSize" :total="total" :page-sizes="[10, 15, 20, 50]" layout="total, sizes, prev, pager, next, jumper" @current-change="page = $event" @size-change="pageSize = $event; page = 1" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="如：新工龄工资规则" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-radio-group v-model="form.ruleType">
            <el-radio value="OLD">旧规则（每年+300）</el-radio>
            <el-radio value="NEW">新规则（第2年起+100）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="第一年金额" prop="firstYearAmount">
              <el-input-number v-model="form.firstYearAmount" :min="0" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年递增金额" prop="incrementPerYear">
              <el-input-number v-model="form.incrementPerYear" :min="0" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="生效日期">
              <el-date-picker v-model="form.effectiveDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失效日期">
              <el-date-picker v-model="form.expireDate" type="date" placeholder="可不填" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
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
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-shrink: 0; }
</style>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Delete, Edit, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Position {
  id?: number; positionName: string; positionType: string; positionSalary: number; description: string
}

const loading = ref(false)
const tableData = ref<Position[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增岗位')
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)
const form = reactive<Position>({ positionName: '', positionType: 'TECHNICAL', positionSalary: 0, description: '' })
const rules = {
  positionName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  positionType: [{ required: true, message: '请选择岗位类型', trigger: 'change' }],
  positionSalary: [{ required: true, message: '请输入岗位工资', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try { const res = await request.get('/api/position'); tableData.value = res.data.data || [] } finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增岗位'; editingId.value = null
  Object.assign(form, { positionName: '', positionType: 'TECHNICAL', positionSalary: 0, description: '' })
  dialogVisible.value = true
}
function handleEdit(row: Position) {
  dialogTitle.value = '编辑岗位'; editingId.value = row.id!
  Object.assign(form, row)
  dialogVisible.value = true
}
async function handleDelete(row: Position) {
  try {
    await ElMessageBox.confirm(`确定要删除岗位 "${row.positionName}" 吗？`, '删除确认', { type: 'warning' })
    await request.delete(`/api/position/${row.id}`)
    ElMessage.success('删除成功'); loadData()
  } catch { /* cancelled */ }
}
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await request.put('/api/position', { ...form, id: editingId.value })
      ElMessage.success('更新成功')
    } else {
      await request.post('/api/position', form)
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
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增岗位</el-button>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </div>
    <el-card shadow="hover">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="positionName" label="岗位名称" width="160" />
        <el-table-column label="岗位类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.positionType === 'MANAGEMENT' ? 'primary' : 'success'" size="small">
              {{ row.positionType === 'MANAGEMENT' ? '管理岗' : '技术岗' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="positionSalary" label="岗位工资(元/月)" width="140" align="right" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="岗位名称" prop="positionName">
          <el-input v-model="form.positionName" placeholder="如：高级开发工程师" />
        </el-form-item>
        <el-form-item label="岗位类型" prop="positionType">
          <el-radio-group v-model="form.positionType">
            <el-radio value="MANAGEMENT">管理岗</el-radio>
            <el-radio value="TECHNICAL">技术岗</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="岗位工资" prop="positionSalary">
          <el-input-number v-model="form.positionSalary" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
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
.page-container { max-width: 900px; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; }
</style>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Delete, Edit, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Title {
  id?: number; titleName: string; titleSalary: number; description: string
}

const loading = ref(false)
const tableData = ref<Title[]>([])
const page = ref(1)
const pageSize = ref(15)
const total = ref(0)
const pagedData = computed(() => {
  const sorted = [...tableData.value].sort((a, b) => b.titleSalary - a.titleSalary)
  total.value = sorted.length
  const start = (page.value - 1) * pageSize.value
  return sorted.slice(start, start + pageSize.value)
})
const dialogVisible = ref(false)
const dialogTitle = ref('新增职称')
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)
const form = reactive<Title>({ titleName: '', titleSalary: 0, description: '' })
const rules = {
  titleName: [{ required: true, message: '请输入职称名称', trigger: 'blur' }],
  titleSalary: [{ required: true, message: '请输入职级工资', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try { const res = await request.get('/api/title'); tableData.value = res.data.data || [] } finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增职称'; editingId.value = null
  Object.assign(form, { titleName: '', titleSalary: 0, description: '' })
  dialogVisible.value = true
}
function handleEdit(row: Title) {
  dialogTitle.value = '编辑职称'; editingId.value = row.id!
  Object.assign(form, row)
  dialogVisible.value = true
}
async function handleDelete(row: Title) {
  try {
    await ElMessageBox.confirm(`确定要删除职称 "${row.titleName}" 吗？`, '删除确认', { type: 'warning' })
    await request.delete(`/api/title/${row.id}`)
    ElMessage.success('删除成功'); loadData()
  } catch { /* cancelled */ }
}
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await request.put('/api/title', { ...form, id: editingId.value })
      ElMessage.success('更新成功')
    } else {
      await request.post('/api/title', form)
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
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增职称</el-button>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </div>
    <el-card shadow="never" style="border: none;">
      <div style="flex: 1; overflow: auto;">
        <el-table :data="pagedData" v-loading="loading" border stripe>
        <el-table-column label="职称名称" min-width="130" align="center">
          <template #default="{ row }">
            <span class="title-name-cell">{{ row.titleName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="职级工资" min-width="130" align="center">
          <template #default="{ row }">
            <span class="title-salary-cell">¥{{ row.titleSalary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="130" align="center" show-overflow-tooltip />
        <el-table-column label="操作" min-width="100" align="center">
          <template #default="{ row }">
            <el-button link class="title-action edit-btn" @click="handleEdit(row)">编辑</el-button>
            <el-button link class="title-action del-btn" @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="职称名称" prop="titleName">
          <el-input v-model="form.titleName" placeholder="如：正高级、副高级、中级" />
        </el-form-item>
        <el-form-item label="职级工资" prop="titleSalary">
          <el-input-number v-model="form.titleSalary" :min="0" :step="100" style="width: 100%" />
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
.page-container { height: calc(100vh - 100px); display: flex; flex-direction: column; }
.page-container :deep(.el-card) { flex: 1; display: flex; flex-direction: column; }
.page-container :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-shrink: 0; }

.title-name-cell {
  color: #1e4f8a;
  font-weight: 700;
}

.title-salary-cell {
  color: #e6a23c;
  font-weight: 600;
}

.title-action {
  font-size: 15px !important;
}

.edit-btn {
  color: #409eff !important;
}

.del-btn {
  color: #f56c6c !important;
}
</style>

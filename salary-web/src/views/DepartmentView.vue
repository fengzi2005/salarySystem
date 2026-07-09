<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Search, Delete, Edit, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Department {
  id?: number
  deptName: string
  parentId: number
  deptLevel: number
  managerId?: number
  sortOrder: number
  description: string
  children?: Department[]
}

const loading = ref(false)
const treeData = ref<Department[]>([])
const deptPage = ref(1)
const deptPageSize = ref(20)
const deptTotal = computed(() => {
  // 递归统计所有节点数量
  function count(nodes: Department[]): number {
    let c = nodes.length
    for (const n of nodes) {
      if (n.children && n.children.length > 0) c += count(n.children)
    }
    return c
  }
  return count(treeData.value)
})
const searchLevel = ref<number | null>(null)
const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const form = reactive<Department>({
  deptName: '',
  parentId: 0,
  deptLevel: 1,
  sortOrder: 1,
  description: ''
})

const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptLevel: [{ required: true, message: '请选择层级', trigger: 'change' }]
}

const allTreeData = ref<Department[]>([])

async function loadTree() {
  loading.value = true
  try {
    const res = await request.get('/api/department/tree')
    allTreeData.value = res.data.data || []
    applyFilter()
  } finally {
    loading.value = false
  }
}

function applyFilter() {
  if (!searchLevel.value) {
    treeData.value = allTreeData.value
    return
  }
  treeData.value = filterTreeByLevel(allTreeData.value, searchLevel.value)
}

function filterTreeByLevel(nodes: Department[], targetLevel: number): Department[] {
  const result: Department[] = []
  for (const node of nodes) {
    if (node.deptLevel === targetLevel) {
      // 匹配层级：只保留节点本身，去掉子节点
      result.push({ ...node, children: [] })
    } else if (node.children && node.children.length > 0) {
      // 未匹配：递归查找子节点
      const filteredChildren = filterTreeByLevel(node.children, targetLevel)
      if (filteredChildren.length > 0) {
        result.push({ ...node, children: filteredChildren })
      }
    }
  }
  return result
}

function handleAdd(parentId = 0, level = 1) {
  dialogTitle.value = '新增部门'
  editingId.value = null
  form.deptName = ''
  form.parentId = parentId
  form.deptLevel = parentId === 0 ? 1 : level
  form.sortOrder = getNextSortOrder(parentId)
  form.description = ''
  form.managerId = undefined
  dialogVisible.value = true
}

/** 获取同一父部门下的下一个可用序号（基于全量数据） */
function getNextSortOrder(parentId: number): number {
  const allDepts = flattenTree(allTreeData.value)
  const siblings = allDepts.filter(d => d.parentId === parentId)
  if (siblings.length === 0) return 1
  const maxOrder = Math.max(...siblings.map(d => d.sortOrder || 0))
  return maxOrder + 1
}

/** 递归展开树形数据 */
function flattenTree(nodes: Department[]): Department[] {
  let result: Department[] = []
  for (const node of nodes) {
    result.push(node)
    if (node.children && node.children.length > 0) {
      result = result.concat(flattenTree(node.children))
    }
  }
  return result
}

function handleEdit(row: Department) {
  dialogTitle.value = '编辑部门'
  editingId.value = row.id!
  form.deptName = row.deptName
  form.parentId = row.parentId
  form.deptLevel = row.deptLevel
  form.sortOrder = row.sortOrder
  form.description = row.description
  form.managerId = row.managerId
  dialogVisible.value = true
}

async function handleDelete(row: Department) {
  try {
    await ElMessageBox.confirm(`确定要删除部门 "${row.deptName}" 吗？`, '删除确认', { type: 'warning' })
    await request.delete(`/api/department/${row.id}`)
    ElMessage.success('删除成功')
    loadTree()
  } catch { /* cancelled */ }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 校验同一父部门下序号是否重复（基于全量数据）
  const allDepts = flattenTree(allTreeData.value)
  const duplicate = allDepts.find(d =>
    d.parentId === form.parentId &&
    d.sortOrder === form.sortOrder &&
    d.id !== editingId.value
  )
  if (duplicate) {
    ElMessage.warning(`序号 ${form.sortOrder} 已被同级部门"${duplicate.deptName}"使用，请更换序号`)
    return
  }

  try {
    if (editingId.value) {
      await request.put('/api/department', { ...form, id: editingId.value })
      ElMessage.success('更新成功')
    } else {
      await request.post('/api/department', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadTree()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

function getLevelText(level: number) {
  return { 1: '总部', 2: '一级部门', 3: '分部' }[level] || ''
}

function getLevelTag(level: number) {
  const map: Record<number, string> = { 1: '', 2: 'success', 3: 'warning' }
  return map[level] || 'info'
}

onMounted(loadTree)
</script>

<template>
  <div class="page-container">
    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleAdd(0, 1)">新增部门</el-button>
      <el-select v-model="searchLevel" placeholder="按层级筛选" clearable style="width: 140px; margin-left: 10px" @change="applyFilter">
        <el-option label="总部" :value="1" />
        <el-option label="一级部门" :value="2" />
        <el-option label="分部" :value="3" />
      </el-select>
      <el-button :icon="Refresh" @click="loadTree" style="margin-left: 10px">刷新</el-button>
    </div>

    <!-- 部门树形表格 -->
    <el-card shadow="hover">
      <div style="flex: 1; overflow: auto;">
        <el-table
        :data="treeData"
        v-loading="loading"
        row-key="id"
        border
        stripe
        default-expand-all
        :tree-props="{ children: 'children' }"
        style="width: 100%"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="78" align="center">
          <template #default="{ row }">
            <span :class="'dept-name-level-' + row.deptLevel">{{ row.deptName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="层级" width="122" align="center">
          <template #default="{ row }">
            <span :class="'level-text level-color-' + row.deptLevel">{{ getLevelText(row.deptLevel) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序号" width="90" align="center" />
        <el-table-column prop="description" label="描述" width="184" align="center" show-overflow-tooltip />
        <el-table-column label="操作" width="276" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link class="action-btn add-btn" @click="handleAdd(row.id, row.deptLevel + 1)">添加子部门</el-button>
            <el-button type="warning" link class="action-btn" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link class="action-btn" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <div style="display: flex; justify-content: center; padding: 14px 0 4px; flex-shrink: 0;">
        <el-pagination :current-page="deptPage" :page-size="deptPageSize" :total="deptTotal" :page-sizes="[10, 20, 50]" layout="total, prev, pager, next" @current-change="deptPage = $event" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="层级" prop="deptLevel">
          <el-select v-model="form.deptLevel" disabled style="width: 100%">
            <el-option label="总部" :value="1" />
            <el-option label="一级部门" :value="2" />
            <el-option label="分部" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="1" :max="999" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="部门描述（选填）" />
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
.page-container { font-size: 15px; height: calc(100vh - 100px); display: flex; flex-direction: column; }
.page-container :deep(.el-card) { flex: 1; display: flex; flex-direction: column; }
.page-container :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.toolbar { margin-bottom: 16px; display: flex; align-items: center; flex-shrink: 0; }

.level-text {
  font-size: 15px;
  font-weight: 600;
}

.level-color-1 {
  color: #1a3a6c;
}

.level-color-2 {
  color: #2c5fa1;
}

.level-color-3 {
  color: #4a90d9;
}

.dept-name-level-1 {
  color: #303133;
  font-size: 16px;
  font-weight: 700;
}

.dept-name-level-2 {
  color: #2c5fa1;
  font-size: 15px;
  font-weight: 600;
}

.dept-name-level-3 {
  color: #4a90d9;
  font-size: 14px;
  font-weight: 600;
}

.action-btn {
  font-size: 15px !important;
}

.add-btn {
  color: #409eff !important;
}
</style>

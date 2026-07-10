import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 认证状态管理
 * 管理用户登录状态、Token、角色信息
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const username = ref<string>(localStorage.getItem('username') || '')
  const roleCode = ref<string>(localStorage.getItem('roleCode') || '')
  const employeeId = ref<number | null>(
    localStorage.getItem('employeeId') ? Number(localStorage.getItem('employeeId')) : null
  )
  const employeeName = ref<string>(localStorage.getItem('employeeName') || '')
  const positionName = ref<string>(localStorage.getItem('positionName') || '')

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)

  /** 是否是管理员 */
  const isAdmin = computed(() => roleCode.value === 'ADMIN')

  /** 是否是管理人员 */
  const isManager = computed(() => roleCode.value === 'MANAGER')

  /** 是否是普通员工 */
  const isEmployee = computed(() => roleCode.value === 'EMPLOYEE')

  /**
   * 设置登录信息
   */
  function setLoginInfo(data: {
    token: string
    username: string
    roleCode: string
    employeeId: number | null
    employeeName: string
    positionName?: string
  }) {
    token.value = data.token
    username.value = data.username
    roleCode.value = data.roleCode
    employeeId.value = data.employeeId
    employeeName.value = data.employeeName
    positionName.value = data.positionName || ''

    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    localStorage.setItem('roleCode', data.roleCode)
    localStorage.setItem('employeeId', String(data.employeeId || ''))
    localStorage.setItem('employeeName', data.employeeName || '')
    localStorage.setItem('positionName', data.positionName || '')
  }

  /**
   * 清除登录信息（登出）
   */
  function clearLoginInfo() {
    token.value = ''
    username.value = ''
    roleCode.value = ''
    employeeId.value = null
    employeeName.value = ''
    positionName.value = ''

    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('roleCode')
    localStorage.removeItem('employeeId')
    localStorage.removeItem('employeeName')
    localStorage.removeItem('positionName')
  }

  return {
    token,
    username,
    roleCode,
    employeeId,
    employeeName,
    positionName,
    isLoggedIn,
    isAdmin,
    isManager,
    isEmployee,
    setLoginInfo,
    clearLoginInfo
  }
})

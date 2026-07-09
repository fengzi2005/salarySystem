import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 创建 Axios 实例
 * 配置基础URL、超时时间、拦截器
 */
const request: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 * 在每次请求前自动添加 Authorization 请求头
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 统一处理错误响应：
 * - 401：未登录/token过期，清除登录状态，重定向到登录页
 * - 403：没有权限
 * - 500：服务器错误
 */
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // 后端返回的 Result 格式
    if (res.code === 401) {
      ElMessage.error(res.message || '登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
      return Promise.reject(new Error(res.message))
    }
    if (res.code === 403) {
      ElMessage.error(res.message || '没有操作权限')
      return Promise.reject(new Error(res.message))
    }
    if (res.code !== 200 && res.code !== undefined) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message))
    }
    return response
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('没有操作权限')
      } else {
        ElMessage.error(error.response.data?.message || '服务器异常')
      }
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request

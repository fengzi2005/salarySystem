<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'
import logoImg from '@/assets/logo.png'

const router = useRouter()
const authStore = useAuthStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const formRef = ref()

/**
 * 处理登录
 */
async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await request.post('/api/auth/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    if (res.data.code === 200) {
      const data = res.data.data
      authStore.setLoginInfo({
        token: data.token,
        username: data.username,
        roleCode: data.roleCode,
        employeeId: data.employeeId,
        employeeName: data.employeeName || '',
        positionName: data.positionName || ''
      })
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.data.message || '登录失败')
    }
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || err.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <img :src="logoImg" alt="佛山大学" class="login-logo" />
      <h2 class="login-card-title">工资管理系统</h2>
      <p class="login-card-subtitle">用户登录</p>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="loginRules"
        size="large"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
            round
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tips">
        <p style="font-weight:600;color:#606266;margin-bottom:6px;">演示账号</p>
        <p>admin / 123456（管理员）</p>
        <p>sunqi / 123456（总经理）</p>
        <p>zhoumingyuan / 123456（普通员工）</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #e8f0fe 0%, #f0f4f8 50%, #e8f0fe 100%);
}

.login-card {
  width: 460px;
  background: #ffffff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 8px 32px rgba(44, 95, 161, 0.12);
}

.login-logo {
  display: block;
  width: 200px;
  height: auto;
  margin: 0 auto 24px;
}

.login-card-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a3a6c;
  text-align: center;
  margin-bottom: 6px;
  letter-spacing: 2px;
}

.login-card-subtitle {
  font-size: 13px;
  color: #909399;
  text-align: center;
  margin-bottom: 32px;
}

.login-form {
  margin-top: 8px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #4a90d9, #2c5fa1);
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #5da0e9, #3c6fb1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(44, 95, 161, 0.4);
}

.login-tips {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  text-align: center;
}

.login-tips p {
  font-size: 12px;
  color: #b0b3bb;
  line-height: 1.8;
}
</style>

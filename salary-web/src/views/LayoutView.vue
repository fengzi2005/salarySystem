<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  HomeFilled,
  OfficeBuilding,
  User,
  Briefcase,
  Medal,
  Clock,
  Setting,
  Money,
  DataAnalysis,
  Document,
  Timer,
  SwitchButton,
  UserFilled,
  Expand,
  Fold
} from '@element-plus/icons-vue'
import logoImg from '@/assets/logo.png'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapse = ref(false)

/** 侧边栏菜单项 */
const menuItems = computed(() => {
  const items = [
    { path: '/dashboard', title: '系统首页', icon: HomeFilled },
    { path: '/department', title: '部门管理', icon: OfficeBuilding },
    { path: '/employee', title: '员工管理', icon: User },
    { path: '/position', title: '岗位管理', icon: Briefcase },
    { path: '/title', title: '职称管理', icon: Medal },
    { path: '/attendance', title: '考勤管理', icon: Clock },
    { path: '/salary-config', title: '工资项配置', icon: Setting },
    { path: '/salary', title: '工资管理', icon: Money },
    { path: '/reports', title: '统计报表', icon: DataAnalysis },
    { path: '/seniority-rule', title: '工龄工资规则', icon: Timer }
  ]

  // 所有角色都有"我的工资"
  items.push({ path: '/my-salary', title: '我的工资', icon: Document })

  return items
})

/** 当前激活菜单项 */
const activeMenu = computed(() => {
  // 子路由匹配
  if (route.path.startsWith('/salary') && route.path !== '/salary-config') {
    return '/salary'
  }
  return route.path
})

/** 切换侧边栏折叠 */
function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

/** 退出登录 */
function handleLogout() {
  authStore.clearLoginInfo()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout-container">
    <!-- 左侧导航栏：从浅蓝到深蓝过渡 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <!-- Logo 区域 -->
      <div class="logo-section">
        <img :src="logoImg" alt="佛山大学" class="logo-img" />
      </div>

      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="transparent"
        text-color="#ffffff"
        active-text-color="#ffffff"
        router
        class="sidebar-menu"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>

      <!-- 底部操作区 -->
      <div class="aside-footer">
        <el-button
          :icon="isCollapse ? Expand : Fold"
          text
          @click="toggleCollapse"
          style="color: #fff; width: 100%"
        >
          <span v-show="!isCollapse">收起菜单</span>
        </el-button>
      </div>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container class="layout-right">
      <!-- 顶部标签栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <span class="header-title">{{ route.meta.title || '工资管理系统' }}</span>
        </div>
        <div class="header-right">
          <div class="header-user">
            <el-icon><UserFilled /></el-icon>
            <span class="user-name">{{ authStore.employeeName || authStore.username }}</span>
            <span class="user-role">{{ authStore.isAdmin ? '管理员' : authStore.isManager ? '管理人员' : '员工' }}</span>
            <el-divider direction="vertical" />
            <el-button link :icon="SwitchButton" @click="handleLogout" class="logout-btn">退出登录</el-button>
          </div>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="layout-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
}

/* 左侧导航栏：浅蓝→深蓝渐变 */
.layout-aside {
  background: linear-gradient(180deg, #3b7dd8 0%, #1e4f8a 35%, #0f2d54 70%, #091f3d 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

/* Logo 区域 */
.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  gap: 10px;
}

.logo-img {
  width: 200px;
  height: auto;
  display: block;
  margin: 0 auto;
}

/* 侧边栏菜单 */
.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  padding-top: 4px;
}

.sidebar-menu .el-menu-item {
  margin: 2px 8px;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.sidebar-menu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.2) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.25) !important;
  font-weight: 600;
}

/* 底部操作区 */
.aside-footer {
  padding: 10px 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* 右侧容器 */
.layout-right {
  flex-direction: column;
  background: #f0f4f8;
}

/* 顶部标签栏 */
.layout-header {
  background: linear-gradient(90deg, #f8fafd 0%, #ffffff 40%, #ffffff 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-bottom: 1px solid #e8ecf1;
  z-index: 10;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a3a6c;
  position: relative;
  padding-left: 16px;
}

.header-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 26px;
  background: linear-gradient(180deg, #3b7dd8, #1e4f8a);
  border-radius: 2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  color: #1e4f8a;
}

.header-user .user-name {
  font-weight: 600;
}

.header-user .user-role {
  font-size: 15px;
  color: #7b8ba3;
}

.header-user .logout-btn {
  font-size: 15px;
  color: #7b8ba3;
  height: auto;
  padding: 0;
}

.header-user .logout-btn:hover {
  color: #f56c6c;
}

/* 主内容区域 */
.layout-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f0f4f8;
}
</style>

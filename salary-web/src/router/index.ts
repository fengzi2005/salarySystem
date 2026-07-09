import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { noAuth: true }
    },
    {
      path: '/',
      component: () => import('@/views/LayoutView.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '系统首页' }
        },
        {
          path: 'department',
          name: 'department',
          component: () => import('@/views/DepartmentView.vue'),
          meta: { title: '部门管理' }
        },
        {
          path: 'employee',
          name: 'employee',
          component: () => import('@/views/EmployeeView.vue'),
          meta: { title: '员工管理' }
        },
        {
          path: 'position',
          name: 'position',
          component: () => import('@/views/PositionView.vue'),
          meta: { title: '岗位管理' }
        },
        {
          path: 'title',
          name: 'title',
          component: () => import('@/views/TitleView.vue'),
          meta: { title: '职称管理' }
        },
        {
          path: 'attendance',
          name: 'attendance',
          component: () => import('@/views/AttendanceView.vue'),
          meta: { title: '考勤管理' }
        },
        {
          path: 'salary-config',
          name: 'salaryConfig',
          component: () => import('@/views/SalaryConfigView.vue'),
          meta: { title: '工资项配置' }
        },
        {
          path: 'salary',
          name: 'salary',
          component: () => import('@/views/SalaryView.vue'),
          meta: { title: '月度工资管理' }
        },
        {
          path: 'my-salary',
          name: 'mySalary',
          component: () => import('@/views/MySalaryView.vue'),
          meta: { title: '我的工资' }
        },
        {
          path: 'reports',
          name: 'reports',
          component: () => import('@/views/ReportsView.vue'),
          meta: { title: '统计报表' }
        },
        {
          path: 'seniority-rule',
          name: 'seniorityRule',
          component: () => import('@/views/SeniorityRuleView.vue'),
          meta: { title: '工龄工资规则' }
        }
      ]
    }
  ]
})

/**
 * 路由守卫：检查登录状态
 * 未登录访问需要认证的页面 → 重定向到 /login
 */
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.noAuth) {
    // 不需要认证的页面（如登录页）
    if (token && to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    // 需要认证的页面
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router

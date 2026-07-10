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
          meta: { title: '系统首页', roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'] }
        },
        {
          path: 'department',
          name: 'department',
          component: () => import('@/views/DepartmentView.vue'),
          meta: { title: '部门管理', roles: ['ADMIN', 'MANAGER'] }
        },
        {
          path: 'employee',
          name: 'employee',
          component: () => import('@/views/EmployeeView.vue'),
          meta: { title: '员工管理', roles: ['ADMIN', 'MANAGER'] }
        },
        {
          path: 'position',
          name: 'position',
          component: () => import('@/views/PositionView.vue'),
          meta: { title: '岗位管理', roles: ['ADMIN'] }
        },
        {
          path: 'title',
          name: 'title',
          component: () => import('@/views/TitleView.vue'),
          meta: { title: '职称管理', roles: ['ADMIN'] }
        },
        {
          path: 'attendance',
          name: 'attendance',
          component: () => import('@/views/AttendanceView.vue'),
          meta: { title: '考勤管理', roles: ['ADMIN'] }
        },
        {
          path: 'salary-config',
          name: 'salaryConfig',
          component: () => import('@/views/SalaryConfigView.vue'),
          meta: { title: '工资项配置', roles: ['ADMIN'] }
        },
        {
          path: 'salary',
          name: 'salary',
          component: () => import('@/views/SalaryView.vue'),
          meta: { title: '工资管理', roles: ['ADMIN'] }
        },
        {
          path: 'my-salary',
          name: 'mySalary',
          component: () => import('@/views/MySalaryView.vue'),
          meta: { title: '薪资明细', roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'] }
        },
        {
          path: 'reports',
          name: 'reports',
          component: () => import('@/views/ReportsView.vue'),
          meta: { title: '统计报表', roles: ['ADMIN'] }
        },
        {
          path: 'seniority-rule',
          name: 'seniorityRule',
          component: () => import('@/views/SeniorityRuleView.vue'),
          meta: { title: '工龄工资规则', roles: ['ADMIN'] }
        }
      ]
    }
  ]
})

/**
 * 路由守卫：检查登录状态和角色权限
 */
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const roleCode = localStorage.getItem('roleCode') || ''

  if (to.meta.noAuth) {
    if (token && to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  // 角色权限检查
  const allowedRoles = to.meta.roles as string[] | undefined
  if (allowedRoles && !allowedRoles.includes(roleCode)) {
    next('/dashboard')
    return
  }

  next()
})

export default router

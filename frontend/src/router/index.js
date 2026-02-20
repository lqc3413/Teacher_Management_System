import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  // 教师端路由
  {
    path: '/teacher',
    component: () => import('../layout/TeacherLayout.vue'),
    meta: { requiresAuth: true, role: 'teacher' },
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('../views/teacher/Dashboard.vue')
      },
      {
        path: 'info-fill',
        name: 'InfoFill',
        component: () => import('../views/teacher/InfoFill.vue')
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('../views/teacher/History.vue')
      },
      {
        path: 'achievements',
        name: 'Achievements',
        component: () => import('../views/teacher/Achievements.vue')
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('../views/profile/Profile.vue')
      }
    ]
  },
  // 管理员端路由
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '控制台' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('../views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'depts',
        name: 'DeptManage',
        component: () => import('../views/admin/DeptManage.vue'),
        meta: { title: '部门管理' }
      },
      {
        path: 'tasks',
        name: 'TaskManage',
        component: () => import('../views/admin/TaskManage.vue'),
        meta: { title: '任务管理' }
      },
      {
        path: 'audit',
        name: 'MaterialAudit',
        component: () => import('../views/admin/MaterialAudit.vue'),
        meta: { title: '申报审核' }
      },
      {
        path: 'notices',
        name: 'NoticeManage',
        component: () => import('../views/admin/NoticeManage.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('../views/admin/SystemSettings.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/profile/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫 — 未登录用户跳转到登录页
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth !== false && !token) {
    // 需要登录但没有 Token，跳转到登录页
    next({ path: '/' })
  } else if (to.path === '/' && token) {
    // 已登录用户访问登录页，跳转到对应 Dashboard
    const role = localStorage.getItem('role')
    if (role === 'admin') {
      next({ path: '/admin/dashboard' })
    } else {
      next({ path: '/teacher/dashboard' })
    }
  } else {
    next()
  }
})

export default router

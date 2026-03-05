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
  // 部门主任端路由（复用 TeacherLayout，附加部门审核功能）
  {
    path: '/dept-director',
    component: () => import('../layout/TeacherLayout.vue'),
    meta: { requiresAuth: true, role: 'dept_director' },
    children: [
      {
        path: 'dashboard',
        name: 'DeptDirectorDashboard',
        component: () => import('../views/teacher/Dashboard.vue')
      },
      {
        path: 'audit',
        name: 'DeptAudit',
        component: () => import('../views/dept-director/DeptAudit.vue')
      },
      {
        path: 'info-fill',
        name: 'DeptInfoFill',
        component: () => import('../views/teacher/InfoFill.vue')
      },
      {
        path: 'history',
        name: 'DeptHistory',
        component: () => import('../views/teacher/History.vue')
      },
      {
        path: 'achievements',
        name: 'DeptAchievements',
        component: () => import('../views/teacher/Achievements.vue')
      },
      {
        path: 'profile',
        name: 'DeptProfile',
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
 * 路由守卫 — 未登录用户跳转到登录页，已登录用户校验角色
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  // 统一转小写，防止大小写不一致导致匹配失败
  const role = token ? (localStorage.getItem('role') || '').toLowerCase() : null

  // 根据角色获取对应的 Dashboard 路径
  function getDashboardPath(r) {
    if (r === 'admin') return '/admin/dashboard'
    if (r === 'dept_director') return '/dept-director/dashboard'
    return '/teacher/dashboard'
  }

  if (to.meta.requiresAuth !== false && !token) {
    // 需要登录但没有 Token，跳转到登录页
    next({ path: '/' })
  } else if (to.path === '/' && token) {
    // 已登录用户访问登录页，跳转到对应 Dashboard
    next({ path: getDashboardPath(role) })
  } else if (token && to.meta.role) {
    // 已登录用户访问受保护路由时，校验角色是否匹配
    const routeRole = to.meta.role

    // 部门主任可以访问教师端路由（因为同时拥有教师权限）
    const isAllowed = routeRole === role ||
      (role === 'dept_director' && routeRole === 'teacher')

    if (!isAllowed) {
      const target = getDashboardPath(role)
      // 如果目标路径就是当前路径，直接放行，避免无限重定向
      if (to.path === target) {
        next()
      } else {
        next({ path: target })
      }
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router


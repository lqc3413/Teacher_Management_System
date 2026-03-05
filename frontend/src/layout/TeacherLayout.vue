<template>
  <div class="teacher-layout teacher-theme">
    <!-- Left Sidebar -->
    <aside class="sidebar" :class="{ 'mobile-hidden': isMobile }">
      <div class="logo-area">
        <div class="logo-icon">
          <el-icon><Check /></el-icon>
        </div>
        <div class="logo-text">教科研平台</div>
      </div>

      <nav class="sidebar-menu">
        <router-link :to="routePrefix + '/dashboard'" class="nav-item" active-class="active">
          <el-icon><HomeFilled /></el-icon>
          <span class="label">工作台</span>
        </router-link>
        <router-link v-if="isDeptDirector" :to="routePrefix + '/audit'" class="nav-item" active-class="active">
          <el-icon><Stamp /></el-icon>
          <span class="label">部门审核</span>
        </router-link>
        <router-link :to="routePrefix + '/info-fill'" class="nav-item" active-class="active">
          <el-icon><EditPen /></el-icon>
          <span class="label">信息填报</span>
        </router-link>
        <router-link :to="routePrefix + '/history'" class="nav-item" active-class="active">
          <el-icon><List /></el-icon>
          <span class="label">填报记录</span>
        </router-link>
        <router-link :to="routePrefix + '/achievements'" class="nav-item" active-class="active">
          <el-icon><Trophy /></el-icon>
          <span class="label">我的成果</span>
        </router-link>
        <router-link :to="routePrefix + '/profile'" class="nav-item" active-class="active">
          <el-icon><User /></el-icon>
          <span class="label">个人中心</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="version-text">v2.0</div>
      </div>
    </aside>

    <!-- Top Header (Context & Actions) -->
    <header class="top-header" :class="{ 'mobile-header': isMobile }">
      <div class="header-left">
        <!-- Mobile Toggle -->
        <button v-if="isMobile" class="mobile-toggle" @click="mobileDrawerVisible = true">
          <el-icon><Expand /></el-icon>
        </button>
        <!-- Page Context -->
        <div class="page-context" v-if="!isMobile">
          <span class="crumb-root">{{ isDeptDirector ? '部门主任' : '教师端' }}</span>
          <span class="crumb-sep">/</span>
          <span class="crumb-current">{{ currentRouteName }}</span>
        </div>
      </div>

      <!-- User Actions -->
      <div class="user-actions">
        <!-- Notification Bell -->
        <div class="notification-bell" @click="notifDrawerVisible = true">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notif-badge">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>
        </div>

        <el-dropdown @command="handleCommand" trigger="click">
          <div class="user-profile">
            <div class="avatar-circle">{{ userInitials }}</div>
            <span class="user-name" v-if="!isMobile">{{ realName }}</span>
            <el-icon class="dropdown-icon" v-if="!isMobile"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="teacher-dropdown">
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- Mobile Drawer for Navigation -->
    <el-drawer
      v-model="mobileDrawerVisible"
      direction="ltr"
      size="280px"
      :with-header="false"
      class="teacher-mobile-drawer"
    >
      <div class="drawer-sidebar">
        <div class="logo-area">
          <div class="logo-icon">E</div>
          <div class="logo-text">EduCert</div>
        </div>
        <nav class="mobile-nav">
          <router-link :to="routePrefix + '/dashboard'" class="mobile-nav-item" active-class="active" @click="mobileDrawerVisible = false">
            <el-icon><HomeFilled /></el-icon> 工作台
          </router-link>
          <router-link v-if="isDeptDirector" :to="routePrefix + '/audit'" class="mobile-nav-item" active-class="active" @click="mobileDrawerVisible = false">
            <el-icon><Stamp /></el-icon> 部门审核
          </router-link>
          <router-link :to="routePrefix + '/info-fill'" class="mobile-nav-item" active-class="active" @click="mobileDrawerVisible = false">
            <el-icon><EditPen /></el-icon> 信息填报
          </router-link>
          <router-link :to="routePrefix + '/history'" class="mobile-nav-item" active-class="active" @click="mobileDrawerVisible = false">
            <el-icon><List /></el-icon> 填报记录
          </router-link>
          <router-link :to="routePrefix + '/achievements'" class="mobile-nav-item" active-class="active" @click="mobileDrawerVisible = false">
            <el-icon><Trophy /></el-icon> 我的成果
          </router-link>
          <router-link :to="routePrefix + '/profile'" class="mobile-nav-item" active-class="active" @click="mobileDrawerVisible = false">
            <el-icon><User /></el-icon> 个人中心
          </router-link>
        </nav>
      </div>
    </el-drawer>

    <!-- Main Content -->
    <main class="main-content" :class="{ 'mobile-content': isMobile }">
      <div class="content-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- Notification Drawer -->
    <NotificationDrawer v-model="notifDrawerVisible" @refresh-count="fetchUnreadCount" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, EditPen, List, Expand, User, Bell, ArrowDown, Check, Trophy, Stamp } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useIsMobile } from '../hooks/useIsMobile'
import NotificationDrawer from '../components/NotificationDrawer.vue'
import { getUnreadCount } from '../api/notification'

const router = useRouter()
const route = useRoute()
const { isMobile } = useIsMobile()
const mobileDrawerVisible = ref(false)
const notifDrawerVisible = ref(false)
const realName = ref('Teacher')

// 判断当前用户是否为部门主任
const userRole = ref(localStorage.getItem('role') || 'teacher')
const isDeptDirector = computed(() => userRole.value === 'dept_director')
const routePrefix = computed(() => isDeptDirector.value ? '/dept-director' : '/teacher')

// Helper for initials
const userInitials = computed(() => {
  return realName.value ? realName.value.charAt(0).toUpperCase() : 'T'
})

// Route Name Map for Header
const routeNameMap = {
  'TeacherDashboard': '工作台',
  'DeptDirectorDashboard': '工作台',
  'DeptAudit': '部门审核',
  'InfoFill': '信息填报',
  'DeptInfoFill': '信息填报',
  'History': '填报记录',
  'DeptHistory': '填报记录',
  'Achievements': '我的成果',
  'DeptAchievements': '我的成果',
  'TeacherProfile': '个人中心',
  'DeptProfile': '个人中心'
}
const currentRouteName = computed(() => routeNameMap[route.name] || '工作台')

// Notifications
const unreadCount = ref(0)
let pollTimer = null

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      realName.value = userInfo.realName || userInfo.username || 'Teacher'
    } catch (e) {
      console.error('Error parsing user info', e)
    }
  }

  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push(routePrefix.value + '/profile')
    } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'teacher-logout-box teacher-theme', 
      confirmButtonClass: 'teacher-logout-confirm',
    }).then(() => {
      localStorage.clear()
      ElMessage.success('已退出登录')
      router.replace('/')
    })
  }
}
</script>

<style scoped lang="scss">
.teacher-layout {
  min-height: 100vh;
  background-color: var(--color-bg);
  display: flex;
}

/* Sidebar Styling - Pro Max (Luminous Enterprise) */
.sidebar {
  width: 256px;
  background-color: var(--color-sidebar); /* White */
  color: var(--color-text);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 1000;
  border-right: 1px solid var(--color-border);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &.mobile-hidden {
    display: none;
  }

  .logo-area {
    height: 72px;
    display: flex;
    align-items: center;
    padding: 0 24px;
    
    .logo-icon {
        width: 32px;
        height: 32px;
        background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-primary) 100%);
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        margin-right: 12px;
        box-shadow: none;
        
        .el-icon {
          font-size: 18px;
          font-weight: 700;
        }
    }
    
    .logo-text {
      font-family: var(--font-heading);
      font-size: 20px;
      font-weight: 700;
      color: var(--color-text);
      letter-spacing: -0.02em;
      margin-right: 8px;
    }

    .role-badge {
      font-family: var(--font-body);
      font-size: 10px;
      padding: 2px 8px;
      border-radius: 999px;
      background: rgba(0, 0, 0, 0.05);
      color: var(--color-text-light);
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      border: 1px solid transparent;
    }
  }

  .sidebar-menu {
    flex: 1;
    padding: 24px 16px;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 12px;
      color: var(--color-text-light); /* Zinc 500 */
      text-decoration: none;
      border-radius: 6px;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      font-size: 14px;
      font-weight: 500;
      font-family: var(--font-body);

      .el-icon {
        font-size: 18px;
        color: #64748B;
        transition: color 0.2s ease;
      }

      &:hover {
        color: var(--color-primary);
        background: rgba(0, 0, 0, 0.03);
        
        .el-icon {
          color: var(--color-primary);
        }
      }

      &.active {
        color: #fff;
        background: var(--color-primary);
        box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
        
        .el-icon {
            color: #fff;
        }
      }
    }
  }

  .sidebar-footer {
    padding: 24px;
    margin: 0 16px 16px;
    
    .version-text {
      font-family: var(--font-heading);
      font-size: 12px;
      color: #475569;
      text-align: center;
    }
  }
}

/* Top Header Styling */
.top-header {
  position: fixed;
  top: 0;
  right: 0;
  left: 256px;
  height: 72px;
  background: rgba(255, 255, 255, 0.85); /* Proper glass effect base */
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  z-index: 900;
  transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &.mobile-header {
    left: 0;
    padding: 0 20px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .mobile-toggle {
      background: none;
      border: none;
      font-size: 20px;
      color: var(--color-secondary);
      padding: 4px;
      cursor: pointer;
    }
    
    .page-context {
      font-family: var(--font-heading);
      font-size: 15px;
      font-weight: 500;
      color: var(--color-text);
      display: flex;
      align-items: center;
      gap: 10px;
      
      .crumb-root {
        color: var(--color-text-light);
      }
      
      .crumb-sep {
        color: #CBD5E1;
        font-size: 12px;
      }
      
      .crumb-current {
        font-weight: 600;
        color: var(--color-text);
        background: rgba(15, 23, 42, 0.03);
        padding: 4px 10px;
        border-radius: 6px;
      }
    }
  }

  .user-actions {
    display: flex;
    align-items: center;
    gap: 16px;

    .notification-bell {
      cursor: pointer;
      color: var(--color-secondary);
      transition: all 0.2s;
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 10px;

      &:hover {
        color: var(--color-primary);
        background: #EFF6FF; /* Indigo 50 */
      }
      
      :deep(.el-badge__content) {
        border: 2px solid #fff;
      }
    }

    .user-profile {
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      padding: 4px;
      padding-right: 12px;
      border-radius: 99px;
      transition: all 0.2s;
      border: 1px solid transparent;
      
      &:hover {
        background: #fff;
        border-color: var(--color-border);
        box-shadow: var(--shadow-sm);
      }

      .avatar-circle {
        width: 32px;
        height: 32px;
        background: var(--avatar-bg);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        
        font-weight: 600;
        color: #fff;
        font-family: var(--font-heading);
        font-size: 13px;
        box-shadow: none;
      }

      .user-name {
        font-size: 14px;
        font-weight: 500;
        color: var(--color-text);
        font-family: var(--font-body);
      }
      
      .dropdown-icon {
        font-size: 12px;
        color: #94A3B8;
      }
    }
  }
}

/* Main Content Area */
.main-content {
  flex: 1;
  margin-left: 256px;
  padding-top: 72px;
  min-height: 100vh;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  width: calc(100% - 256px);
  box-sizing: border-box;
  min-width: 0; /* Let flex item shrink below minimum content width */
  
  /* Subtle dot pattern background */
  background-color: #F8FAFC;
  background-image: radial-gradient(#E2E8F0 1px, transparent 1px);
  background-size: 24px 24px;
  
  &.mobile-content {
    margin-left: 0;
    width: 100%;
  }

  .content-wrapper {
    max-width: 1280px; /* Wider for Pro Max feel */
    margin: 0 auto;
    padding: 32px 40px;
    box-sizing: border-box;
    width: 100%;
    
    @media (max-width: 768px) {
      padding: 24px 16px;
    }
  }
}

/* Mobile Drawer Styling */
.teacher-mobile-drawer {
    :deep(.el-drawer__body) {
        padding: 0;
        background: var(--color-sidebar);
    }
}

.drawer-sidebar {
    height: 100%;
    background: var(--color-sidebar);
    color: #fff;
    display: flex;
    flex-direction: column;
    
    .logo-area {
        height: 72px;
        padding: 0 20px;
        display: flex;
        align-items: center;
        border-bottom: 1px solid rgba(255,255,255,0.05);
        gap: 12px;
        
        .logo-icon {
            width: 32px;
            height: 32px;
            background: var(--color-primary);
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
        }

        .logo-text {
            font-family: var(--font-heading);
            font-size: 18px;
            font-weight: 700;
        }
    }
    
    .mobile-nav {
        padding: 20px 12px;
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        .mobile-nav-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px;
            color: var(--color-text); /* Use dark text for visibility on light bg */
            text-decoration: none;
            border-radius: 6px;
            font-family: var(--font-body);
            font-weight: 500;
            
            &.active {
                background: var(--color-primary);
                color: #fff;
            }
        }
    }
}
</style>

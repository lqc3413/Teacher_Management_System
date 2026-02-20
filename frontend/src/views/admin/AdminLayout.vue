<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="logo-area">
        <el-icon :size="24" color="#fff"><Monitor /></el-icon>
        <span class="logo-text">教科研管理系统</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#1e293b"
        text-color="#94a3b8"
        active-text-color="#fff"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/depts">
          <el-icon><OfficeBuilding /></el-icon>
          <span>部门管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/audit">
          <el-icon><DocumentChecked /></el-icon>
          <span>申报审核</span>
        </el-menu-item>

        <el-menu-item index="/admin/notices">
          <el-icon><Notification /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
      <!-- Header -->
      <header class="top-header">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="user-actions">
          <!-- 通知铃铛 -->
          <div class="notification-bell" @click="drawerVisible = true">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
              <el-icon :size="22"><Bell /></el-icon>
            </el-badge>
          </div>

          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">管理员</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Page View -->
      <div class="page-container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- 通知抽屉 -->
    <NotificationDrawer v-model="drawerVisible" @refresh-count="fetchUnreadCount" />
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Monitor, DataBoard, User, Document, Setting, UserFilled, ArrowDown, DocumentChecked, OfficeBuilding, Bell, List, Notification } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import NotificationDrawer from '../../components/NotificationDrawer.vue'
import { getUnreadCount } from '../../api/notification'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => route.meta.title || '当前页面')

// 通知相关
const drawerVisible = ref(false)
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
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/admin/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.clear()
      router.push('/')
    })
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  min-height: 100vh;
  // Background handled in global style.css
}

.sidebar {
  width: 260px;
  background-color: var(--color-primary); /* Deep Stone */
  color: #a8a29e;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.1);
  z-index: 10;
  transition: all 0.3s ease;

  .logo-area {
    height: 80px;
    display: flex;
    align-items: center;
    padding: 0 24px;
    gap: 12px;
    background: linear-gradient(180deg, rgba(255,255,255,0.03) 0%, transparent 100%);

    .logo-text {
      font-size: 20px;
      font-weight: 700;
      color: #fff;
      font-family: var(--font-heading);
      letter-spacing: 0.5px;
      
      background: linear-gradient(to right, #fff, #fbbf24);
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }

  .el-menu-vertical {
    border-right: none;
    flex: 1;
    padding: 20px 12px;
    background: transparent !important;

    :deep(.el-menu-item) {
      height: 52px;
      margin-bottom: 8px;
      border-radius: 12px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid transparent;
      
      &.is-active {
        background: linear-gradient(90deg, rgba(202, 138, 4, 0.15) 0%, transparent 100%) !important;
        color: #fbbf24 !important; /* Gold */
        border-left: 3px solid #fbbf24;
        font-weight: 600;
        
        .el-icon {
          color: #fbbf24;
        }
      }
      
      &:hover:not(.is-active) {
        background-color: rgba(255, 255, 255, 0.03) !important;
        color: #fff !important;
        transform: translateX(4px);
      }

      .el-icon {
        transition: transform 0.3s ease;
      }
      
      &:hover .el-icon {
        transform: scale(1.1);
      }
    }
  }
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;

  .top-header {
    height: 70px;
    margin: 16px 24px 0;
    border-radius: 16px;
    
    /* Glass Effect */
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.6);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
    
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 32px;
    z-index: 5;

    .breadcrumb {
      :deep(.el-breadcrumb__inner) {
        font-weight: 500;
        
        &.is-link:hover {
          color: var(--color-cta);
        }
      }
    }

    .user-actions {
      display: flex;
      align-items: center;
      gap: 16px;

      .notification-bell {
        cursor: pointer;
        padding: 8px;
        border-radius: 50%;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        color: #6b7280;

        &:hover {
          background: rgba(0, 0, 0, 0.04);
          color: var(--color-cta);
        }
      }

      .el-dropdown-link {
        display: flex;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        padding: 6px 12px;
        border-radius: 20px;
        transition: background 0.2s;
        
        &:hover {
          background: rgba(0,0,0,0.03);
        }

        .username {
          font-size: 14px;
          font-weight: 600;
          color: var(--color-primary);
        }
      }
    }
  }

  .page-container {
    flex: 1;
    padding: 24px;
    overflow-y: auto;
    
    /* Custom Scrollbar */
    &::-webkit-scrollbar {
      width: 6px;
    }
    &::-webkit-scrollbar-thumb {
      background: rgba(0,0,0,0.1);
      border-radius: 3px;
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>

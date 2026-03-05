<template>
  <div class="dashboard-home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.teacherCount }}</div>
            <div class="stat-label">注册教师</div>
          </div>
          <el-icon class="stat-icon" color="#3b82f6"><User /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingCount }}</div>
            <div class="stat-label">待审核申报</div>
          </div>
          <el-icon class="stat-icon" color="#eab308"><Timer /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalAchievements }}</div>
            <div class="stat-label">累计成果</div>
          </div>
          <el-icon class="stat-icon" color="#10b981"><Trophy /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.monthlyCount }}</div>
            <div class="stat-label">本月新增</div>
          </div>
          <el-icon class="stat-icon" color="#8b5cf6"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <div class="section-header">
      <div class="section-title">快捷入口</div>
    </div>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="action-card" @click="$router.push('/admin/users')">
          <div class="action-icon bg-blue">
            <el-icon><User /></el-icon>
          </div>
          <div class="action-info">
            <h4>用户管理</h4>
            <p>管理系统用户、角色及部门</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="action-card" @click="$router.push('/admin/audit')">
          <div class="action-icon bg-yellow">
            <el-icon><DocumentChecked /></el-icon>
          </div>
          <div class="action-info">
            <h4>申报审核</h4>
            <p>审核教师提交的教学科研信息</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="action-card" @click="$router.push('/admin/settings')">
          <div class="action-icon bg-purple">
            <el-icon><Setting /></el-icon>
          </div>
          <div class="action-info">
            <h4>系统设置</h4>
            <p>配置系统参数、管理通知公告</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待审核申报列表 -->
    <div class="section-header">
      <div class="section-title">待审核申报</div>
      <el-button link type="primary" @click="$router.push('/admin/audit')">
        查看全部 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
      </el-button>
    </div>
    
    <el-card shadow="never" class="table-card">
      <el-table :data="pendingList" style="width: 100%" v-loading="loading">
        <el-table-column prop="teacherName" label="教师姓名" width="120" />
        <el-table-column prop="deptName" label="所在学院" show-overflow-tooltip />
        <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default>
            <el-button type="primary" link size="small" @click="$router.push('/admin/audit')">
              <el-icon style="margin-right: 4px"><DocumentChecked /></el-icon>审核
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无待审核申报" :image-size="60" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Timer, Trophy, TrendCharts, DocumentChecked, Setting, ArrowRight } from '@element-plus/icons-vue'
import { getAdminStats, getSubmissionList } from '../../api/admin'
import dayjs from 'dayjs'

const stats = ref({
  teacherCount: 0,
  pendingCount: 0,
  totalAchievements: 0,
  monthlyCount: 0
})

const pendingList = ref([])
const loading = ref(false)

onMounted(async () => {
  fetchStats()
  fetchPendingList()
})

async function fetchStats() {
  try {
    const res = await getAdminStats()
    stats.value = res.data
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

async function fetchPendingList() {
  loading.value = true
  try {
    const res = await getSubmissionList({
      pageNum: 1,
      pageSize: 5,
      status: 0 // 0=Pending
    })
    pendingList.value = res.data.records
  } catch (error) {
    console.error('获取待审核列表失败', error)
  } finally {
    loading.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped lang="scss">
.dashboard-home {
  .stat-card {
    height: 100%;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 32px 24px;
    }

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 32px rgba(202, 138, 4, 0.1);
      background: rgba(255, 255, 255, 0.9);
      border-color: rgba(202, 138, 4, 0.2);
    }

    .stat-content {
      .stat-value {
        font-family: var(--font-heading);
        font-size: 36px;
        font-weight: 600;
        color: var(--color-primary);
        line-height: 1.2;
        margin-bottom: 4px;
        letter-spacing: -1px;
      }
      .stat-label {
        font-family: var(--font-body);
        color: #78716c; /* Stone 500 */
        font-size: 14px;
        font-weight: 500;
        letter-spacing: 0.5px;
        text-transform: uppercase;
      }
    }

    .stat-icon {
      font-size: 48px;
      color: var(--color-cta); /* All icons Gold */
      opacity: 0.15;
      transition: all 0.3s ease;
    }
    
    &:hover .stat-icon {
      opacity: 0.8;
      transform: scale(1.1) rotate(5deg);
    }
  }

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 32px 0 16px;
    padding-right: 4px;

    .section-title {
      font-size: 20px;
      font-weight: 700;
      font-family: var(--font-heading);
      color: var(--color-primary);
      padding-left: 12px;
      border-left: 4px solid var(--color-cta);
      letter-spacing: -0.5px;
      margin: 0;
    }
  }

  /* Old section-title style override/removal if needed, currently reusing logic but cleaner structure */

  .action-card {
    height: 100%;
    cursor: pointer;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover {
      transform: translateY(-4px);
      background: #fff;
      box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
      border-color: rgba(202, 138, 4, 0.3);

      .action-icon {
        transform: scale(1.1);
        box-shadow: 0 8px 16px rgba(202, 138, 4, 0.2);
      }
    }

    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      padding: 24px;
    }

    .action-icon {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20px;
      color: #fff;
      font-size: 24px;
      transition: all 0.3s ease;

      /* All icons now use Gold gradient for luxury feel */
      background: linear-gradient(135deg, #CA8A04 0%, #A16207 100%);
      box-shadow: 0 4px 12px rgba(202, 138, 4, 0.15);
    }

    .action-info {
      h4 {
        margin: 0 0 6px;
        font-size: 18px;
        font-weight: 600;
        font-family: var(--font-heading);
        color: var(--color-primary);
      }
      p {
        margin: 0;
        font-size: 13px;
        color: #78716c;
        line-height: 1.4;
      }
    }
  }

  .table-card {
    border-radius: 16px;
    border: 1px solid rgba(255, 255, 255, 0.5);
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    
    :deep(.el-table) {
      background: transparent;
      --el-table-tr-bg-color: transparent;
      --el-table-header-bg-color: rgba(202, 138, 4, 0.05);
      
      th.el-table__cell {
        background: rgba(202, 138, 4, 0.05);
        color: var(--color-primary);
        font-weight: 600;
      }
    }
  }
}
</style>

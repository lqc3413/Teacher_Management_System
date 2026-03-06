<template>
  <div class="dashboard-container">
    <!-- 1. Header Area: Welcome + Quick Info -->
    <div class="header-section">
      <div class="welcome-box">
        <div class="date-badge">
          <el-icon><Calendar /></el-icon>
          {{ currentDate }}
        </div>
        <h1 class="welcome-title">欢迎回来，{{ user.realName }}老师</h1>
        <p class="welcome-subtitle">今天是致力于教学与科研的一天</p>
      </div>
      <div class="header-decoration"></div>
    </div>

    <div class="main-grid">
      <!-- Left Column: Status & Actions -->
      <div class="left-col">
        <!-- 2. Filing Status (显示当前任务状态) -->
        <div v-if="activeTask" class="section-card status-card">
          <div class="card-header">
            <h3>{{ activeTask.taskName }}</h3>
            <span class="status-tag" :class="isSubmitted ? 'done' : 'pending'">
              {{ isSubmitted ? '已完成' : '待填报' }}
            </span>
          </div>
          <div class="status-timeline">
            <!-- Step 1: Fill -->
            <div class="step-item active">
              <div class="step-icon"><el-icon><EditPen /></el-icon></div>
              <div class="step-content">
                <span class="step-title">资料填报</span>
                <span class="step-desc">填写教学科研成果</span>
              </div>
            </div>
            <!-- Line -->
            <div class="step-line" :class="{ active: isSubmitted }"></div>
            <!-- Step 2: Submit -->
            <div class="step-item" :class="{ active: isSubmitted }">
              <div class="step-icon">
                <el-icon v-if="isSubmitted"><Check /></el-icon>
                <el-icon v-else><Upload /></el-icon>
              </div>
              <div class="step-content">
                <span class="step-title">提交审核</span>
                <span class="step-desc">{{ isSubmitted ? formatTime(submitTime) : '等待提交' }}</span>
              </div>
            </div>
            <!-- Line -->
            <div class="step-line" :class="{ active: currentStatus === 1 }"></div>
            <!-- Step 3: Review / Archive -->
            <div class="step-item" :class="{ active: currentStatus >= 0 }">
              <div class="step-icon">
                <el-icon v-if="currentStatus === 1"><CircleCheckFilled /></el-icon>
                <el-icon v-else><Finished /></el-icon>
              </div>
              <div class="step-content">
                <span class="step-title">{{ currentStatus === 1 ? '审核完毕' : '审核入库' }}</span>
                <span class="step-desc" :class="{ 'text-primary': currentStatus === 1 }">
                  {{ currentStatus === 1 ? '已归档' : '管理员审核中' }}
                </span>
              </div>
            </div>
          </div>

          <div class="task-deadline-bar">
            <div class="left-info">
              <el-icon class="deadline-icon"><Timer /></el-icon>
              <span class="deadline-label">截止时间：{{ formatTaskTime(activeTask.endTime) }}</span>
            </div>
            <span v-if="countdown" class="countdown-tag">{{ countdown }}</span>
          </div>
          
          <div class="card-footer">
            <el-button v-if="!isSubmitted" type="primary" class="action-btn" @click="$router.push({ path: '/teacher/info-fill', query: { taskId: activeTask.id } })">
              立即填报 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
            <el-button v-else class="action-btn" @click="$router.push('/teacher/history')">
              查看历史记录
            </el-button>
          </div>
        </div>

        <!-- 无活动任务时的占位卡片 -->
        <div v-else class="section-card status-card no-task-card">
          <div class="card-header">
            <h3>当前任务</h3>
            <span class="status-tag" style="background: #e5e7eb; color: #78716c;">暂无</span>
          </div>
          <div class="no-task-content">
            <el-icon class="no-task-icon"><Finished /></el-icon>
            <p>当前没有进行中的采集任务</p>
            <span>管理员发布新任务后，这里会显示任务详情和填报入口</span>
          </div>
        </div>

        <!-- 3. Quick Actions Grid -->
        <div class="quick-actions-grid">
          <div class="action-card" :class="{ disabled: !activeTask }" @click="activeTask ? $router.push({ path: '/teacher/info-fill', query: { taskId: activeTask.id } }) : null">
            <div class="icon-wrapper"><el-icon><DocumentAdd /></el-icon></div>
            <span class="label">新增申报</span>
          </div>
          <div class="action-card" @click="$router.push('/teacher/history')">
            <div class="icon-wrapper"><el-icon><DataLine /></el-icon></div>
            <span class="label">历史档案</span>
          </div>
           <div class="action-card" @click="$router.push('/teacher/profile')">
            <div class="icon-wrapper"><el-icon><User /></el-icon></div>
            <span class="label">个人中心</span>
          </div>
          <div class="action-card">
            <div class="icon-wrapper"><el-icon><Setting /></el-icon></div>
            <span class="label">系统设置</span>
          </div>
        </div>
      </div>

      <!-- Right Column: Stats & Notices -->
      <div class="right-col">
        <!-- 4. Key Stats (Compact) -->
        <div class="stats-row">
            <div class="mini-stat">
                <div class="label">年度累计</div>
                <div class="value">{{ stats.yearlyCount }} <span class="unit">次</span></div>
            </div>
            <div class="mini-stat">
                <div class="label">总计成果</div>
                <div class="value">{{ stats.totalAchievements }} <span class="unit">项</span></div>
            </div>
        </div>

        <!-- 5. Notices -->
        <div class="section-card notice-card">
          <div class="card-header">
            <h3>最新公告</h3>
            <a href="javascript:;" class="view-all">全部</a>
          </div>
          <div class="notice-list-compact">
            <div 
              v-for="item in notices" 
              :key="item.id" 
              class="notice-item-compact"
              @click="showNoticeDetail(item)"
            >
              <span class="dot"></span>
              <span class="text">{{ item.title }}</span>
              <span class="date">{{ item.date }}</span>
            </div>
             <div v-if="notices.length === 0" class="empty-text">暂无公告</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Notice Dialog -->
    <el-dialog
      v-model="noticeDialogVisible"
      :title="currentNotice.title"
      width="600px"
      class="minimal-dialog"
      destroy-on-close
    >
      <div class="notice-meta">
        <el-tag size="small" effect="plain">{{ currentNotice.category }}</el-tag>
        <span class="time">{{ currentNotice.date }}</span>
      </div>
      <div class="notice-body">
        {{ currentNotice.content }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { getDashboardStats, getSubmissionHistory, getCurrentTask } from '../../api/teacher' // Import getSubmissionHistory
import { getPublishedNotices } from '../../api/admin'
import { 
  Calendar, EditPen, Upload, Check, Finished, ArrowRight, CircleCheckFilled,
  DocumentAdd, DataLine, User, Setting, Bell, Timer
} from '@element-plus/icons-vue'

const router = useRouter()
const user = ref({ realName: '教师' })
const currentDate = ref(dayjs().format('YYYY年MM月DD日'))
const isSubmitted = ref(false)
const currentStatus = ref(-1)
const submitTime = ref(null)

const activeTask = ref(null)
const taskSubmitted = ref(false)
const countdown = ref('')
let countdownTimer = null

const stats = ref({
  yearlyCount: 0,
  totalAchievements: 0
})

const notices = ref([])
const noticeDialogVisible = ref(false)
const currentNotice = ref({})

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('MM-DD HH:mm')
}

const formatTaskTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

function updateCountdown() {
  if (!activeTask.value) return
  const now = dayjs()
  const end = dayjs(activeTask.value.endTime)
  const diff = end.diff(now, 'second')
  if (diff <= 0) {
    countdown.value = '已截止'
    clearInterval(countdownTimer)
    return
  }
  const days = Math.floor(diff / 86400)
  const hours = Math.floor((diff % 86400) / 3600)
  const minutes = Math.floor((diff % 3600) / 60)
  if (days > 0) {
    countdown.value = `剩余 ${days}天${hours}小时`
  } else {
    countdown.value = `剩余 ${hours}小时${minutes}分钟`
  }
}

const showNoticeDetail = (item) => {
  currentNotice.value = item
  noticeDialogVisible.value = true
}

onMounted(async () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    user.value = JSON.parse(userInfoStr)
  }

  try {
    const res = await getDashboardStats()
    isSubmitted.value = res.data.isSubmitted || false
    submitTime.value = res.data.submitTime || null
    stats.value.yearlyCount = res.data.yearlyCount || 0
    stats.value.totalAchievements = res.data.totalAchievements || 0
    
    // Fallback: If isSubmitted is true, fetch detailed status from history
    if (isSubmitted.value) {
        // Default to reviewing first
        currentStatus.value = 0
        try {
            // Get latest submission
            const historyRes = await getSubmissionHistory({ page: 1, size: 1 })
            if (historyRes.data && historyRes.data.records && historyRes.data.records.length > 0) {
                // Assuming the first one is the latest
                // Check if it matches the current month/year if needed, 
                // but for now, just take the latest status
                currentStatus.value = historyRes.data.records[0].status
            }
        } catch (e) {
            console.error('Fetch History Error', e)
        }
    } else {
        currentStatus.value = -1
    }

  } catch (error) {
    // console.error('Fetch Stats Error', error)
  }

  try {
    const res = await getPublishedNotices()
    notices.value = (res.data || []).slice(0, 5).map(n => ({
      id: n.id,
      category: n.category,
      title: n.title,
      content: n.content,
      date: n.publishTime ? dayjs(n.publishTime).format('MM-DD') : ''
    }))
  } catch (error) {
    // console.error('Fetch Notices Error', error)
  }

  // 获取当前活动任务
  try {
    const taskRes = await getCurrentTask()
    if (taskRes.data && taskRes.data.task) {
      activeTask.value = taskRes.data.task
      // 只有提交存在且不可重提时才标记已提交（被驳回/退回的可以继续填报）
      taskSubmitted.value = taskRes.data.submissionExists && !taskRes.data.canResubmit
      updateCountdown()
      countdownTimer = setInterval(updateCountdown, 60000) // 每分钟更新
    }
  } catch (error) {
    // console.error('Fetch Task Error', error)
  }
})

onBeforeUnmount(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<style scoped lang="scss">
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 40px;
  font-family: var(--font-body);
  
  * {
    box-sizing: border-box;
  }
}

/* Task Banner */
.task-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, rgba(202, 138, 4, 0.08) 0%, rgba(161, 98, 7, 0.04) 100%);
  border: 1px solid rgba(202, 138, 4, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(12px);
  animation: slideDown 0.4s ease-out;

  .task-info {
    flex: 1;

    .task-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 3px 10px;
      border-radius: 99px;
      font-size: 12px;
      font-weight: 600;
      color: #fff;
      background: linear-gradient(135deg, #CA8A04, #A16207);
      margin-bottom: 8px;
    }

    .task-title {
      margin: 0 0 4px;
      font-size: 18px;
      font-weight: 700;
      font-family: var(--font-heading);
      color: var(--color-primary);
    }

    .task-deadline {
      margin: 0;
      font-size: 13px;
      color: #78716c;

      .countdown {
        color: #CA8A04;
        font-weight: 600;
      }
    }
  }

  .task-action {
    margin-left: 24px;
    flex-shrink: 0;
  }

  .go-fill-btn {
    border-radius: 12px;
    padding: 12px 24px;
    font-weight: 600;
    background: linear-gradient(135deg, #CA8A04, #A16207);
    border-color: #CA8A04;
  }
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-12px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Responsive */
@media (max-width: 768px) {
  .task-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 16px; /* Decrease massive paddings for mobile screens */

    .task-action {
      margin-left: 0;
      width: 100%;

      .go-fill-btn {
        width: 100%;
      }
    }
  }
}
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
  position: relative;
  
  .welcome-box {
    position: relative;
    z-index: 2;
    
    .date-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      background: #F4F4F5; /* Zinc 100 */
      border-radius: 4px;
      font-size: 12px;
      color: var(--color-text-light);
      margin-bottom: 12px;
    }
    
    .welcome-title {
      font-size: 28px;
      font-weight: 600;
      color: var(--color-text);
      margin: 0 0 8px;
      letter-spacing: -0.02em;
      word-break: break-word; /* Ensure long names break properly */
      white-space: normal;
    }
    
    .welcome-subtitle {
      font-size: 15px;
      color: var(--color-text-light);
      margin: 0;
    }
  }
}

/* Layout Grid */
.main-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  
  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.left-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.right-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Cards Common */
.section-card {
  background: #FFFFFF;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  padding: 24px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      margin: 0;
      color: var(--color-text);
    }
  }
}

/* 2. Timeline Status */
.status-timeline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 32px;
  padding: 0 12px;
  
  .step-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    position: relative;
    z-index: 2;
    flex: 1;
    
    .step-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #FFFFFF;
      border: 2px solid #E4E4E7; /* Zinc 200 */
      display: flex;
      align-items: center;
      justify-content: center;
      color: #A1A1AA;
      transition: all 0.3s ease;
      font-size: 18px;
    }
    
    .step-content {
      text-align: center;
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .step-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--color-text-light);
      }
      .step-desc {
        font-size: 12px;
        color: #A1A1AA;
      }
    }
    
    &.active {
      .step-icon {
        border-color: var(--color-primary);
        color: var(--color-primary);
        background: #F4F4F5;
      }
      .step-title {
        color: var(--color-text);
      }
    }
  }
  
  .step-line {
    flex: 1;
    height: 2px;
    background: #E4E4E7;
    margin-top: 20px;
    margin-left: -20px;
    margin-right: -20px;
    z-index: 1;
    
    &.active {
      background: var(--color-primary);
    }
  }
}

/* Deadline bar inside status card */
.task-deadline-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
  padding: 12px 16px;
  background: #FAFAFA; /* Neutral light gray */
  border: 1px solid #F4F4F5; /* Subtle border */
  border-radius: 8px;
  
  .left-info {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .deadline-icon {
      font-size: 16px;
      color: #A1A1AA; /* Zinc-400 */
    }
    
    .deadline-label {
      font-size: 13px;
      font-weight: 500;
      color: #71717A; /* Zinc-500 */
    }
  }

  .countdown-tag {
    display: inline-flex;
    align-items: center;
    height: 24px;
    padding: 0 10px;
    background: #FFFFFF;
    border: 1px solid #E4E4E7;
    border-radius: 4px; /* Slightly squared for teacher UI */
    color: #18181B; /* Zinc-900 */
    font-weight: 600;
    font-size: 12px;
    line-height: 1;
    
    /* Optional: status indicator dot */
    &::before {
      content: '';
      display: inline-block;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background-color: #CA8A04; /* Amber dot for urgency */
      margin-right: 6px;
    }
  }
}

.status-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  
  &.pending {
    background: #F4F4F5;
    color: var(--color-text-light);
  }
  &.done {
    background: #18181B;
    color: #FFF;
  }
}

/* No-task placeholder */
.no-task-card {
  .no-task-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 32px 0 16px;
    text-align: center;

    .no-task-icon {
      font-size: 48px;
      color: #D4D4D8;
      margin-bottom: 12px;
    }

    p {
      margin: 0 0 4px;
      font-size: 15px;
      font-weight: 500;
      color: #71717A;
    }

    span {
      font-size: 13px;
      color: #A1A1AA;
    }
  }
}

/* Disabled action card */
.action-card.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}

.action-btn {
  width: 100%;
  height: 44px;
  border-radius: 4px;
  font-weight: 500;
}

/* 3. Quick Actions */
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  
  .action-card {
    background: #FFFFFF;
    border: 1px solid var(--color-border);
    border-radius: 4px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      border-color: var(--color-primary);
      transform: translateY(-2px);
      
      .icon-wrapper {
        background: var(--color-primary);
        color: #FFF;
      }
    }
    
    .icon-wrapper {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      background: #F4F4F5;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: var(--color-text);
      transition: all 0.2s;
    }
    
    .label {
      font-size: 13px;
      font-weight: 500;
      color: var(--color-text);
    }
  }
}

/* 4. Stats Row */
.stats-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  
  .mini-stat {
    background: #FFF;
    border: 1px solid var(--color-border);
    border-radius: 4px;
    padding: 20px;
    
    .label {
      font-size: 13px;
      color: var(--color-text-light);
      margin-bottom: 8px;
    }
    
    .value {
      font-size: 24px;
      font-weight: 600;
      color: var(--color-text);
      font-family: var(--font-heading);
      
      .unit {
        font-size: 12px;
        color: var(--color-text-light);
        margin-left: 2px;
        font-weight: 400;
      }
    }
  }
}

/* 5. Compact Notices */
.notice-list-compact {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .notice-item-compact {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #F4F4F5;
    cursor: pointer;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover .text {
      color: var(--color-primary);
    }
    
    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #E4E4E7;
      flex-shrink: 0; /* Prevent dot from shrinking */
    }
    
    .text {
      flex: 1;
      font-size: 14px;
      color: var(--color-text);
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
      transition: color 0.2s;
      min-width: 0; /* CRITICAL: allows flex child to shrink and trigger ellipsis */
    }
    
    .date {
      font-size: 13px;
      color: var(--color-text-light);
      flex-shrink: 0; /* Prevent date from shrinking */
      font-variant-numeric: tabular-nums;
    }
  }
  
  .empty-text {
    text-align: center;
    color: #A1A1AA;
    padding: 20px;
    font-size: 13px;
  }
}

.view-all {
  font-size: 13px;
  color: var(--color-text-light);
  text-decoration: none;
  &:hover { color: var(--color-primary); }
}

/* Dialog */
/* Dialog Content Styles */
.notice-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #E4E4E7;

  .el-tag {
    font-weight: 500;
    border: none;
    padding: 0 10px;
  }
  
  .time {
    font-size: 13px;
    color: #71717A;
    font-variant-numeric: tabular-nums;
    display: flex;
    align-items: center;
    
    &::before {
      content: '';
      display: inline-block;
      width: 1px;
      height: 12px;
      background: #E4E4E7;
      margin-right: 12px;
    }
  }
}

.notice-body {
  font-size: 15px;
  line-height: 1.75;
  color: #3F3F46;
  white-space: pre-wrap;
  text-align: justify;
  min-height: 80px;
}

/* Dialog Frame Overrides */
:deep(.minimal-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  
  .el-dialog__header {
    margin: 0;
    padding: 32px 32px 16px; /* 顶部留足空间 */
    background: #FFF;
    border-bottom: none;
    
    .el-dialog__title {
      font-size: 20px;
      font-weight: 600;
      color: #18181B;
      line-height: 1.4;
      letter-spacing: -0.01em;
    }
    
    .el-dialog__headerbtn {
      top: 24px;
      right: 24px;
      font-size: 18px;
    }
  }

  .el-dialog__body {
    padding: 0 32px 40px; /* 内容区域padding */
  }
}

/* Mobile Responsive Adjustments - Wrapped for Specificity */
@media (max-width: 768px) {
  .dashboard-container {
    padding-bottom: 80px; 
    
    .header-section {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
      margin-bottom: 24px;
      
      .welcome-box {
        .welcome-title {
          font-size: 24px; 
        }
        .welcome-subtitle {
          font-size: 14px;
        }
      }
    }

    .main-grid {
      grid-template-columns: 1fr;
      gap: 16px;
    }

    .section-card {
      padding: 16px;
    }

    .task-deadline-bar {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
      padding: 12px;
      
      .countdown-tag {
        align-self: flex-start;
      }
    }

    /* Nested properly to override previous styles */
    .status-timeline {
      flex-direction: column;
      gap: 0;
      padding: 0;
      
      .step-item {
        flex-direction: row;
        align-items: center;
        gap: 16px;
        width: 100%;
        text-align: left;
        padding-bottom: 24px;
        
        &:last-child {
          padding-bottom: 0;
        }

        .step-icon {
          width: 36px;
          height: 36px;
          font-size: 16px;
          flex-shrink: 0;
          z-index: 2;
        }

        .step-content {
          align-items: flex-start;
          text-align: left;
          flex: 1;
          min-width: 0; /* Let flex item shrink below content width */
          
          .step-title {
            font-size: 15px;
            white-space: normal;
            word-break: break-word;
          }
          .step-desc {
            white-space: normal;
            word-break: break-word;
          }
        }
      }

      .step-line {
        display: none; 
      }
      
      .step-item:not(:last-child)::after {
        content: '';
        position: absolute;
        left: 17px;
        top: 36px; 
        bottom: 0;
        width: 2px;
        background: #E4E4E7;
        z-index: 1;
      }
      
      .step-item.active:not(:last-child)::after {
         background: var(--color-primary);
      }
    }

    .quick-actions-grid {
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
      
      .action-card {
        padding: 16px;
        
        .icon-wrapper {
          width: 40px;
          height: 40px;
          font-size: 20px;
        }
        
        .label {
          font-size: 12px;
        }
      }
    }

    .stats-row {
      gap: 12px;
      
      .mini-stat {
        padding: 16px;
        
        .value {
          font-size: 20px;
        }
      }
    }
  }
}
</style>

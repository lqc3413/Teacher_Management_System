<template>
  <el-drawer
    v-model="visible"
    title="消息通知"
    direction="rtl"
    size="420px"
    :before-close="handleClose"
    class="notification-drawer"
  >
    <template #header>
      <div class="drawer-header">
        <span class="drawer-title">消息通知</span>
        <el-button
          v-if="messages.length > 0"
          type="primary"
          link
          size="small"
          @click="handleMarkAllRead"
          :disabled="unreadCount === 0"
        >
          全部已读
        </el-button>
      </div>
    </template>

    <div class="notification-list" v-loading="loading">
      <!-- 空状态 -->
      <div v-if="!loading && messages.length === 0" class="empty-state">
        <el-icon :size="48" color="#d1d5db"><BellFilled /></el-icon>
        <p>暂无消息通知</p>
      </div>

      <!-- 消息列表 -->
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="notification-item"
        :class="{ unread: msg.isRead === 0 }"
        @click="handleClickMessage(msg)"
      >
        <div class="notification-icon" :class="getTypeClass(msg.type)">
          <el-icon :size="18">
            <component :is="getTypeIcon(msg.type)" />
          </el-icon>
        </div>
        <div class="notification-body">
          <div class="notification-title">{{ msg.title }}</div>
          <div class="notification-content">{{ msg.content }}</div>
          <div class="notification-time">{{ formatTime(msg.createdAt) }}</div>
        </div>
        <div v-if="msg.isRead === 0" class="unread-dot"></div>
      </div>

      <!-- 分页加载 -->
      <div v-if="hasMore && messages.length > 0" class="load-more">
        <el-button link type="primary" @click="loadMore" :loading="loadingMore">
          加载更多
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch } from 'vue'
import { BellFilled, SuccessFilled, CircleCloseFilled, DocumentChecked } from '@element-plus/icons-vue'
import { getMessages, markAsRead, markAllAsRead } from '../api/notification'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'refreshCount'])

const visible = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const messages = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(15)
const unreadCount = ref(0)

const hasMore = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    pageNum.value = 1
    messages.value = []
    fetchMessages()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

async function fetchMessages() {
  loading.value = true
  try {
    const res = await getMessages({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (pageNum.value === 1) {
      messages.value = res.data.records
    } else {
      messages.value.push(...res.data.records)
    }
    total.value = res.data.total
    hasMore.value = messages.value.length < total.value

    // 计算未读数
    unreadCount.value = messages.value.filter(m => m.isRead === 0).length
  } catch (error) {
    console.error('获取消息失败', error)
  } finally {
    loading.value = false
  }
}

function loadMore() {
  pageNum.value++
  loadingMore.value = true
  fetchMessages().finally(() => {
    loadingMore.value = false
  })
}

async function handleClickMessage(msg) {
  if (msg.isRead === 0) {
    try {
      await markAsRead(msg.id)
      msg.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      emit('refreshCount')
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    messages.value.forEach(m => { m.isRead = 1 })
    unreadCount.value = 0
    emit('refreshCount')
  } catch (error) {
    console.error('全部标记已读失败', error)
  }
}

function handleClose() {
  visible.value = false
}

function getTypeIcon(type) {
  switch (type) {
    case 1: return DocumentChecked   // 提交通知
    case 2: return SuccessFilled     // 审核通过
    case 3: return CircleCloseFilled // 审核驳回
    default: return BellFilled
  }
}

function getTypeClass(type) {
  switch (type) {
    case 1: return 'type-submit'
    case 2: return 'type-approved'
    case 3: return 'type-rejected'
    default: return 'type-default'
  }
}

function formatTime(time) {
  if (!time) return ''
  const d = dayjs(time)
  const now = dayjs()
  if (now.diff(d, 'day') < 1) {
    return d.fromNow()   // "3 分钟前"
  } else if (now.diff(d, 'day') < 7) {
    return d.fromNow()   // "2 天前"
  } else {
    return d.format('MM-DD HH:mm')
  }
}
</script>

<style scoped lang="scss">
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.drawer-title {
  font-size: 18px;
  font-weight: 700;
  font-family: var(--font-heading);
  color: var(--color-primary);
}

.notification-list {
  padding: 0 4px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #9ca3af;

  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  margin-bottom: 4px;

  &:hover {
    background: rgba(0, 0, 0, 0.02);
  }

  &.unread {
    background: rgba(79, 70, 229, 0.04);

    &:hover {
      background: rgba(79, 70, 229, 0.08);
    }

    .notification-title {
      font-weight: 700;
    }
  }
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;

  &.type-submit {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
  }

  &.type-approved {
    background: linear-gradient(135deg, #10b981, #059669);
  }

  &.type-rejected {
    background: linear-gradient(135deg, #ef4444, #dc2626);
  }

  &.type-default {
    background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  }
}

.notification-body {
  flex: 1;
  min-width: 0;

  .notification-title {
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 4px;
    line-height: 1.4;
  }

  .notification-content {
    font-size: 13px;
    color: #6b7280;
    line-height: 1.5;
    margin-bottom: 6px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .notification-time {
    font-size: 12px;
    color: #9ca3af;
  }
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  flex-shrink: 0;
  margin-top: 6px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.load-more {
  text-align: center;
  padding: 16px 0;
}
</style>

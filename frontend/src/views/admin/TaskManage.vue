<template>
  <div class="task-manage">
    <!-- 头部操作栏 -->
    <div class="page-header">
      <h2 class="page-title">采集任务管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        发布新任务
      </el-button>
    </div>

    <!-- 任务列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="taskList" style="width: 100%" v-loading="loading">
        <el-table-column prop="taskName" label="任务名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="截止时间" width="170">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              :type="statusMap[row.status]?.type"
              effect="light"
              round
            >
              {{ statusMap[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              link
              size="small"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              size="small"
              @click="handleEnd(row)"
            >
              结束
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无任务，点击右上角创建" :image-size="80" />
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 创建任务弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="发布采集任务"
      width="520"
      :close-on-click-modal="false"
      class="task-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="任务名称" prop="taskName">
          <el-input
            v-model="formData.taskName"
            placeholder="如：2024年10-11月信息收集"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="起止时间" prop="timeRange">
          <el-date-picker
            v-model="formData.timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="截止时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="submitting">
          创建任务
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTaskList, createTask, publishTask, endTask, deleteTask } from '../../api/admin'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const taskList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusMap = {
  0: { label: '未发布', type: 'info' },
  1: { label: '进行中', type: 'success' },
  2: { label: '已结束', type: '' }
}

const formData = reactive({
  taskName: '',
  timeRange: null
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择起止时间', trigger: 'change' }]
}

function formatTime(t) {
  if (!t) return ''
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}

function openCreateDialog() {
  formData.taskName = ''
  formData.timeRange = null
  dialogVisible.value = true
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getTaskList({ page: currentPage.value, size: pageSize.value })
    taskList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (err) {
    console.error('获取任务列表失败', err)
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createTask({
      taskName: formData.taskName,
      startTime: formData.timeRange[0],
      endTime: formData.timeRange[1]
    })
    ElMessage.success('任务创建成功')
    dialogVisible.value = false
    fetchList()
  } catch (err) {
    ElMessage.error('创建失败：' + (err.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm(
      `发布后，其他进行中的任务将自动结束。确定要发布"${row.taskName}"吗？`,
      '确认发布',
      { type: 'warning', confirmButtonText: '确定发布', cancelButtonText: '取消' }
    )
    await publishTask(row.id)
    ElMessage.success('任务已发布')
    fetchList()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('发布失败')
  }
}

async function handleEnd(row) {
  try {
    await ElMessageBox.confirm(
      `确定要强制结束"${row.taskName}"吗？教师将无法再提交数据。`,
      '确认结束',
      { type: 'warning', confirmButtonText: '确定结束', cancelButtonText: '取消' }
    )
    await endTask(row.id)
    ElMessage.success('任务已结束')
    fetchList()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${row.taskName}"吗？此操作不可恢复。`,
      '确认删除',
      { type: 'error', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteTask(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.task-manage {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .page-title {
      font-family: var(--font-heading);
      font-size: 24px;
      font-weight: 700;
      color: var(--color-primary);
      margin: 0;
      letter-spacing: -0.5px;
      padding-left: 12px;
      border-left: 4px solid var(--color-cta);
    }
  }

  .table-card {
    border-radius: 16px;
    border: 1px solid rgba(255, 255, 255, 0.5);
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);

    :deep(.el-table) {
      background: transparent;
      --el-table-tr-bg-color: transparent;
      --el-table-header-bg-color: rgba(202, 138, 4, 0.05);

      th.el-table__cell {
        background: rgba(202, 138, 4, 0.05);
        color: var(--color-primary);
        font-weight: 600;
        font-size: 13px;
      }
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      padding: 16px 0 8px;
    }
  }
}
</style>

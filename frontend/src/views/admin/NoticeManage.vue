<template>
  <div class="notice-manage">
    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="noticeSearch" class="search-form">
        <el-form-item label="标题">
          <el-input
            v-model="noticeSearch.title"
            placeholder="搜索标题"
            clearable
            @clear="fetchNotices"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="noticeSearch.status"
            placeholder="状态筛选"
            clearable
            style="width: 120px"
            @change="fetchNotices"
          >
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchNotices" :icon="Search">查询</el-button>
          <el-button @click="resetSearch" :icon="Refresh">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right; margin-right: 0;">
          <el-button type="primary" :icon="Plus" @click="handleAddNotice">新增公告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never" class="table-card">
      <el-table 
        :data="noticeList" 
        stripe 
        v-loading="noticeLoading" 
        style="width: 100%"
        border
      >
        <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
        <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="scope">
            <el-tag :type="getCategoryType(scope.row.category)" size="small">
              {{ scope.row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEditNotice(scope.row)">
              编辑
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              link type="success" size="small"
              @click="handlePublish(scope.row)"
            >
              发布
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteNotice(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="noticeTotal"
          :page-size="noticePageSize"
          v-model:current-page="noticeCurrentPage"
          @current-change="fetchNotices"
        />
      </div>
    </el-card>

    <!-- ========== 通知新增/编辑弹窗 ========== -->
    <el-dialog
      v-model="noticeDialogVisible"
      :title="noticeDialogType === 'add' ? '新增公告' : '编辑公告'"
      width="640px"
      destroy-on-close
    >
      <el-form :model="noticeForm" label-width="80px" :rules="noticeRules" ref="noticeFormRef">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="noticeForm.category" placeholder="选择分类" style="width: 100%">
            <el-option label="教务" value="教务" />
            <el-option label="科研" value="科研" />
            <el-option label="人事" value="人事" />
            <el-option label="系统" value="系统" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="noticeForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="noticeForm.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noticeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleNoticeSubmit" :loading="noticeSaving">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import {
  getNoticeList, addNotice, updateNotice, deleteNotice
} from '../../api/admin'

// ========== 通知公告管理 ==========
const noticeLoading = ref(false)
const noticeSaving = ref(false)
const noticeList = ref([])
const noticeTotal = ref(0)
const noticeCurrentPage = ref(1)
const noticePageSize = ref(10)
const noticeSearch = ref({ title: '', status: null })

const noticeDialogVisible = ref(false)
const noticeDialogType = ref('add')
const noticeFormRef = ref(null)
const noticeForm = ref({
  id: null,
  title: '',
  content: '',
  category: '系统',
  status: 0
})

const noticeRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const fetchNotices = async () => {
  noticeLoading.value = true
  try {
    const params = {
      pageNum: noticeCurrentPage.value,
      pageSize: noticePageSize.value
    }
    if (noticeSearch.value.title) params.title = noticeSearch.value.title
    if (noticeSearch.value.status !== null && noticeSearch.value.status !== '') {
      params.status = noticeSearch.value.status
    }
    const res = await getNoticeList(params)
    noticeList.value = res.data.records || []
    noticeTotal.value = res.data.total || 0
  } catch (e) {
    console.error('获取通知列表失败', e)
  } finally {
    noticeLoading.value = false
  }
}

const resetSearch = () => {
  noticeSearch.value = { title: '', status: null }
  noticeCurrentPage.value = 1
  fetchNotices()
}

const handleAddNotice = () => {
  noticeDialogType.value = 'add'
  noticeForm.value = { id: null, title: '', content: '', category: '系统', status: 0 }
  noticeDialogVisible.value = true
}

const handleEditNotice = (row) => {
  noticeDialogType.value = 'edit'
  noticeForm.value = { ...row }
  noticeDialogVisible.value = true
}

const handleNoticeSubmit = async () => {
  const formEl = noticeFormRef.value
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (!valid) return
    noticeSaving.value = true
    try {
      if (noticeDialogType.value === 'add') {
        await addNotice(noticeForm.value)
        ElMessage.success('新增成功')
      } else {
        await updateNotice(noticeForm.value)
        ElMessage.success('修改成功')
      }
      noticeDialogVisible.value = false
      fetchNotices()
    } catch (e) {
      console.error('操作失败', e)
    } finally {
      noticeSaving.value = false
    }
  })
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布此公告吗？发布后将对教师端可见。', '确认发布', {
      confirmButtonText: '发布',
      cancelButtonText: '取消',
      type: 'info'
    })
    await updateNotice({ id: row.id, status: 1 })
    ElMessage.success('发布成功')
    fetchNotices()
  } catch (e) {
    // cancelled
  }
}

const handleDeleteNotice = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除此公告吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    fetchNotices()
  } catch (e) {
    // cancelled
  }
}

const getCategoryType = (category) => {
  const map = { '教务': '', '科研': 'success', '人事': 'warning', '系统': 'info' }
  return map[category] || 'info'
}

const formatTime = (time) => {
  if (!time) return '—'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const indexMethod = (index) => {
  return (noticeCurrentPage.value - 1) * noticePageSize.value + index + 1
}

// ========== 初始化 ==========
onMounted(() => {
  fetchNotices()
})
</script>

<style scoped lang="scss">
.notice-manage {
  .search-card {
    margin-bottom: 20px;
    border-radius: 12px;
    border: none;
    /* Glass Effect */
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
    
    :deep(.el-card__body) {
      padding: 24px 24px 8px;
    }

    .el-form-item {
      margin-bottom: 16px;
      margin-right: 24px;
      
      :deep(.el-form-item__label) {
        font-weight: 500;
        color: var(--color-text);
      }
    }
  }

  .table-card {
    border-radius: 12px;
    border: none;
    /* Glass Effect */
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
    padding: 8px;

    :deep(.el-table) {
      background-color: transparent;
      --el-table-tr-bg-color: transparent;
      --el-table-header-bg-color: rgba(28, 25, 23, 0.03);
      border-radius: 8px;
      overflow: hidden;

      th.el-table__cell {
        background-color: rgba(28, 25, 23, 0.03);
        font-weight: 600;
        font-family: var(--font-heading);
      }
    }
    
    .pagination {
      margin-top: 24px;
      display: flex;
      justify-content: flex-end;
      padding-right: 16px;
    }
  }
}

/* Button override */
:deep(.el-button--primary) {
  background-color: var(--color-cta);
  border-color: var(--color-cta);
  
  &:hover {
    background-color: var(--color-cta-hover);
    border-color: var(--color-cta-hover);
  }
}
</style>

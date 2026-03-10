<template>
  <div class="material-audit">
    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="审核中" :value="0" />
            <el-option label="已归档" :value="1" />
            <el-option label="已驳回" :value="2" />
            <el-option label="待终审" :value="3" />
            <el-option label="终审退回" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门/学院">
          <el-select v-model="searchForm.deptId" placeholder="全部学院" clearable filterable style="width: 180px">
            <el-option v-for="d in deptList" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          <el-button @click="resetSearch" :icon="Refresh">重置</el-button>
          <el-button @click="handleBatchExport" :icon="Download" :loading="exporting" class="export-btn">批量导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        stripe
        style="width: 100%"
        v-loading="loading"
        border
      >
        <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
        <el-table-column prop="teacherName" label="教师姓名" width="120" />
        <el-table-column prop="employeeNo" label="工号" width="120" />
        <el-table-column prop="deptName" label="所属学院" min-width="150" show-overflow-tooltip />
        <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleView(scope.row)">查看详情</el-button>
            <el-button link type="primary" size="small" @click="handleSingleExport(scope.row)">导出</el-button>
            <el-button
              v-if="scope.row.status === 0 || scope.row.status === 3"
              type="primary"
              size="small"
              plain
              @click="handleAudit(scope.row)"
            >
              {{ scope.row.status === 3 ? '终审' : '审核' }}
            </el-button>
            <el-button
              link
              type="info"
              size="small"
              disabled
              v-else-if="scope.row.status === 1 || scope.row.status === 2"
            >
              已审核
            </el-button>
            <el-tag v-else-if="scope.row.status === 4" type="info" size="small">终审退回</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          @current-change="getList"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="auditDialogVisible"
      :title="currentAuditRow?.status === 3 ? '终审提交' : '审核提交'"
      width="500px"
      append-to-body
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">通过 (归档)</el-radio>
            <el-radio :label="2">驳回</el-radio>
            <el-radio v-if="currentAuditRow?.status === 3" :label="4">退回部门主任</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit" :loading="auditSubmitting">
            确 定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Detail View Dialog -->
    <el-dialog
      v-model="viewDialogVisible"
      title="提交详情"
      width="800px"
      append-to-body
      top="5vh"
    >
      <div class="detail-container" v-if="detailData">
        <el-descriptions title="基本信息" :column="2" border size="small">
          <el-descriptions-item label="教师姓名">{{ detailData.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ detailData.employeeNo }}</el-descriptions-item>
          <el-descriptions-item label="所属学院">{{ detailData.deptName }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ detailData.taskName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailData.status)">{{ getStatusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatTime(detailData.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="detailData.deptAuditStatus" class="audit-remark-box" style="margin-top: 12px;">
          <p><strong>部门初审：</strong>
            <el-tag :type="detailData.deptAuditStatus === 1 ? 'success' : 'danger'" size="small">
              {{ detailData.deptAuditStatus === 1 ? '已通过' : '已驳回' }}
            </el-tag>
          </p>
          <p v-if="detailData.deptAuditRemark"><strong>部门审核意见：</strong>{{ detailData.deptAuditRemark }}</p>
          <p v-if="detailData.deptAuditTime"><strong>部门审核时间：</strong>{{ formatTime(detailData.deptAuditTime) }}</p>
        </div>

        <div v-if="detailData.auditRemark" class="audit-remark-box">
          <p><strong>终审意见：</strong>{{ detailData.auditRemark }}</p>
        </div>

        <el-divider content-position="left">填报内容</el-divider>

        <!-- Dynamic Sections based on data presence -->
        <div class="section-list">
          <!-- Records List -->
          <div v-for="(label, key) in recordLabels" :key="key">
            <el-card v-if="hasRecord(key)" shadow="never" class="mb-2">
              <template #header><span>{{ label }}</span></template>
              
              <!-- List type records -->
              <el-table 
                v-if="Array.isArray(detailData[key])" 
                :data="detailData[key]" 
                border 
                size="small"
              >
                <el-table-column 
                  v-for="col in getColumns(key)" 
                  :key="col.prop" 
                  :prop="col.prop" 
                  :label="col.label" 
                />
              </el-table>

              <!-- Single Object records -->
              <el-descriptions v-else :column="2" border size="small">
                <el-descriptions-item 
                  v-for="col in getColumns(key)" 
                  :key="col.prop" 
                  :label="col.label"
                >
                  {{ detailData[key][col.prop] || '—' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, DocumentChecked, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getSubmissionList, getSubmissionDetail, auditSubmission, getAllDepts, exportSubmissions, exportSingleSubmission } from '../../api/admin'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  deptId: ''
})
const deptList = ref([])
const exporting = ref(false)

const viewDialogVisible = ref(false)
const detailData = ref(null)

const auditDialogVisible = ref(false)
const auditSubmitting = ref(false)
const currentAuditId = ref(null)
const currentAuditRow = ref(null)
const auditForm = reactive({
  status: 1,
  auditRemark: ''
})

// Field mapping configuration
const recordLabels = {
  ipList: '知识产权',
  competition: '指导竞赛',
  training: '培训进修',
  report: '咨询报告',
  book: '自编教材',
  award: '成果获奖',
  paperList: '论文发表',
  verticalProject: '纵向项目',
  horizontalProject: '横向项目',
  innovationProject: '创新创业项目'
}

const columnConfig = {
  ipList: [
    {prop: 'name', label: '名称'}, {prop: 'type', label: '类型'},
    {prop: 'obtainDate', label: '获得时间'}, {prop: 'rank', label: '本人排名'},
    {prop: 'otherParticipants', label: '其他参与人员'}
  ],
  competition: [
    {prop: 'category', label: '竞赛类别'}, {prop: 'name', label: '竞赛名称'},
    {prop: 'organizer', label: '主办单位或发证单位'}, {prop: 'awardDate', label: '获奖时间'},
    {prop: 'certNo', label: '证书编号'}, {prop: 'certName', label: '证书完整名称'},
    {prop: 'awardLevel', label: '奖项级别'}, {prop: 'awardGrade', label: '奖项等级'},
    {prop: 'students', label: '参赛学生'}, {prop: 'advisorTeachers', label: '指导教师'}
  ],
  training: [
    {prop: 'type', label: '培训类型'}, {prop: 'name', label: '培训名称'},
    {prop: 'form', label: '培训形式'}, {prop: 'hours', label: '学时'},
    {prop: 'organizer', label: '主办单位'}, {prop: 'startDate', label: '开始时间'},
    {prop: 'endDate', label: '结束时间'}
  ],
  report: [
    {prop: 'name', label: '报告名称'}, {prop: 'level', label: '采纳单位级别'},
    {prop: 'adoptDate', label: '采纳时间'}, {prop: 'rank', label: '本人排名'},
    {prop: 'others', label: '其他参与人员'}
  ],
  book: [
    {prop: 'name', label: '著作名称'}, {prop: 'publisher', label: '出版社'},
    {prop: 'publishDate', label: '出版时间'}, {prop: 'textbookLevel', label: '教材入选情况'},
    {prop: 'rank', label: '本人排名'}, {prop: 'selectionDate', label: '入选时间'}
  ],
  award: [
    {prop: 'name', label: '获奖成果名称'}, {prop: 'certNo', label: '证书编号'},
    {prop: 'type', label: '成果类型'}, {prop: 'level', label: '获奖级别'},
    {prop: 'grade', label: '获奖等级'}, {prop: 'awardDate', label: '获奖时间'},
    {prop: 'rank', label: '本人排名'}, {prop: 'orgRank', label: '所在单位排名'}
  ],
  paperList: [
    {prop: 'paperType', label: '论文类型'}, {prop: 'paperName', label: '论文名称'},
    {prop: 'authorType', label: '作者类型'}, {prop: 'otherAuthors', label: '其他作者'},
    {prop: 'journalName', label: '发表期刊'}, {prop: 'indexCategory', label: '收录类别'},
    {prop: 'publishDate', label: '发表时间'}
  ],
  verticalProject: [
    {prop: 'researchType', label: '教研或科研类型'}, {prop: 'projectName', label: '项目名称'},
    {prop: 'fundSource', label: '项目基金来源'}, {prop: 'level', label: '项目级别'},
    {prop: 'teamMembers', label: '项目团队成员'}, {prop: 'setupDate', label: '立项时间'},
    {prop: 'setupNo', label: '立项编号或文号'}, {prop: 'updateStatus', label: '项目更新状态'},
    {prop: 'acceptDate', label: '结题验收或鉴定时间'}, {prop: 'funds', label: '项目经费金额（元）'}
  ],
  horizontalProject: [
    {prop: 'researchType', label: '教研或科研类型'}, {prop: 'projectName', label: '项目名称'},
    {prop: 'fundSource', label: '项目基金来源'}, {prop: 'level', label: '项目级别'},
    {prop: 'teamMembers', label: '项目团队成员'}, {prop: 'setupDate', label: '立项时间'},
    {prop: 'setupNo', label: '立项编号或文号'}, {prop: 'updateStatus', label: '项目更新状态'},
    {prop: 'acceptDate', label: '结题验收或鉴定时间'}, {prop: 'funds', label: '项目经费金额（元）'}
  ],
  innovationProject: [
    {prop: 'status', label: '项目状态'}, {prop: 'level', label: '级别'},
    {prop: 'projectName', label: '项目名称'}, {prop: 'startDate', label: '起始时间'},
    {prop: 'completion', label: '是否结题'}, {prop: 'leaderStudent', label: '项目负责学生'},
    {prop: 'otherStudents', label: '其他参与学生'}, {prop: 'funds', label: '项目经费（元）'},
    {prop: 'paperInfo', label: '论文发表情况'}, {prop: 'otherTeachers', label: '其他指导教师'}
  ]
}

const hasRecord = (key) => {
  const val = detailData.value[key]
  if (Array.isArray(val)) return val.length > 0
  return val != null
}

const getColumns = (key) => {
  return columnConfig[key] || []
}

// Lifecycle
onMounted(() => {
  getList()
  loadDepts()
})

const loadDepts = () => {
  getAllDepts().then(res => {
    deptList.value = res.data || []
  })
}

// Methods
const getList = () => {
  loading.value = true
  // Convert empty string status to null/undefined for API
  const params = { ...searchForm }
  if (params.status === '') delete params.status
  
  getSubmissionList(params).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

const handleSearch = () => {
  searchForm.pageNum = 1
  getList()
}

const resetSearch = () => {
  searchForm.status = ''
  searchForm.deptId = ''
  handleSearch()
}

// 文件下载工具函数
const downloadBlob = (data, filename) => {
  const blob = new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(url)
}

const handleBatchExport = () => {
  exporting.value = true
  const params = {}
  if (searchForm.status !== '') params.status = searchForm.status
  if (searchForm.deptId !== '') params.deptId = searchForm.deptId
  exportSubmissions(params).then(res => {
    downloadBlob(res, '教学科研数据导出.xlsx')
    ElMessage.success('导出成功')
  }).catch(() => {
    ElMessage.error('导出失败')
  }).finally(() => {
    exporting.value = false
  })
}

const handleSingleExport = (row) => {
  exportSingleSubmission(row.id).then(res => {
    const name = row.teacherName || '教师'
    downloadBlob(res, `${name}_${row.taskName || ''}_教学科研数据.xlsx`)
    ElMessage.success('导出成功')
  }).catch(() => {
    ElMessage.error('导出失败')
  })
}

const handleView = (row) => {
  getSubmissionDetail(row.id).then(res => {
    detailData.value = res.data
    viewDialogVisible.value = true
  })
}

const handleAudit = (row) => {
  currentAuditId.value = row.id
  currentAuditRow.value = row
  auditForm.status = 1
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

const submitAudit = () => {
  auditSubmitting.value = true
  auditSubmission({
    id: currentAuditId.value,
    status: auditForm.status,
    auditRemark: auditForm.auditRemark
  }).then(() => {
    ElMessage.success('审核完成')
    auditDialogVisible.value = false
    getList()
  }).finally(() => {
    auditSubmitting.value = false
  })
}

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const indexMethod = (index) => {
  return (searchForm.pageNum - 1) * searchForm.pageSize + index + 1
}

const getStatusLabel = (status) => {
  const map = {0: '待审核', 1: '已归档', 2: '已驳回', 3: '待终审', 4: '终审退回'}
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {0: 'warning', 1: 'success', 2: 'danger', 3: '', 4: 'info'}
  return map[status] || 'info'
}
</script>

<style scoped lang="scss">
.material-audit {
  /* search card */
  .search-card {
    margin-bottom: 20px;
    border-radius: 12px;
    border: none;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
    
    :deep(.el-card__body) {
      padding: 24px 24px 8px;
    }
    
    :deep(.el-form-item) {
      margin-bottom: 16px;
    }
  }

  /* table card */
  .table-card {
    min-height: 500px;
    border-radius: 12px;
    border: none;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
    padding: 8px;

    :deep(.el-table) {
      background-color: transparent;
      --el-table-tr-bg-color: transparent;
      --el-table-header-bg-color: rgba(28, 25, 23, 0.03);
      border-radius: 8px;

      th.el-table__cell {
        font-family: var(--font-heading);
        font-weight: 600;
        color: var(--color-primary);
      }
    }
  }

  .pagination {
    margin-top: 24px;
    padding-right: 16px;
    display: flex;
    justify-content: flex-end;
  }

  /* detail view */
  .detail-container {
    max-height: 65vh;
    overflow-y: auto;
    padding: 20px;
    background: #fafaf9; /* Warm stone bg inside dialog */
    border-radius: 8px;

    :deep(.el-descriptions__title) {
      font-family: var(--font-heading);
      font-weight: 600;
    }
  }

  .audit-remark-box {
    background: rgba(254, 242, 242, 0.6);
    color: #ef4444;
    padding: 16px;
    border-radius: 8px;
    margin: 16px 0;
    border: 1px solid rgba(239, 68, 68, 0.1);
  }

  .mb-2 {
    margin-bottom: 20px;
    border-radius: 8px;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
    
    :deep(.el-card__header) {
      padding: 12px 20px;
      background: rgba(28, 25, 23, 0.02);
      border-bottom: 1px solid rgba(0, 0, 0, 0.03);
      font-weight: 600;
      color: var(--color-primary);
    }
  }
}

/* Button overrides for audit actions */
:deep(.el-button--primary:not(.is-link):not(.is-plain)) {
  background-color: var(--color-cta);
  border-color: var(--color-cta);
  color: #fff;
}
:deep(.el-button.export-btn) {
  background-color: var(--color-secondary);
  border-color: var(--color-secondary);
  color: #fff;
}
:deep(.el-button.export-btn:hover) {
  opacity: 0.9;
}
</style>

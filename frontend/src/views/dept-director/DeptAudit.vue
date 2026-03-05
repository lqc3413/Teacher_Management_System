<template>
  <div class="dept-audit-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card pending">
        <div class="stat-number">{{ stats.pendingCount || 0 }}</div>
        <div class="stat-label">待初审</div>
      </div>
      <div class="stat-card approved">
        <div class="stat-number">{{ stats.approvedCount || 0 }}</div>
        <div class="stat-label">已通过待终审</div>
      </div>
      <div class="stat-card rejected">
        <div class="stat-number">{{ stats.rejectedCount || 0 }}</div>
        <div class="stat-label">已驳回</div>
      </div>
      <div class="stat-card returned">
        <div class="stat-number">{{ stats.finalRejectedCount || 0 }}</div>
        <div class="stat-label">终审退回</div>
      </div>
    </div>

    <!-- 筛选和列表 -->
    <div class="audit-panel">
      <div class="panel-header">
        <h3>{{ stats.deptName || '本部门' }} - 教师申报审核</h3>
        <div class="filter-area">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="fetchList" style="width: 140px">
            <el-option label="待初审" :value="0" />
            <el-option label="终审退回" :value="4" />
          </el-select>
          <el-button type="primary" @click="fetchList" :icon="Refresh">刷新</el-button>
        </div>
      </div>

      <el-table :data="submissions" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="realName" label="教师姓名" width="100" />
        <el-table-column prop="employeeNo" label="工号" width="100" />
        <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openDetail(row)">查看</el-button>
            <el-button
              v-if="row.status === 0 || row.status === 4"
              size="small"
              type="success"
              @click="openAuditDialog(row, true)"
            >通过</el-button>
            <el-button
              v-if="row.status === 0 || row.status === 4"
              size="small"
              type="danger"
              @click="openAuditDialog(row, false)"
            >驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-area">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditApproved ? '通过初审' : '驳回申报'"
      width="480px"
    >
      <div class="audit-info">
        <p><strong>教师：</strong>{{ auditTarget?.realName }} ({{ auditTarget?.employeeNo }})</p>
        <p><strong>任务名称：</strong>{{ auditTarget?.taskName }}</p>
      </div>
      <el-form>
        <el-form-item :label="auditApproved ? '审核意见（可选）' : '驳回原因'">
          <el-input
            v-model="auditRemark"
            type="textarea"
            :rows="3"
            :placeholder="auditApproved ? '选填审核意见' : '请填写驳回原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button
          :type="auditApproved ? 'success' : 'danger'"
          @click="submitAudit"
          :loading="auditLoading"
        >
          {{ auditApproved ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="申报详情"
      width="800px"
      top="5vh"
    >
      <div v-if="detailData" class="detail-content">
        <el-descriptions title="基本信息" :column="2" border size="small">
          <el-descriptions-item label="教师姓名">{{ detailData.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ detailData.employeeNo }}</el-descriptions-item>
          <el-descriptions-item label="所属学院">{{ detailData.deptName }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ detailData.taskName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailData.status)">{{ getStatusText(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(detailData.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="detailData.deptAuditStatus" class="audit-remark-box" style="margin-top: 12px;">
          <p><strong>部门初审：</strong>
            <el-tag :type="detailData.deptAuditStatus === 1 ? 'success' : 'danger'" size="small">
              {{ detailData.deptAuditStatus === 1 ? '已通过' : '已驳回' }}
            </el-tag>
          </p>
          <p v-if="detailData.deptAuditRemark"><strong>部门审核意见：</strong>{{ detailData.deptAuditRemark }}</p>
        </div>

        <div v-if="detailData.auditRemark" class="audit-remark-box">
          <p><strong>终审意见：</strong>{{ detailData.auditRemark }}</p>
        </div>

        <el-divider content-position="left">填报内容</el-divider>

        <div class="section-list">
          <div v-for="(label, key) in recordLabels" :key="key">
            <el-card v-if="hasRecord(key)" shadow="never" style="margin-bottom: 12px;">
              <template #header><span>{{ label }}</span></template>
              <el-table v-if="Array.isArray(detailData[key])" :data="detailData[key]" border size="small">
                <el-table-column v-for="col in getColumns(key)" :key="col.prop" :prop="col.prop" :label="col.label" />
              </el-table>
              <el-descriptions v-else :column="2" border size="small">
                <el-descriptions-item v-for="col in getColumns(key)" :key="col.prop" :label="col.label">
                  {{ detailData[key][col.prop] }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getDeptPendingSubmissions, deptAuditSubmission, getDeptSubmissionDetail, getDeptDirectorStats } from '../../api/dept-director'

const loading = ref(false)
const submissions = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref(null)
const stats = ref({})

// 审核对话框
const auditDialogVisible = ref(false)
const auditTarget = ref(null)
const auditApproved = ref(true)
const auditRemark = ref('')
const auditLoading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const detailData = ref(null)

function getStatusText(status) {
  const map = { 0: '待初审', 1: '已归档', 2: '已驳回', 3: '待终审', 4: '终审退回' }
  return map[status] || '未知'
}

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: '' }
  return map[status] || ''
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

async function fetchStats() {
  try {
    const res = await getDeptDirectorStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (e) {
    console.error('获取统计失败', e)
  }
}

async function fetchList() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (statusFilter.value !== null && statusFilter.value !== '') {
      params.status = statusFilter.value
    }
    const res = await getDeptPendingSubmissions(params)
    if (res.code === 200) {
      submissions.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

function openAuditDialog(row, approved) {
  auditTarget.value = row
  auditApproved.value = approved
  auditRemark.value = ''
  auditDialogVisible.value = true
}

async function submitAudit() {
  if (!auditApproved.value && !auditRemark.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  auditLoading.value = true
  try {
    const res = await deptAuditSubmission(auditTarget.value.id, {
      approved: auditApproved.value,
      remark: auditRemark.value
    })
    if (res.code === 200) {
      ElMessage.success(auditApproved.value ? '已通过初审' : '已驳回')
      auditDialogVisible.value = false
      fetchList()
      fetchStats()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    auditLoading.value = false
  }
}

async function openDetail(row) {
  try {
    const res = await getDeptSubmissionDetail(row.id)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

// 材料类型标签
const recordLabels = {
  ipList: '知识产权', competition: '指导竞赛', training: '培训进修',
  report: '咨询报告', book: '自编教材', award: '成果获奖',
  paper: '论文发表', verticalProject: '纵向项目',
  horizontalProject: '横向项目', innovationProject: '创新创业项目'
}

const columnConfig = {
  ipList: [
    {prop: 'name', label: '名称'}, {prop: 'type', label: '类型'},
    {prop: 'obtainDate', label: '获得时间'}, {prop: 'rank', label: '本人排名'},
    {prop: 'otherParticipants', label: '其他参与人员'}
  ],
  competition: [
    {prop: 'category', label: '竞赛类别'}, {prop: 'name', label: '竞赛名称'},
    {prop: 'organizer', label: '主办单位'}, {prop: 'awardDate', label: '获奖时间'},
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
    {prop: 'adoptDate', label: '采纳时间'}, {prop: 'rank', label: '本人排名'}
  ],
  book: [
    {prop: 'name', label: '著作名称'}, {prop: 'publisher', label: '出版社'},
    {prop: 'publishDate', label: '出版时间'}, {prop: 'rank', label: '本人排名'}
  ],
  award: [
    {prop: 'name', label: '获奖成果名称'}, {prop: 'type', label: '成果类型'},
    {prop: 'level', label: '获奖级别'}, {prop: 'grade', label: '获奖等级'},
    {prop: 'awardDate', label: '获奖时间'}, {prop: 'rank', label: '本人排名'}
  ],
  paper: [
    {prop: 'paperType', label: '论文类型'}, {prop: 'paperName', label: '论文名称'},
    {prop: 'authorType', label: '作者类型'}, {prop: 'journalName', label: '发表期刊'},
    {prop: 'publishDate', label: '发表时间'}
  ],
  verticalProject: [
    {prop: 'projectName', label: '项目名称'}, {prop: 'level', label: '项目级别'},
    {prop: 'fundSource', label: '基金来源'}, {prop: 'setupDate', label: '立项时间'},
    {prop: 'updateStatus', label: '更新状态'}
  ],
  horizontalProject: [
    {prop: 'projectName', label: '项目名称'}, {prop: 'level', label: '项目级别'},
    {prop: 'fundSource', label: '基金来源'}, {prop: 'setupDate', label: '立项时间'},
    {prop: 'updateStatus', label: '更新状态'}
  ],
  innovationProject: [
    {prop: 'projectName', label: '项目名称'}, {prop: 'level', label: '级别'},
    {prop: 'leaderStudent', label: '负责学生'}, {prop: 'startDate', label: '起始时间'},
    {prop: 'completion', label: '是否结题'}
  ]
}

const hasRecord = (key) => {
  if (!detailData.value) return false
  const val = detailData.value[key]
  if (Array.isArray(val)) return val.length > 0
  return val != null
}

const getColumns = (key) => columnConfig[key] || []

onMounted(() => {
  fetchStats()
  fetchList()
})
</script>

<style scoped>
.dept-audit-page {
  padding: 20px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--teacher-card-bg, #fff);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card .stat-number {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-card .stat-label {
  font-size: 13px;
  color: #888;
}

.stat-card.pending .stat-number { color: #e6a23c; }
.stat-card.approved .stat-number { color: #409eff; }
.stat-card.rejected .stat-number { color: #f56c6c; }
.stat-card.returned .stat-number { color: #909399; }

.audit-panel {
  background: var(--teacher-card-bg, #fff);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.filter-area {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.audit-info {
  margin-bottom: 16px;
}

.audit-info p {
  margin: 4px 0;
  color: #666;
}

.detail-content {
  padding: 8px 0;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .stat-card {
    padding: 14px;
  }

  .stat-card .stat-number {
    font-size: 24px;
  }

  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .filter-area {
    width: 100%;
  }
}
</style>

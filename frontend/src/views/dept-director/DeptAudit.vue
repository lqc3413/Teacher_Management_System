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
        <h3 class="page-title">{{ stats.deptName || '本部门' }} - 教师申报审核</h3>
        <div class="filter-area">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="fetchList" style="width: 140px">
            <el-option label="待初审" :value="0" />
            <el-option label="终审退回" :value="4" />
          </el-select>
          <el-button type="primary" @click="fetchList" :icon="Refresh">刷新</el-button>
        </div>
      </div>

      <!-- PC端表格视图 -->
      <div v-if="!isMobile" class="table-wrapper">
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
              <div class="action-buttons">
                <el-button link class="btn-audit-view" size="small" @click="openDetail(row)">查看</el-button>
                <el-button
                  v-if="row.status === 0 || row.status === 4"
                  link
                  class="btn-audit-pass"
                  size="small"
                  @click="openAuditDialog(row, true)"
                >通过</el-button>
                <el-button
                  v-if="row.status === 0 || row.status === 4"
                  link
                  class="btn-audit-reject"
                  size="small"
                  @click="openAuditDialog(row, false)"
                >驳回</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

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

      <!-- 移动端卡片视图 -->
      <div v-else class="mobile-card-list" v-loading="loading">
        <div v-if="submissions.length === 0" class="empty-text">暂无数据</div>
        <el-card 
          v-for="item in submissions" 
          :key="item.id" 
          class="mobile-item-card"
          shadow="sm"
        >
          <div class="card-header">
            <div class="header-left">
              <span class="teacher-name">{{ item.realName }}</span>
              <span class="task-name-text">{{ item.taskName || '无任务' }}</span>
            </div>
            <el-tag :type="getStatusType(item.status)" size="small">
              {{ getStatusText(item.status) }}
            </el-tag>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="label">提交时间：</span>
              <span class="value">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
          <div class="card-footer">
            <el-button link class="btn-audit-view" size="small" @click="openDetail(item)">查看</el-button>
            <el-button v-if="item.status === 0 || item.status === 4" link class="btn-audit-pass" size="small" @click="openAuditDialog(item, true)">通过</el-button>
            <el-button v-if="item.status === 0 || item.status === 4" link class="btn-audit-reject" size="small" @click="openAuditDialog(item, false)">驳回</el-button>
          </div>
        </el-card>
        
        <div class="pagination-area mobile-pagination">
          <el-pagination
            small
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            v-model:current-page="currentPage"
            @current-change="fetchList"
          />
        </div>
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
                  {{ detailData[key][col.prop] || '—' }}
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
import { useIsMobile } from '../../hooks/useIsMobile'

const { isMobile } = useIsMobile()
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
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: 'info' }
  return map[status] || 'info'
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
  paperList: '论文发表', verticalProject: '纵向项目',
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

<style scoped lang="scss">
.dept-audit-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px;
  padding-bottom: 60px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--teacher-card-bg, #fff);
  border-radius: 4px; /* Sharper */
  border: 1px solid var(--color-border);
  padding: 20px;
  text-align: center;
  box-shadow: none; /* Flat */
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--teacher-shadow-hover, 0 12px 24px rgba(0,0,0,0.06));
}

.stat-card .stat-number {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-card .stat-label {
  font-size: 13px;
  color: var(--color-text-light, #71717A);
}

.stat-card.pending .stat-number { color: #92400E; } /* 沉稳琥珀/褐 */
.stat-card.approved .stat-number { color: #065F46; } /* 雅青（绿） */
.stat-card.rejected .stat-number { color: #9F1239; } /* 朱砂红 */
.stat-card.returned .stat-number { color: var(--color-text-light, #71717A); } /* 灰 */

.audit-panel {
  background: var(--teacher-card-bg, #fff);
  border-radius: 4px;
  border: 1px solid var(--color-border);
  padding: 24px;
  box-shadow: none;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .page-title {
    font-family: var(--font-heading);
    font-size: 20px;
    font-weight: 700;
    color: var(--color-text);
    margin: 0;
    letter-spacing: -0.02em;
    display: flex;
    align-items: center;
    gap: 12px;
    
    &::before {
      content: '';
      width: 4px;
      height: 20px;
      border-radius: 2px;
      background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
    }
  }
}

.filter-area {
  display: flex;
  gap: 12px;
  align-items: center;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.audit-info {
  margin-bottom: 16px;
}

.audit-info p {
  margin: 4px 0;
  color: var(--color-text-light, #71717A);
}

.detail-content {
  padding: 8px 0;
}

:deep(.el-table) {
  border-radius: 0;
  --el-table-header-bg-color: #F8FAFC;
  --el-table-row-hover-bg-color: #F1F5F9;
  
  th.el-table__cell {
    font-weight: 600;
    font-size: 13px;
    color: var(--color-text-light);
    text-transform: uppercase;
    letter-spacing: 0.02em;
  }
}

.action-buttons {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
  white-space: nowrap;
}

.btn-audit-view { color: var(--color-primary) !important; font-weight: 500; }
.btn-audit-view:hover { color: var(--color-text-light) !important; }
.btn-audit-pass { color: #065F46 !important; font-weight: 500; }
.btn-audit-pass:hover { color: #047857 !important; }
.btn-audit-reject { color: #9F1239 !important; font-weight: 500; }
.btn-audit-reject:hover { color: #BE123C !important; }

/* 移动端卡片视图样式 */
.mobile-card-list {
  .mobile-item-card {
    margin-bottom: 12px;
    border-radius: 4px;
    border: 1px solid var(--color-border);
    box-shadow: none;
    transition: all 0.2s ease;
    
    &:hover {
      box-shadow: var(--shadow-md);
    }
    
    :deep(.el-card__body) {
      padding: 16px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 1px solid var(--color-border);
      
      .teacher-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--color-text);
      }

      .header-left {
        display: flex;
        flex-direction: column;
        gap: 4px;
      }

      .task-name-text {
        font-size: 13px;
        color: var(--color-text-light);
      }
    }
    
    .card-body {
      margin-bottom: 16px;
      
      .info-row {
        display: flex;
        font-size: 14px;
        color: var(--color-text-light);
        
        .value {
          color: var(--color-text);
          margin-left: 8px;
          font-weight: 500;
        }
      }
    }
    
    .card-footer {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;
      border-top: 1px solid var(--color-border);
      padding-top: 12px;
      
      .el-button {
        margin-left: 0;
        margin-bottom: 4px;
      }
    }
  }
  
  .mobile-pagination {
    justify-content: center;
    margin-bottom: 20px;
    margin-top: 20px;
  }
  
  .empty-text {
    text-align: center;
    color: var(--color-text-light);
    margin-top: 40px;
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-card .stat-number {
    font-size: 24px;
  }

  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .panel-header .page-title {
    font-size: 18px;
    align-self: flex-start;
  }

  .filter-area {
    width: 100%;
    
    .el-select, .el-button {
      flex: 1;
    }
  }

  .audit-panel {
    padding: 16px;
  }
}
</style>

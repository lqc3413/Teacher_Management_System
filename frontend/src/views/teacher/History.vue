<template>
  <div class="history-container">
    <div class="header-actions">
      <h2 class="page-title">历史填报记录</h2>
      <div class="filter-box">
        <el-date-picker
          v-model="searchYear"
          type="year"
          placeholder="选择年份"
          value-format="YYYY"
          style="width: 150px"
        />
        <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
      </div>
    </div>

    <!-- PC端表格视图 -->
    <el-card v-if="!isMobile" shadow="never" class="table-card">
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="submitMonth" label="填报月份" width="180" sortable />
        <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提交时间" width="220">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="150">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="viewDetail(scope.row)">
              查看详情
            </el-button>
            <el-button link type="primary" size="small" @click="exportPdf(scope.row)">
              导出PDF
            </el-button>
            <el-button v-if="scope.row.status === 2" link type="danger" size="small" @click="handleResubmit(scope.row)">
              重新修改
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 移动端卡片视图 -->
    <div v-else class="mobile-card-list" v-loading="loading">
      <div v-if="tableData.length === 0" class="empty-text">暂无数据</div>
      <el-card 
        v-for="item in tableData" 
        :key="item.id" 
        class="mobile-item-card"
        shadow="sm"
      >
        <div class="card-header">
          <div class="header-left">
            <span class="month-text">{{ item.submitMonth }}</span>
            <div class="task-name-text">{{ item.taskName || '无任务名称' }}</div>
          </div>
          <el-tag :type="getStatusType(item.status)" size="small">
            {{ getStatusLabel(item.status) }}
          </el-tag>
        </div>
        <div class="card-body">
          <div class="info-row">
            <span class="label">提交时间：</span>
            <span class="value">{{ formatTime(item.createTime) }}</span>
          </div>
        </div>
        <div class="card-footer">
          <el-button link type="primary" size="small" @click="viewDetail(item)">
            查看详情
          </el-button>
          <el-button link type="primary" size="small" @click="exportPdf(item)">
            导出PDF
          </el-button>
          <el-button v-if="item.status === 2" link type="danger" size="small" @click="handleResubmit(item)">
            重新修改
          </el-button>
        </div>
      </el-card>
      
      <div class="pagination-container mobile-pagination">
        <el-pagination
          small
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog 
      v-model="detailVisible" 
      :title="'提交详情 — ' + (detailData?.submitMonth || '')" 
      width="700px"
      :fullscreen="isMobile"
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <!-- 知识产权 -->
          <el-descriptions v-if="detailData.ipList && detailData.ipList.length" title="知识产权" :column="1" border size="small" class="detail-section">
            <el-descriptions-item v-for="(ip, idx) in detailData.ipList" :key="idx" :label="'#' + (idx+1)">
              {{ ip.name || '—' }}（{{ ip.type || '—' }}）— 排名第{{ ip.rank }}，获取日期 {{ ip.obtainDate || '—' }}，其他参与人员：{{ ip.otherParticipants || '—' }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 竞赛 -->
          <el-descriptions v-if="detailData.competition" title="指导学生竞赛" :column="isMobile ? 1 : 2" border size="small" class="detail-section">
            <el-descriptions-item label="竞赛类别">{{ detailData.competition.category || '—' }}</el-descriptions-item>
            <el-descriptions-item label="竞赛名称">{{ detailData.competition.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="主办单位或发证单位">{{ detailData.competition.organizer || '—' }}</el-descriptions-item>
            <el-descriptions-item label="获奖时间">{{ detailData.competition.awardDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="证书编号">{{ detailData.competition.certNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="证书完整名称">{{ detailData.competition.certName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="奖项级别">{{ detailData.competition.awardLevel || '—' }}</el-descriptions-item>
            <el-descriptions-item label="奖项等级">{{ detailData.competition.awardGrade || '—' }}</el-descriptions-item>
            <el-descriptions-item label="参赛学生">{{ detailData.competition.students || '—' }}</el-descriptions-item>
            <el-descriptions-item label="指导教师">{{ detailData.competition.advisorTeachers || '—' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 培训 -->
          <el-descriptions v-if="detailData.training" title="进修培训" :column="isMobile ? 1 : 2" border size="small" class="detail-section">
            <el-descriptions-item label="培训类型">{{ detailData.training.type || '—' }}</el-descriptions-item>
            <el-descriptions-item label="培训名称">{{ detailData.training.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="培训形式">{{ detailData.training.form || '—' }}</el-descriptions-item>
            <el-descriptions-item label="学时">{{ detailData.training.hours || '—' }}</el-descriptions-item>
            <el-descriptions-item label="主办单位">{{ detailData.training.organizer || '—' }}</el-descriptions-item>
            <el-descriptions-item label="起止时间">{{ detailData.training.startDate || '—' }} 至 {{ detailData.training.endDate || '—' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 论文 -->
          <el-descriptions v-if="detailData.paper" title="发表论文" :column="isMobile ? 1 : 2" border size="small" class="detail-section">
            <el-descriptions-item label="论文类型">{{ detailData.paper.paperType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="论文名称">{{ detailData.paper.paperName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="作者类型">{{ detailData.paper.authorType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="其他作者">{{ detailData.paper.otherAuthors || '—' }}</el-descriptions-item>
            <el-descriptions-item label="发表期刊">{{ detailData.paper.journalName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="收录类别">{{ detailData.paper.indexCategory || '—' }}</el-descriptions-item>
            <el-descriptions-item label="发表日期">{{ detailData.paper.publishDate || '—' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 奖项 -->
          <el-descriptions v-if="detailData.award" title="教学科研成果奖" :column="isMobile ? 1 : 2" border size="small" class="detail-section">
            <el-descriptions-item label="获奖名称">{{ detailData.award.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="奖项类型">{{ detailData.award.type || '—' }}</el-descriptions-item>
            <el-descriptions-item label="级别">{{ detailData.award.level || '—' }}</el-descriptions-item>
            <el-descriptions-item label="等级">{{ detailData.award.grade || '—' }}</el-descriptions-item>
            <el-descriptions-item label="本人排名">第{{ detailData.award.rank }}名</el-descriptions-item>
            <el-descriptions-item label="所在单位排名">{{ detailData.award.orgRank ? '第' + detailData.award.orgRank + '名' : '—' }}</el-descriptions-item>
            <el-descriptions-item label="获奖日期">{{ detailData.award.awardDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="证书编号">{{ detailData.award.certNo || '—' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 著作/教材 -->
          <el-descriptions v-if="detailData.book" title="出版著作/教材" :column="isMobile ? 1 : 2" border size="small" class="detail-section">
            <el-descriptions-item label="名称">{{ detailData.book.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="出版社">{{ detailData.book.publisher || '—' }}</el-descriptions-item>
            <el-descriptions-item label="出版日期">{{ detailData.book.publishDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="教材入选情况">{{ detailData.book.textbookLevel || '—' }}</el-descriptions-item>
            <el-descriptions-item label="排名">第{{ detailData.book.rank }}名</el-descriptions-item>
            <el-descriptions-item label="入选时间">{{ detailData.book.selectionDate || '—' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 报告 -->
          <el-descriptions v-if="detailData.report" title="咨询调研报告" :column="isMobile ? 1 : 2" border size="small" class="detail-section">
            <el-descriptions-item label="报告名称">{{ detailData.report.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="报告采纳单位级别">{{ detailData.report.level || '—' }}</el-descriptions-item>
            <el-descriptions-item label="采纳时间">{{ detailData.report.adoptDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="本人排名">第{{ detailData.report.rank }}名</el-descriptions-item>
            <el-descriptions-item label="其他参与人员">{{ detailData.report.others || '—' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 纵向项目 -->
          <el-descriptions v-if="detailData.verticalProject" title="纵向项目" :column="isMobile ? 1 : 2" border size="small" class="detail-section">
            <el-descriptions-item label="教研或科研类型">{{ detailData.verticalProject.researchType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目名称">{{ detailData.verticalProject.projectName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目基金来源">{{ detailData.verticalProject.fundSource || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目级别">{{ detailData.verticalProject.level || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目团队成员">{{ detailData.verticalProject.teamMembers || '—' }}</el-descriptions-item>
            <el-descriptions-item label="立项时间">{{ detailData.verticalProject.setupDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="立项编号或文号">{{ detailData.verticalProject.setupNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目更新状态">{{ detailData.verticalProject.updateStatus || '—' }}</el-descriptions-item>
            <el-descriptions-item label="结题验收或鉴定时间">{{ detailData.verticalProject.acceptDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目经费金额（元）">{{ detailData.verticalProject.funds || '—' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 横向项目 -->
          <el-descriptions v-if="detailData.horizontalProject" title="横向项目" :column="isMobile ? 1 : 2" border size="small" class="detail-section">
            <el-descriptions-item label="教研或科研类型">{{ detailData.horizontalProject.researchType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目名称">{{ detailData.horizontalProject.projectName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目基金来源">{{ detailData.horizontalProject.fundSource || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目级别">{{ detailData.horizontalProject.level || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目团队成员">{{ detailData.horizontalProject.teamMembers || '—' }}</el-descriptions-item>
            <el-descriptions-item label="立项时间">{{ detailData.horizontalProject.setupDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="立项编号或文号">{{ detailData.horizontalProject.setupNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目更新状态">{{ detailData.horizontalProject.updateStatus || '—' }}</el-descriptions-item>
            <el-descriptions-item label="结题验收或鉴定时间">{{ detailData.horizontalProject.acceptDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目经费金额（元）">{{ detailData.horizontalProject.funds || '—' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 创新创业项目 -->
          <el-descriptions v-if="detailData.innovationProject" title="指导学生参加创新创业项目" :column="isMobile ? 1 : 2" border size="small" class="detail-section">
            <el-descriptions-item label="项目状态">{{ detailData.innovationProject.status || '—' }}</el-descriptions-item>
            <el-descriptions-item label="级别">{{ detailData.innovationProject.level || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目名称">{{ detailData.innovationProject.projectName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="起始时间">{{ detailData.innovationProject.startDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="是否结题">{{ detailData.innovationProject.completion || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目负责学生">{{ detailData.innovationProject.leaderStudent || '—' }}</el-descriptions-item>
            <el-descriptions-item label="其他参与学生">{{ detailData.innovationProject.otherStudents || '—' }}</el-descriptions-item>
            <el-descriptions-item label="项目经费（元）">{{ detailData.innovationProject.funds || '—' }}</el-descriptions-item>
            <el-descriptions-item label="论文发表情况">{{ detailData.innovationProject.paperInfo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="其他指导教师">{{ detailData.innovationProject.otherTeachers || '—' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 无任何子表数据 -->
          <el-empty v-if="(!detailData.ipList || !detailData.ipList.length) && !detailData.competition && !detailData.training && !detailData.paper && !detailData.award && !detailData.book && !detailData.report && !detailData.verticalProject && !detailData.horizontalProject && !detailData.innovationProject" description="该次提交暂无详情数据" />
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getSubmissionHistory, getSubmissionDetail } from '../../api/teacher'
import dayjs from 'dayjs'
import { useIsMobile } from '../../hooks/useIsMobile'

const router = useRouter()

const { isMobile } = useIsMobile()

const searchYear = ref('')
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const tableData = ref([])

const statusMap = { 0: '审核中', 1: '已归档', 2: '被退回' }
const getStatusLabel = (status) => statusMap[status] || '未知'
const getStatusType = (status) => {
  switch (status) {
    case 1: return 'success'
    case 0: return 'warning'
    case 2: return 'danger'
    default: return 'info'
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchYear.value) {
      params.year = searchYear.value
    }
    const res = await getSubmissionHistory(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取历史记录失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchData()
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

const viewDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getSubmissionDetail(row.id)
    detailData.value = res.data
  } catch (error) {
    console.error('获取详情失败', error)
  } finally {
    detailLoading.value = false
  }
}

const handleResubmit = (row) => {
  router.push({ path: '/teacher/info-fill', query: { resubmitId: row.id } })
}

const exportPdf = (row) => {
  ElMessage.info('导出PDF功能开发中，敬请期待')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.history-container {
  max-width: 1280px;
  margin: 0 auto;
  padding-bottom: 60px;

  .header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28px;

    .page-title {
      font-family: var(--font-heading);
      font-size: 24px;
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
        height: 24px;
        border-radius: 2px;
        background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
      }
    }

    .filter-box {
      display: flex;
      gap: 12px;
    }
  }

  .table-card {
    border-radius: 4px; /* Sharper */
    border: 1px solid var(--color-border);
    box-shadow: none; /* Flat */
    overflow: hidden;
    
    :deep(.el-card__body) {
        padding: 0;
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
  }

  .pagination-container {
    margin-top: 20px;
    padding: 16px;
    display: flex;
    justify-content: flex-end;
  }
  
  // Mobile styles
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
        
        .month-text {
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
        gap: 8px;
        border-top: 1px solid var(--color-border);
        padding-top: 12px;
        
        .el-button {
          margin-left: 0;
        }
      }
    }
    
    .mobile-pagination {
      justify-content: center;
      margin-bottom: 20px;
    }
    
    .empty-text {
      text-align: center;
      color: var(--color-text-light);
      margin-top: 40px;
    }
  }

  @media (max-width: 768px) {
    .header-actions {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
      
      .filter-box {
        width: 100%;
        
        .el-date-editor {
          flex: 1;
        }
      }
    }
  }
}

// Detail dialog styles
:deep(.el-dialog) {
  border-radius: 4px;
  overflow: hidden;
  
  .el-dialog__header {
    padding: 24px;
    background: #F8FAFC;
    border-bottom: 1px solid var(--color-border);
    margin: 0;
    
    .el-dialog__title {
      font-weight: 600;
      color: var(--color-text);
    }
  }
  
  .el-dialog__body {
    padding: 24px;
  }
}

.detail-section {
  margin-bottom: 24px;
  
  :deep(.el-descriptions__header) {
      margin-bottom: 12px;
  }
  
  :deep(.el-descriptions__title) {
      font-weight: 600;
      font-size: 15px;
      color: var(--color-primary);
  }
  
  :deep(.el-descriptions__label) {
      color: var(--color-text-light);
      font-weight: 500;
  }
}
</style>

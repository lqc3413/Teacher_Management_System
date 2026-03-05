<template>
  <div class="achievements-container">
    <div class="header-actions">
      <h2 class="page-title">我的成果档案</h2>
      <el-button :icon="Download" :loading="exportLoading" @click="handleExportAll">导出全部</el-button>
    </div>

    <!-- 状态筛选 -->
    <div class="status-filter">
      <el-radio-group v-model="statusFilter" size="default">
        <el-radio-button value="all">
          全部 <span class="filter-count">{{ totalCount('all') }}</span>
        </el-radio-button>
        <el-radio-button value="approved">
          <span class="status-dot approved"></span>已通过 <span class="filter-count">{{ totalCount('approved') }}</span>
        </el-radio-button>
        <el-radio-button value="pending">
          <span class="status-dot pending"></span>审核中 <span class="filter-count">{{ totalCount('pending') }}</span>
        </el-radio-button>
        <el-radio-button value="rejected">
          <span class="status-dot rejected"></span>已退回 <span class="filter-count">{{ totalCount('rejected') }}</span>
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-overview">
      <div class="stat-item" v-for="item in statCards" :key="item.label">
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-label">{{ item.label }}</div>
      </div>
    </div>

    <!-- 标签页 -->
    <el-card shadow="never" class="tabs-card" v-loading="loading">
      <el-tabs v-model="activeTab" class="achievement-tabs" stretch>
        <!-- 学术论文 -->
        <el-tab-pane name="papers">
          <template #label>
            学术论文 <span v-if="currentData.papers.length" class="count-tag">{{ currentData.papers.length }}</span>
          </template>
          <el-table :data="currentData.papers" stripe style="width: 100%" v-if="currentData.papers.length">
            <el-table-column prop="paperName" label="论文名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="paperType" label="论文类型" width="120" />
            <el-table-column prop="journalName" label="发表期刊" width="160" show-overflow-tooltip />
            <el-table-column prop="indexCategory" label="收录类别" width="120" />
            <el-table-column prop="authorType" label="作者类型" width="120" />
            <el-table-column prop="publishDate" label="发表日期" width="120" sortable />
          </el-table>
          <el-empty v-else description="暂无论文数据" />
        </el-tab-pane>

        <!-- 获奖荣誉 -->
        <el-tab-pane name="awards">
          <template #label>
            获奖荣誉 <span v-if="currentData.awards.length" class="count-tag">{{ currentData.awards.length }}</span>
          </template>
          <el-table :data="currentData.awards" stripe style="width: 100%" v-if="currentData.awards.length">
            <el-table-column prop="name" label="奖项名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="type" label="奖项类型" width="120" />
            <el-table-column prop="level" label="级别" width="100" />
            <el-table-column prop="grade" label="等级" width="100" />
            <el-table-column prop="rank" label="排名" width="80">
              <template #default="{ row }">第{{ row.rank }}名</template>
            </el-table-column>
            <el-table-column prop="awardDate" label="获奖日期" width="120" sortable />
          </el-table>
          <el-empty v-else description="暂无获奖数据" />
        </el-tab-pane>

        <!-- 知识产权 -->
        <el-tab-pane name="ips">
          <template #label>
            知识产权 <span v-if="currentData.ips.length" class="count-tag">{{ currentData.ips.length }}</span>
          </template>
          <el-table :data="currentData.ips" stripe style="width: 100%" v-if="currentData.ips.length">
            <el-table-column prop="name" label="名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="obtainDate" label="获取日期" width="120" sortable />
            <el-table-column prop="rank" label="排名" width="80">
              <template #default="{ row }">第{{ row.rank }}名</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无知识产权数据" />
        </el-tab-pane>

        <!-- 学科竞赛 -->
        <el-tab-pane name="competitions">
          <template #label>
            学科竞赛 <span v-if="currentData.competitions.length" class="count-tag">{{ currentData.competitions.length }}</span>
          </template>
          <el-table :data="currentData.competitions" stripe style="width: 100%" v-if="currentData.competitions.length">
            <el-table-column prop="name" label="竞赛名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="category" label="竞赛类别" width="120" />
            <el-table-column prop="awardLevel" label="奖项级别" width="100" />
            <el-table-column prop="awardGrade" label="奖项等级" width="100" />
            <el-table-column prop="organizer" label="主办单位" width="160" show-overflow-tooltip />
            <el-table-column prop="awardDate" label="获奖日期" width="120" sortable />
          </el-table>
          <el-empty v-else description="暂无竞赛数据" />
        </el-tab-pane>

        <!-- 进修培训 -->
        <el-tab-pane name="trainings">
          <template #label>
            进修培训 <span v-if="currentData.trainings.length" class="count-tag">{{ currentData.trainings.length }}</span>
          </template>
          <el-table :data="currentData.trainings" stripe style="width: 100%" v-if="currentData.trainings.length">
            <el-table-column prop="name" label="培训名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="type" label="培训类型" width="120" />
            <el-table-column prop="form" label="培训形式" width="100" />
            <el-table-column prop="hours" label="学时" width="80" />
            <el-table-column prop="organizer" label="主办单位" width="160" show-overflow-tooltip />
            <el-table-column label="起止时间" width="200">
              <template #default="{ row }">{{ row.startDate || '—' }} 至 {{ row.endDate || '—' }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无培训数据" />
        </el-tab-pane>

        <!-- 著作教材 -->
        <el-tab-pane name="textbooks">
          <template #label>
            著作教材 <span v-if="currentData.textbooks.length" class="count-tag">{{ currentData.textbooks.length }}</span>
          </template>
          <el-table :data="currentData.textbooks" stripe style="width: 100%" v-if="currentData.textbooks.length">
            <el-table-column prop="name" label="名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="publisher" label="出版社" width="160" show-overflow-tooltip />
            <el-table-column prop="publishDate" label="出版日期" width="120" sortable />
            <el-table-column prop="textbookLevel" label="教材级别" width="120" />
            <el-table-column prop="rank" label="排名" width="80">
              <template #default="{ row }">第{{ row.rank }}名</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无著作教材数据" />
        </el-tab-pane>

        <!-- 咨询报告 -->
        <el-tab-pane name="reports">
          <template #label>
            咨询报告 <span v-if="currentData.reports.length" class="count-tag">{{ currentData.reports.length }}</span>
          </template>
          <el-table :data="currentData.reports" stripe style="width: 100%" v-if="currentData.reports.length">
            <el-table-column prop="name" label="报告名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="level" label="级别" width="120" />
            <el-table-column prop="adoptDate" label="采用日期" width="120" sortable />
            <el-table-column prop="rank" label="排名" width="80">
              <template #default="{ row }">第{{ row.rank }}名</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无咨询报告数据" />
        </el-tab-pane>

        <!-- 纵向项目 -->
        <el-tab-pane name="verticalProjects">
          <template #label>
            纵向项目 <span v-if="currentData.verticalProjects.length" class="count-tag">{{ currentData.verticalProjects.length }}</span>
          </template>
          <el-table :data="currentData.verticalProjects" stripe style="width: 100%" v-if="currentData.verticalProjects.length">
            <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="researchType" label="教研/科研" width="120" />
            <el-table-column prop="fundSource" label="基金来源" width="140" show-overflow-tooltip />
            <el-table-column prop="level" label="项目级别" width="100" />
            <el-table-column prop="setupDate" label="立项时间" width="120" sortable />
            <el-table-column prop="updateStatus" label="项目状态" width="100" />
            <el-table-column prop="funds" label="经费(元)" width="100" />
          </el-table>
          <el-empty v-else description="暂无纵向项目数据" />
        </el-tab-pane>

        <!-- 横向项目 -->
        <el-tab-pane name="horizontalProjects">
          <template #label>
            横向项目 <span v-if="currentData.horizontalProjects.length" class="count-tag">{{ currentData.horizontalProjects.length }}</span>
          </template>
          <el-table :data="currentData.horizontalProjects" stripe style="width: 100%" v-if="currentData.horizontalProjects.length">
            <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="researchType" label="教研/科研" width="120" />
            <el-table-column prop="fundSource" label="基金来源" width="140" show-overflow-tooltip />
            <el-table-column prop="level" label="项目级别" width="100" />
            <el-table-column prop="setupDate" label="立项时间" width="120" sortable />
            <el-table-column prop="updateStatus" label="项目状态" width="100" />
            <el-table-column prop="funds" label="经费(元)" width="100" />
          </el-table>
          <el-empty v-else description="暂无横向项目数据" />
        </el-tab-pane>

        <!-- 创新创业 -->
        <el-tab-pane name="innovationProjects">
          <template #label>
            创新创业 <span v-if="currentData.innovationProjects.length" class="count-tag">{{ currentData.innovationProjects.length }}</span>
          </template>
          <el-table :data="currentData.innovationProjects" stripe style="width: 100%" v-if="currentData.innovationProjects.length">
            <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="level" label="项目级别" width="100" />
            <el-table-column prop="status" label="项目状态" width="100" />
            <el-table-column prop="startDate" label="起始时间" width="120" sortable />
            <el-table-column prop="completion" label="是否结题" width="100" />
            <el-table-column prop="leaderStudent" label="负责学生" width="120" show-overflow-tooltip />
            <el-table-column prop="funds" label="经费(元)" width="100" />
          </el-table>
          <el-empty v-else description="暂无创新创业数据" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAchievements, exportAllAchievements } from '../../api/teacher'

const loading = ref(false)
const exportLoading = ref(false)
const activeTab = ref('papers')
const statusFilter = ref('all')

const emptyGroup = () => ({
  papers: [], awards: [], ips: [], competitions: [],
  trainings: [], reports: [], textbooks: [],
  verticalProjects: [], horizontalProjects: [], innovationProjects: []
})

const rawData = reactive({
  approved: emptyGroup(),
  pending: emptyGroup(),
  rejected: emptyGroup()
})

/** 统计某组数据的总数 */
function groupTotal(group) {
  return group.papers.length + group.awards.length + group.ips.length +
    group.competitions.length + group.trainings.length + group.textbooks.length +
    group.reports.length + group.verticalProjects.length + group.horizontalProjects.length +
    group.innovationProjects.length
}

/** 合并多个 group 为一个 */
function mergeGroups(...groups) {
  const merged = emptyGroup()
  for (const g of groups) {
    Object.keys(merged).forEach(key => {
      merged[key] = merged[key].concat(g[key] || [])
    })
  }
  return merged
}

/** 根据当前筛选状态返回对应的数据 */
const currentData = computed(() => {
  if (statusFilter.value === 'approved') return rawData.approved
  if (statusFilter.value === 'pending') return rawData.pending
  if (statusFilter.value === 'rejected') return rawData.rejected
  // 'all' — 合并所有
  return mergeGroups(rawData.approved, rawData.pending, rawData.rejected)
})

/** 计算某个状态分组的总数（用于筛选按钮上的计数） */
function totalCount(status) {
  if (status === 'approved') return groupTotal(rawData.approved)
  if (status === 'pending') return groupTotal(rawData.pending)
  if (status === 'rejected') return groupTotal(rawData.rejected)
  return groupTotal(rawData.approved) + groupTotal(rawData.pending) + groupTotal(rawData.rejected)
}

const statCards = computed(() => {
  const d = currentData.value
  const total = groupTotal(d)
  return [
    { label: '成果总计', value: total },
    { label: '学术论文', value: d.papers.length },
    { label: '获奖荣誉', value: d.awards.length },
    { label: '科研项目', value: d.verticalProjects.length + d.horizontalProjects.length }
  ]
})

const handleExportAll = async () => {
  exportLoading.value = true
  try {
    const res = await exportAllAchievements()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '全部成果数据.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exportLoading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getAchievements()
    const d = res.data || {}
    ;['approved', 'pending', 'rejected'].forEach(status => {
      const src = d[status] || {}
      Object.keys(rawData[status]).forEach(key => {
        rawData[status][key] = src[key] || []
      })
    })
  } catch (error) {
    console.error('获取成果数据失败', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.achievements-container {
  max-width: 1280px;
  margin: 0 auto;
  padding-bottom: 60px;

  .header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

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
  }
}

/* Status Filter Bar */
.status-filter {
  margin-bottom: 20px;

  :deep(.el-radio-group) {
    background: #FFFFFF;
    border: 1px solid var(--color-border);
    border-radius: 8px;
    padding: 4px;
  }

  :deep(.el-radio-button__inner) {
    border: none;
    border-radius: 6px !important;
    font-size: 13px;
    font-weight: 500;
    font-family: var(--font-body);
    padding: 8px 16px;
    color: var(--color-text-light);
    background: transparent;
    box-shadow: none;
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: var(--color-primary);
    color: #fff;
    box-shadow: 0 2px 8px rgba(79, 70, 229, 0.25);
  }

  .status-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;

    &.approved { background: #22C55E; }
    &.pending  { background: #F59E0B; }
    &.rejected { background: #EF4444; }
  }

  .filter-count {
    font-size: 12px;
    opacity: 0.7;
    margin-left: 2px;
  }
}

/* Stats Overview */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  .stat-item {
    background: #FFFFFF;
    border: 1px solid var(--color-border);
    border-radius: 4px;
    padding: 20px;
    transition: all 0.2s;

    &:hover {
      border-color: var(--color-primary);
      transform: translateY(-2px);
    }

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: var(--color-text);
      font-family: var(--font-heading);
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 13px;
      color: var(--color-text-light);
    }
  }
}

/* Tabs Card */
.tabs-card {
  border-radius: 4px;
  border: 1px solid var(--color-border);
  box-shadow: none;
  overflow: hidden;

  :deep(.el-card__body) {
    padding: 0;
  }

  .achievement-tabs {
    :deep(.el-tabs__header) {
      margin: 0;
      padding: 0 24px;
      background: #F8FAFC;
      border-bottom: 1px solid var(--color-border);
    }

    :deep(.el-tabs__nav-wrap::after) {
      display: none;
    }

    :deep(.el-tabs__item) {
      height: 52px;
      line-height: 52px;
      font-size: 14px;
      font-weight: 500;
      color: var(--color-text-light);
      font-family: var(--font-body);
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 6px;

      &.is-active {
        color: var(--color-primary);
        font-weight: 600;
      }
    }

    :deep(.el-tabs__active-bar) {
      background-color: var(--color-primary);
    }

    :deep(.el-tabs__content) {
      padding: 24px;
    }
  }
}

/* Custom Count Tag */
.count-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 10px;
  background-color: #F4F4F5; /* Zinc 100 */
  color: #71717A; /* Zinc 500 */
  font-size: 11px;
  font-weight: 600;
  margin-left: 2px;
  transition: all 0.2s ease;
  line-height: 1;
  font-family: var(--font-body);
}

:deep(.el-tabs__item.is-active) .count-tag {
  background-color: #EEF2FF; /* Indigo 50 */
  color: var(--color-primary);
}

/* Table Styling — consistent with History.vue */
:deep(.el-table) {
  border-radius: 0;
  --el-table-header-bg-color: #F8FAFC;
  --el-table-row-hover-bg-color: #F1F5F9;

  th.el-table__cell {
    font-weight: 600;
    font-size: 13px;
    color: var(--color-text-light);
    letter-spacing: 0.02em;
  }
}

/* Empty state */
:deep(.el-empty) {
  padding: 40px 0;
}

/* Responsive */
@media (max-width: 768px) {
  .achievements-container {
    padding-bottom: 80px;

    .header-actions {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
  }

  .status-filter {
    overflow-x: auto;

    :deep(.el-radio-group) {
      white-space: nowrap;
    }
  }

  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;

    .stat-item {
      padding: 16px;

      .stat-value {
        font-size: 22px;
      }
    }
  }

  .tabs-card .achievement-tabs {
    :deep(.el-tabs__header) {
      padding: 0 12px;
    }

    :deep(.el-tabs__content) {
      padding: 16px;
    }

    :deep(.el-tabs__item) {
      font-size: 13px;
      height: 44px;
      line-height: 44px;
      padding: 0 10px;
    }
  }
}
</style>

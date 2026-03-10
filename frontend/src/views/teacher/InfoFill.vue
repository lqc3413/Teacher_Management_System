<template>
  <div class="info-fill-container">
    <h2 class="page-title">{{ isResubmit ? '重新修改提交' : '教学科研信息填报' }}</h2>

    <!-- 无活跃任务提示 -->
    <el-card v-if="noActiveTask && !isResubmit" class="overview-card" shadow="hover" style="text-align: center; padding: 40px 0;">
      <el-empty description="暂无进行中的采集任务">
        <template #description>
          <p style="color: #71717A; font-size: 14px;">暂无进行中的采集任务，请等待管理员发布新任务</p>
        </template>
        <el-button @click="router.push('/teacher/dashboard')">返回工作台</el-button>
      </el-empty>
    </el-card>

    <!-- 已提交提示 -->
    <el-card v-else-if="alreadySubmitted && !isResubmit" class="overview-card" shadow="hover" style="text-align: center; padding: 40px 0;">
      <el-empty :image-size="80">
        <template #description>
          <h3 style="color: #18181B; margin-bottom: 8px;">您已完成本次任务填报</h3>
          <p style="color: #71717A; font-size: 14px;">当前任务「{{ currentTaskName }}」已提交，无法重复填报</p>
        </template>
        <el-button @click="router.push('/teacher/dashboard')">返回工作台</el-button>
        <el-button type="primary" @click="router.push('/teacher/history')">查看填报记录</el-button>
      </el-empty>
    </el-card>

    <!-- 以下内容仅在有活跃任务且未提交，或重新提交时显示 -->
    <template v-if="(!noActiveTask && !alreadySubmitted) || isResubmit">

    <!-- 当前任务提示 -->
    <el-alert
      v-if="currentTaskName && !isResubmit"
      :title="'当前任务：' + currentTaskName"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    />

    <!-- 退回提示 -->
    <el-alert
      v-if="isResubmit"
      :title="'该提交已被退回'"
      type="warning"
      :description="resubmitRemark ? '退回原因：' + resubmitRemark : '请修改后重新提交'"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    />

    <!-- 1. 概览区域：选择本月有更新的项目 -->
    <el-card class="overview-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>本月动态概览（请勾选本月有更新的项目）</span>
          <div class="header-right">
            <el-button 
              class="excel-link-btn"
              link 
              :icon="Download" 
              @click="handleDownloadTemplate"
            >
              下载模板
            </el-button>
            <el-upload
              class="header-upload"
              :auto-upload="false"
              :show-file-list="false"
              accept=".xlsx,.xls"
              :on-change="handleExcelUpload"
            >
              <el-button 
                class="excel-upload-btn"
                :icon="Upload" 
                :loading="excelUploading"
              >
                导入 Excel
              </el-button>
            </el-upload>

          </div>
        </div>
      </template>
      <div class="switch-grid">
        <el-form-item label="知识产权">
          <el-switch v-model="hasIpUpdate" />
        </el-form-item>
        <el-form-item label="指导竞赛">
          <el-switch v-model="hasCompetition" />
        </el-form-item>
        <el-form-item label="参加培训">
          <el-switch v-model="hasTraining" />
        </el-form-item>
        <el-form-item label="咨询调研报告">
          <el-switch v-model="hasReport" />
        </el-form-item>
        <el-form-item label="出版著作">
          <el-switch v-model="hasBook" />
        </el-form-item>
        <el-form-item label="教学科研奖">
          <el-switch v-model="hasAward" />
        </el-form-item>
        <el-form-item label="发表论文">
          <el-switch v-model="hasPaper" />
        </el-form-item>
        <el-form-item label="纵向项目">
          <el-switch v-model="hasVerticalProject" />
        </el-form-item>
        <el-form-item label="横向项目">
          <el-switch v-model="hasHorizontalProject" />
        </el-form-item>
        <el-form-item label="创新创业项目">
          <el-switch v-model="hasInnovationProject" />
        </el-form-item>
      </div>
    </el-card>

    <!-- 2. 动态表单详情区域 -->
    <el-form
      ref="formRef"
      :model="formData"
      :label-position="isMobile ? 'top' : 'right'"
      class="dynamic-form"
      :class="{ 'mobile-form': isMobile }"
    >
      <!-- 知识产权卡片 -->
      <transition name="el-zoom-in-top">
        <el-card v-if="hasIpUpdate" class="form-card">
          <template #header>
            <div class="card-header-actions">
              <span>知识产权</span>
              <el-button type="primary" size="small" @click="addIpRow">
                新增一行
              </el-button>
            </div>
          </template>
          
          <div
            v-for="(ip, index) in formData.ipList"
            :key="index"
            class="dynamic-row"
          >
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :lg="6">
                <el-form-item
                  label="名称"
                  :prop="'ipList.' + index + '.name'"
                  :rules="{ required: true, message: '请输入名称', trigger: 'blur' }"
                >
                  <el-input v-model="ip.name" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="5">
                <el-form-item
                  label="类型"
                  :prop="'ipList.' + index + '.type'"
                >
                  <el-select v-model="ip.type" placeholder="请选择">
                    <el-option label="发明专利" value="发明专利" />
                    <el-option label="实用新型" value="实用新型" />
                    <el-option label="软件著作权" value="软件著作权" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="5">
                <el-form-item
                  label="获得时间"
                  :prop="'ipList.' + index + '.date'"
                >
                  <el-date-picker
                    v-model="ip.date"
                    type="date"
                    placeholder="选择日期"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="5">
                <el-form-item
                  label="本人排名"
                  :prop="'ipList.' + index + '.rank'"
                >
                  <el-input-number v-model="ip.rank" :min="1" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="5">
                <el-form-item label="其他参与人员">
                  <div style="width: 100%">
                    <div v-for="(p, pIdx) in ip.otherParticipants" :key="pIdx" style="display: flex; align-items: center; margin-bottom: 6px; gap: 6px;">
                      <el-input v-model="ip.otherParticipants[pIdx]" :placeholder="'参与人 ' + (pIdx + 1)" />
                      <el-button v-if="ip.otherParticipants.length > 1" type="danger" :icon="Delete" circle size="small" @click="ip.otherParticipants.splice(pIdx, 1)" />
                    </div>
                    <el-button type="primary" plain size="small" @click="ip.otherParticipants.push('')">+ 添加</el-button>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :xs="12" :sm="6" :lg="3" class="row-action">
                <el-button
                  type="danger"
                  icon="Delete"
                  circle
                  @click="removeIpRow(index)"
                  v-if="formData.ipList.length > 1"
                />
              </el-col>
            </el-row>
          </div>
        </el-card>
      </transition>

      <!-- 指导竞赛卡片 -->
      <transition name="el-zoom-in-top">
        <el-card v-if="hasCompetition" class="form-card" header="指导学生竞赛">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="竞赛类别" prop="competition.category">
                <el-select v-model="formData.competition.category" placeholder="请选择" style="width: 100%">
                  <el-option label="A类" value="A类" />
                  <el-option label="B类" value="B类" />
                  <el-option label="C类" value="C类" />
                  <el-option label="D类" value="D类" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="竞赛名称" prop="competition.name">
                <el-select
                  v-model="formData.competition.name"
                  filterable
                  allow-create
                  default-first-option
                  placeholder="请选择或输入"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in competitionOptions"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="主办单位或发证单位" prop="competition.organizer">
                <el-input v-model="formData.competition.organizer" placeholder="以获奖为准" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="获奖时间" prop="competition.awardDate">
                <el-date-picker
                  v-model="formData.competition.awardDate"
                  type="date"
                  placeholder="选择日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="证书编号" prop="competition.certNo">
                <el-input v-model="formData.competition.certNo" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="证书完整名称" prop="competition.certName">
                <el-input v-model="formData.competition.certName" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="奖项级别" prop="competition.awardLevel">
                <el-select v-model="formData.competition.awardLevel" placeholder="请选择" style="width: 100%">
                  <el-option label="国家级" value="国家级" />
                  <el-option label="省级" value="省级" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="奖项等级" prop="competition.awardGrade">
                <el-select v-model="formData.competition.awardGrade" placeholder="请选择" style="width: 100%">
                  <el-option label="特等奖" value="特等奖" />
                  <el-option label="一等奖" value="一等奖" />
                  <el-option label="二等奖" value="二等奖" />
                  <el-option label="三等奖" value="三等奖" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="参赛学生个人姓名或团队成员">
                <div style="width: 100%">
                  <div v-for="(s, sIdx) in formData.competition.students" :key="sIdx" style="display: flex; align-items: center; margin-bottom: 6px; gap: 6px;">
                    <el-input v-model="formData.competition.students[sIdx]" :placeholder="'学生 ' + (sIdx + 1)" />
                    <el-button v-if="formData.competition.students.length > 1" type="danger" :icon="Delete" circle size="small" @click="formData.competition.students.splice(sIdx, 1)" />
                  </div>
                  <el-button type="primary" plain size="small" @click="formData.competition.students.push('')">+ 添加学生</el-button>
                </div>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="指导教师个人姓名或团队成员">
                <div style="width: 100%">
                  <div v-for="(t, tIdx) in formData.competition.advisorTeachers" :key="tIdx" style="display: flex; align-items: center; margin-bottom: 6px; gap: 6px;">
                    <el-input v-model="formData.competition.advisorTeachers[tIdx]" :placeholder="'教师 ' + (tIdx + 1)" />
                    <el-button v-if="formData.competition.advisorTeachers.length > 1" type="danger" :icon="Delete" circle size="small" @click="formData.competition.advisorTeachers.splice(tIdx, 1)" />
                  </div>
                  <el-button type="primary" plain size="small" @click="formData.competition.advisorTeachers.push('')">+ 添加教师</el-button>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
      </transition>

      <!-- 培训进修卡片 -->
      <transition name="el-zoom-in-top">
        <el-card v-if="hasTraining" class="form-card" header="培训进修情况">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="培训类型" prop="training.type">
                <el-input v-model="formData.training.type" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="培训名称" prop="training.name">
                <el-input v-model="formData.training.name" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="培训形式" prop="training.form">
                <el-input v-model="formData.training.form" placeholder="如：线上/线下" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="学时" prop="training.hours">
                <el-input-number v-model="formData.training.hours" :min="0" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="主办方" prop="training.organizer">
                <el-input v-model="formData.training.organizer" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="开始时间" prop="training.startDate">
                <el-date-picker
                  v-model="formData.training.startDate"
                  type="date"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="结束时间" prop="training.endDate">
                <el-date-picker
                  v-model="formData.training.endDate"
                  type="date"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
      </transition>

      <!-- 咨询调研报告卡片 -->
      <transition name="el-zoom-in-top">
        <el-card v-if="hasReport" class="form-card" header="咨询调研报告">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="报告名称" prop="report.name">
                <el-input v-model="formData.report.name" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="报告采纳单位级别" prop="report.level">
                <el-select v-model="formData.report.level" placeholder="请选择" style="width: 100%">
                  <el-option label="国家级" value="国家级" />
                  <el-option label="省部级" value="省部级" />
                  <el-option label="地厅级" value="地厅级" />
                  <el-option label="县处级" value="县处级" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="采纳时间" prop="report.date">
                <el-date-picker
                  v-model="formData.report.date"
                  type="month"
                  format="YYYY-MM"
                  value-format="YYYY-MM"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="本人排名" prop="report.rank">
                <el-input-number v-model="formData.report.rank" :min="1" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="其他参与人员">
                <div style="width: 100%">
                  <div v-for="(o, oIdx) in formData.report.others" :key="oIdx" style="display: flex; align-items: center; margin-bottom: 6px; gap: 6px;">
                    <el-input v-model="formData.report.others[oIdx]" :placeholder="'参与人 ' + (oIdx + 1)" />
                    <el-button v-if="formData.report.others.length > 1" type="danger" :icon="Delete" circle size="small" @click="formData.report.others.splice(oIdx, 1)" />
                  </div>
                  <el-button type="primary" plain size="small" @click="formData.report.others.push('')">+ 添加成员</el-button>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
      </transition>

      <!-- 出版著作卡片 -->
      <transition name="el-zoom-in-top">
        <el-card v-if="hasBook" class="form-card" header="出版著作">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="著作名称" prop="book.name">
                <el-input v-model="formData.book.name" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="出版社" prop="book.publisher">
                <el-input v-model="formData.book.publisher" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="出版时间" prop="book.date">
                <el-date-picker
                  v-model="formData.book.date"
                  type="month"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="教材入选情况" prop="book.level">
                <el-select v-model="formData.book.level" placeholder="请选择" style="width: 100%">
                  <el-option label="国家级规划教材" value="国家级规划教材" />
                  <el-option label="省级规划教材" value="省级规划教材" />
                  <el-option label="校级教材" value="校级教材" />
                  <el-option label="未入选" value="未入选" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="12" :sm="6" :lg="4">
              <el-form-item label="本人排名" prop="book.rank">
                <el-input-number v-model="formData.book.rank" :min="1" />
              </el-form-item>
            </el-col>
            <el-col :xs="12" :sm="6" :lg="4">
              <el-form-item label="入选时间" prop="book.selectionDate">
                <el-date-picker
                  v-model="formData.book.selectionDate"
                  type="month"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
      </transition>

      <!-- 教学科研成果奖卡片 -->
      <transition name="el-zoom-in-top">
        <el-card v-if="hasAward" class="form-card" header="教学科研成果奖">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="获奖成果名称" prop="award.name">
                <el-input v-model="formData.award.name" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="证书编号" prop="award.certNo">
                <el-input v-model="formData.award.certNo" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="成果类型" prop="award.type">
                <el-select v-model="formData.award.type" placeholder="请选择" style="width: 100%">
                  <el-option label="教学成果奖" value="教学成果奖" />
                  <el-option label="科研成果奖" value="科研成果奖" />
                  <el-option label="指导竞赛奖" value="指导竞赛奖" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="获奖级别" prop="award.level">
                <el-select v-model="formData.award.level" placeholder="请选择" style="width: 100%">
                  <el-option label="国家级" value="国家级" />
                  <el-option label="省级" value="省级" />
                  <el-option label="市级" value="市级" />
                  <el-option label="校级" value="校级" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="获奖等级" prop="award.grade">
                <el-select v-model="formData.award.grade" placeholder="请选择" style="width: 100%">
                  <el-option label="特等奖" value="特等奖" />
                  <el-option label="一等奖" value="一等奖" />
                  <el-option label="二等奖" value="二等奖" />
                  <el-option label="三等奖" value="三等奖" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="获奖时间" prop="award.date">
                <el-date-picker
                  v-model="formData.award.date"
                  type="date"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="本人排名" prop="award.rank">
                <el-input-number v-model="formData.award.rank" :min="1" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="所在单位排名" prop="award.orgRank">
                <el-input-number v-model="formData.award.orgRank" :min="1" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
      </transition>

      <!-- 发表论文卡片 -->
      <transition name="el-zoom-in-top">
        <el-card v-if="hasPaper" class="form-card" header="发表论文">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="论文类型" prop="paper.paperType">
                <el-select v-model="formData.paper.paperType" placeholder="请选择" style="width: 100%">
                  <el-option label="学术论文" value="学术论文" />
                  <el-option label="会议论文" value="会议论文" />
                  <el-option label="综述论文" value="综述论文" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="论文名称" prop="paper.paperName">
                <el-input v-model="formData.paper.paperName" placeholder="请输入论文名称" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="作者类型" prop="paper.authorType">
                <el-select v-model="formData.paper.authorType" placeholder="请选择" style="width: 100%">
                  <el-option label="第一作者" value="第一作者" />
                  <el-option label="第二作者" value="第二作者" />
                  <el-option label="通讯作者" value="通讯作者" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="其他作者">
                <div style="width: 100%">
                  <div v-for="(a, aIdx) in formData.paper.otherAuthors" :key="aIdx" style="display: flex; align-items: center; margin-bottom: 6px; gap: 6px;">
                    <el-input v-model="formData.paper.otherAuthors[aIdx]" :placeholder="'作者 ' + (aIdx + 1)" />
                    <el-button v-if="formData.paper.otherAuthors.length > 1" type="danger" :icon="Delete" circle size="small" @click="formData.paper.otherAuthors.splice(aIdx, 1)" />
                  </div>
                  <el-button type="primary" plain size="small" @click="formData.paper.otherAuthors.push('')">+ 添加作者</el-button>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="发表期刊" prop="paper.journalName">
                <el-input v-model="formData.paper.journalName" placeholder="请输入发表期刊" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="收录类别" prop="paper.indexCategory">
                <el-select v-model="formData.paper.indexCategory" placeholder="请选择" style="width: 100%">
                  <el-option label="SCI" value="SCI" />
                  <el-option label="SSCI" value="SSCI" />
                  <el-option label="EI" value="EI" />
                  <el-option label="CSCD" value="CSCD" />
                  <el-option label="北核" value="北核" />
                  <el-option label="南核" value="南核" />
                  <el-option label="普刊" value="普刊" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="发表时间" prop="paper.publishDate">
                <el-date-picker v-model="formData.paper.publishDate" type="month" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
      </transition>

      <!-- 纵向项目卡片 -->
      <transition name="el-zoom-in-top">
        <el-card v-if="hasVerticalProject" class="form-card" header="纵向项目">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="教研或科研类型" prop="verticalProject.researchType">
                <el-select v-model="formData.verticalProject.researchType" placeholder="请选择" style="width: 100%">
                  <el-option label="教研" value="教研" />
                  <el-option label="科研" value="科研" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目名称" prop="verticalProject.projectName">
                <el-input v-model="formData.verticalProject.projectName" placeholder="仅填本时间段立项或结项的项目" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目基金来源" prop="verticalProject.fundSource">
                <el-input v-model="formData.verticalProject.fundSource" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目级别" prop="verticalProject.level">
                <el-select v-model="formData.verticalProject.level" placeholder="请选择" style="width: 100%">
                  <el-option label="国家级" value="国家级" />
                  <el-option label="省部级重点" value="省部级重点" />
                  <el-option label="省部级一般" value="省部级一般" />
                  <el-option label="市厅级" value="市厅级" />
                  <el-option label="院级重点" value="院级重点" />
                  <el-option label="院级一般" value="院级一般" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="立项时间" prop="verticalProject.setupDate">
                <el-date-picker v-model="formData.verticalProject.setupDate" type="date" placeholder="选择日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="立项编号或文号" prop="verticalProject.setupNo">
                <el-input v-model="formData.verticalProject.setupNo" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目更新状态" prop="verticalProject.updateStatus">
                <el-select v-model="formData.verticalProject.updateStatus" placeholder="请选择" style="width: 100%">
                  <el-option label="立项" value="立项" />
                  <el-option label="结项" value="结项" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="结题验收或鉴定时间" prop="verticalProject.acceptDate">
                <el-date-picker v-model="formData.verticalProject.acceptDate" type="date" placeholder="选择日期" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目经费金额（元）" prop="verticalProject.funds">
                <el-input v-model="formData.verticalProject.funds" />
              </el-form-item>
            </el-col>
          </el-row>
          <!-- 团队成员动态输入 -->
          <el-form-item label="项目团队成员（第一个为项目负责人）">
            <div style="width: 100%">
              <div v-for="(member, idx) in formData.verticalProject.teamMembers" :key="idx" style="display: flex; align-items: center; margin-bottom: 8px; gap: 8px;">
                <el-input v-model="formData.verticalProject.teamMembers[idx]" :placeholder="idx === 0 ? '项目负责人' : '团队成员 ' + (idx + 1)" style="flex: 1" />
                <el-button v-if="formData.verticalProject.teamMembers.length > 1" type="danger" :icon="Delete" circle size="small" @click="formData.verticalProject.teamMembers.splice(idx, 1)" />
              </div>
              <el-button type="primary" plain size="small" @click="formData.verticalProject.teamMembers.push('')">+ 添加成员</el-button>
            </div>
          </el-form-item>
        </el-card>
      </transition>

      <!-- 横向项目卡片 -->
      <transition name="el-zoom-in-top">
        <el-card v-if="hasHorizontalProject" class="form-card" header="横向项目">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="教研或科研类型" prop="horizontalProject.researchType">
                <el-select v-model="formData.horizontalProject.researchType" placeholder="请选择" style="width: 100%">
                  <el-option label="教研" value="教研" />
                  <el-option label="科研" value="科研" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目名称" prop="horizontalProject.projectName">
                <el-input v-model="formData.horizontalProject.projectName" placeholder="仅填本时间段立项或结项的项目" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目基金来源" prop="horizontalProject.fundSource">
                <el-input v-model="formData.horizontalProject.fundSource" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目级别" prop="horizontalProject.level">
                <el-select v-model="formData.horizontalProject.level" placeholder="请选择" style="width: 100%">
                  <el-option label="国家级" value="国家级" />
                  <el-option label="省部级重点" value="省部级重点" />
                  <el-option label="省部级一般" value="省部级一般" />
                  <el-option label="市厅级" value="市厅级" />
                  <el-option label="院级重点" value="院级重点" />
                  <el-option label="院级一般" value="院级一般" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="立项时间" prop="horizontalProject.setupDate">
                <el-date-picker v-model="formData.horizontalProject.setupDate" type="date" placeholder="选择日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="立项编号或文号" prop="horizontalProject.setupNo">
                <el-input v-model="formData.horizontalProject.setupNo" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目更新状态" prop="horizontalProject.updateStatus">
                <el-select v-model="formData.horizontalProject.updateStatus" placeholder="请选择" style="width: 100%">
                  <el-option label="立项" value="立项" />
                  <el-option label="结项" value="结项" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="结题验收或鉴定时间" prop="horizontalProject.acceptDate">
                <el-date-picker v-model="formData.horizontalProject.acceptDate" type="date" placeholder="选择日期" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目经费金额（元）" prop="horizontalProject.funds">
                <el-input v-model="formData.horizontalProject.funds" />
              </el-form-item>
            </el-col>
          </el-row>
          <!-- 团队成员动态输入 -->
          <el-form-item label="项目团队成员（第一个为项目负责人）">
            <div style="width: 100%">
              <div v-for="(member, idx) in formData.horizontalProject.teamMembers" :key="idx" style="display: flex; align-items: center; margin-bottom: 8px; gap: 8px;">
                <el-input v-model="formData.horizontalProject.teamMembers[idx]" :placeholder="idx === 0 ? '项目负责人' : '团队成员 ' + (idx + 1)" style="flex: 1" />
                <el-button v-if="formData.horizontalProject.teamMembers.length > 1" type="danger" :icon="Delete" circle size="small" @click="formData.horizontalProject.teamMembers.splice(idx, 1)" />
              </div>
              <el-button type="primary" plain size="small" @click="formData.horizontalProject.teamMembers.push('')">+ 添加成员</el-button>
            </div>
          </el-form-item>
        </el-card>
      </transition>

      <!-- 创新创业项目卡片 -->
      <transition name="el-zoom-in-top">
        <el-card v-if="hasInnovationProject" class="form-card" header="指导学生参加创新创业项目">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目状态" prop="innovationProject.status">
                <el-select v-model="formData.innovationProject.status" placeholder="请选择" style="width: 100%">
                  <el-option label="立项" value="立项" />
                  <el-option label="结项" value="结项" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="级别" prop="innovationProject.level">
                <el-select v-model="formData.innovationProject.level" placeholder="请选择" style="width: 100%">
                  <el-option label="国家级" value="国家级" />
                  <el-option label="省级" value="省级" />
                  <el-option label="院级" value="院级" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目名称" prop="innovationProject.projectName">
                <el-input v-model="formData.innovationProject.projectName" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="起始时间" prop="innovationProject.startDate">
                <el-date-picker
                  v-model="formData.innovationProject.startDate"
                  type="date"
                  placeholder="选择日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="是否结题" prop="innovationProject.completion">
                <el-select v-model="formData.innovationProject.completion" placeholder="请选择" style="width: 100%">
                  <el-option label="已结题" value="已结题" />
                  <el-option label="未结题" value="未结题" />
                  <el-option label="已放弃" value="已放弃" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目负责学生" prop="innovationProject.leaderStudent">
                <el-input v-model="formData.innovationProject.leaderStudent" placeholder="姓名+学号" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="6">
              <el-form-item label="项目经费（元）" prop="innovationProject.funds">
                <el-input v-model="formData.innovationProject.funds" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="其他参与学生">
                <div style="width: 100%">
                  <div v-for="(st, stIdx) in formData.innovationProject.otherStudents" :key="stIdx" style="display: flex; align-items: center; margin-bottom: 6px; gap: 6px;">
                    <el-input v-model="formData.innovationProject.otherStudents[stIdx]" :placeholder="'学生 ' + (stIdx + 1)" />
                    <el-button v-if="formData.innovationProject.otherStudents.length > 1" type="danger" :icon="Delete" circle size="small" @click="formData.innovationProject.otherStudents.splice(stIdx, 1)" />
                  </div>
                  <el-button type="primary" plain size="small" @click="formData.innovationProject.otherStudents.push('')">+ 添加学生</el-button>
                </div>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="该项目论文发表情况" prop="innovationProject.paperInfo">
                <el-input
                  v-model="formData.innovationProject.paperInfo"
                  type="textarea"
                  :rows="2"
                  placeholder="以 作者、论文题目、期刊或会议、页码 格式录入"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="其他指导教师">
                <div style="width: 100%">
                  <div v-for="(tc, tcIdx) in formData.innovationProject.otherTeachers" :key="tcIdx" style="display: flex; align-items: center; margin-bottom: 6px; gap: 6px;">
                    <el-input v-model="formData.innovationProject.otherTeachers[tcIdx]" :placeholder="'教师 ' + (tcIdx + 1)" />
                    <el-button v-if="formData.innovationProject.otherTeachers.length > 1" type="danger" :icon="Delete" circle size="small" @click="formData.innovationProject.otherTeachers.splice(tcIdx, 1)" />
                  </div>
                  <el-button type="primary" plain size="small" @click="formData.innovationProject.otherTeachers.push('')">+ 添加教师</el-button>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
      </transition>
    </el-form>

    <!-- 3. 底部操作栏 -->
    <div class="form-actions" :class="{ 'sticky-footer': isMobile }">
      <el-button type="primary" size="large" @click="handleSubmit" :block="isMobile">
        提交申报
      </el-button>
      <el-button size="large" @click="handleReset" :block="isMobile">重置</el-button>


    </div>
    <!-- Pro Max Success Dialog -->
    <el-dialog
      v-model="successVisible"
      :show-close="false"
      width="360px"
      align-center
      class="pro-max-success-dialog"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="success-content">
        <div class="success-anim-wrapper">
          <div class="circle-bg"></div>
          <svg class="check-icon" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
             <path d="M14 24 L21 31 L34 17" stroke="#059669" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
          </svg>
        </div>
        
        <h3 class="success-title">提交成功 !</h3>
        
        <div class="success-desc">
          <p>任务名称 <span class="highlight">{{ lastTaskName }}</span></p>
          <div class="status-row">
            当前状态 <span class="status-badge">审核中</span>
          </div>
        </div>
        
        <div class="success-action">
          <button class="pro-btn" @click="handleSuccessClose">
            返回首页
          </button>
        </div>
      </div>
    </el-dialog>

    </template><!-- end v-if !noActiveTask -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { submitTeacherInfo, getSubmissionDetail, downloadExcelTemplate, importExcelData, getCurrentTask } from '../../api/teacher'
import { Upload, Download } from '@element-plus/icons-vue'
import { useIsMobile } from '../../hooks/useIsMobile'
import dayjs from 'dayjs'

const { isMobile } = useIsMobile()
const router = useRouter()
const route = useRoute()
const submitting = ref(false)
const successVisible = ref(false)
const lastTaskName = ref('')
const excelUploading = ref(false)

// 任务驱动状态
const currentTaskId = ref(null)
const currentTaskName = ref('')
const noActiveTask = ref(false)
const alreadySubmitted = ref(false)

const handleSuccessClose = () => {
  successVisible.value = false
  router.push('/teacher/dashboard')
}

// Excel 模板下载
const handleDownloadTemplate = async () => {
  try {
    const res = await downloadExcelTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '教资填报标准模板.xlsx'
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('模板下载成功')
  } catch (e) {
    ElMessage.error('模板下载失败')
  }
}

// Excel 上传导入
const handleExcelUpload = async (file) => {
  const taskId = route.query.taskId ? Number(route.query.taskId) : currentTaskId.value
  if (!taskId) {
    ElMessage.warning('当前没有进行中的采集任务，无法导入')
    return
  }

  try {
    await ElMessageBox.confirm(
      '上传后将覆盖本次任务已有的填报数据，确认导入？',
      '确认导入',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return // 用户取消
  }

  const fd = new FormData()
  fd.append('file', file.raw)
  fd.append('taskId', taskId)

  excelUploading.value = true
  try {
    await importExcelData(fd)
    ElMessage.success('Excel 数据导入成功！')
    successVisible.value = true
    lastTaskName.value = currentTaskName.value || '当前任务'
  } catch (e) {
    ElMessage.error('导入失败：' + (e.message || '未知错误'))
  } finally {
    excelUploading.value = false
  }
}

// 重新修改相关状态
const isResubmit = ref(false)
const resubmitMonth = ref('')
const resubmitRemark = ref('')

// 开关状态
const hasIpUpdate = ref(false)
const hasCompetition = ref(false)
const hasTraining = ref(false)
const hasReport = ref(false)
const hasBook = ref(false)
const hasAward = ref(false)
const hasPaper = ref(false)
const hasVerticalProject = ref(false)
const hasHorizontalProject = ref(false)
const hasInnovationProject = ref(false)

// 表单数据
const formRef = ref(null)
const formData = reactive({
  ipList: [
    { name: '', type: '', date: '', rank: 1, otherParticipants: [''] }
  ],
  competition: {
    category: '',
    name: '',
    organizer: '',
    awardDate: '',
    certNo: '',
    certName: '',
    awardLevel: '',
    awardGrade: '',
    students: [''],
    advisorTeachers: ['']
  },
  training: {
    type: '',
    name: '',
    form: '',
    hours: 0,
    organizer: '',
    startDate: '',
    endDate: ''
  },
  report: {
    name: '',
    level: '',
    date: '',
    rank: 1,
    others: ['']
  },
  book: {
    name: '',
    publisher: '',
    date: '',
    level: '',
    rank: 1,
    selectionDate: ''
  },
  award: {
    name: '',
    type: '',
    level: '',
    grade: '',
    rank: 1,
    orgRank: 1,
    date: '',
    certNo: ''
  },
  paper: {
    paperType: '',
    paperName: '',
    authorType: '',
    otherAuthors: [''],
    journalName: '',
    indexCategory: '',
    publishDate: ''
  },
  verticalProject: {
    researchType: '',
    projectName: '',
    fundSource: '',
    level: '',
    teamMembers: [''],
    setupDate: '',
    setupNo: '',
    updateStatus: '',
    acceptDate: '',
    funds: ''
  },
  horizontalProject: {
    researchType: '',
    projectName: '',
    fundSource: '',
    level: '',
    teamMembers: [''],
    setupDate: '',
    setupNo: '',
    updateStatus: '',
    acceptDate: '',
    funds: ''
  },
  innovationProject: {
    status: '',
    level: '',
    projectName: '',
    startDate: '',
    completion: '',
    leaderStudent: '',
    otherStudents: [''],
    funds: '',
    paperInfo: '',
    otherTeachers: ['']
  }
})

// 日期格式化辅助
const fmt = (val, pattern = 'YYYY-MM-DD') => {
  if (!val) return ''
  const parsed = dayjs(val)
  if (parsed.isValid()) {
    return parsed.format(pattern)
  }
  return typeof val === 'string' ? val.trim() : ''
}

// 预置竞赛选项
const competitionOptions = [
  "睿抗机器人开发者大赛",
  "挑战杯",
  "蓝桥杯",
  "互联网+",
  "中国高校计算机大赛",
  "ACM-ICPC",
  "华为ICT大赛",
  "其他"
]

// 动态行操作
const addIpRow = () => {
  formData.ipList.push({ name: '', type: '', date: '', rank: 1, otherParticipants: [''] })
}

const removeIpRow = (index) => {
  formData.ipList.splice(index, 1)
}

// 提交处理
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid, fields) => {
    if (!valid) {
      ElMessage.error('请检查填写内容是否完整')
      console.log('校验失败:', fields)
      return
    }

    // 至少要开启一个开关
    const hasAny = hasIpUpdate.value || hasCompetition.value ||
        hasTraining.value || hasReport.value || hasBook.value || hasAward.value ||
        hasPaper.value || hasVerticalProject.value || hasHorizontalProject.value || hasInnovationProject.value
    if (!hasAny) {
      ElMessage.warning('请至少勾选一个本月有更新的项目')
      return
    }

    // 构造 payload — taskId 必填
    const resolvedTaskId = route.query.taskId ? Number(route.query.taskId) : currentTaskId.value
    if (!resolvedTaskId && !isResubmit.value) {
      ElMessage.warning('当前没有进行中的采集任务，无法提交')
      return
    }
    const payload = {
      submitMonth: isResubmit.value ? resubmitMonth.value : dayjs().format('YYYY-MM'),
      taskId: resolvedTaskId
    }


    if (hasIpUpdate.value) {
      payload.ipList = formData.ipList.map(ip => ({
        ...ip,
        date: fmt(ip.date),
        otherParticipants: ip.otherParticipants.filter(p => p.trim())
      }))
    }

    if (hasCompetition.value) {
      payload.competition = {
        ...formData.competition,
        awardDate: fmt(formData.competition.awardDate),
        students: formData.competition.students.filter(s => s.trim()),
        advisorTeachers: formData.competition.advisorTeachers.filter(t => t.trim())
      }
    }

    if (hasTraining.value) {
      payload.training = {
        ...formData.training,
        startDate: fmt(formData.training.startDate),
        endDate: fmt(formData.training.endDate)
      }
    }

    if (hasReport.value) {
      payload.report = {
        ...formData.report,
        others: formData.report.others.filter(o => o.trim())
      }
    }

    if (hasBook.value) {
      payload.book = {
        ...formData.book,
        date: fmt(formData.book.date, 'YYYY-MM'),
        selectionDate: fmt(formData.book.selectionDate, 'YYYY-MM')
      }
    }

    if (hasAward.value) {
      payload.award = {
        ...formData.award,
        date: fmt(formData.award.date)
      }
    }

    if (hasPaper.value) {
      payload.paperList = [{
        paperType: formData.paper.paperType,
        paperName: formData.paper.paperName,
        authorType: formData.paper.authorType,
        otherAuthors: formData.paper.otherAuthors.filter(a => a.trim()),
        journalName: formData.paper.journalName,
        indexCategory: formData.paper.indexCategory,
        publishDate: fmt(formData.paper.publishDate, 'YYYY-MM')
      }]
    }

    if (hasVerticalProject.value) {
      payload.verticalProject = {
        ...formData.verticalProject,
        setupDate: fmt(formData.verticalProject.setupDate),
        acceptDate: fmt(formData.verticalProject.acceptDate),
        teamMembers: formData.verticalProject.teamMembers.filter(m => m.trim())
      }
    }

    if (hasHorizontalProject.value) {
      payload.horizontalProject = {
        ...formData.horizontalProject,
        setupDate: fmt(formData.horizontalProject.setupDate),
        acceptDate: fmt(formData.horizontalProject.acceptDate),
        teamMembers: formData.horizontalProject.teamMembers.filter(m => m.trim())
      }
    }

    if (hasInnovationProject.value) {
      payload.innovationProject = {
        ...formData.innovationProject,
        startDate: fmt(formData.innovationProject.startDate),
        otherStudents: formData.innovationProject.otherStudents.filter(s => s.trim()),
        otherTeachers: formData.innovationProject.otherTeachers.filter(t => t.trim())
      }
    }

    try {
      submitting.value = true
      // 模拟一点延时提升体验 (可选)
      // await new Promise(r => setTimeout(r, 600))
      
      await submitTeacherInfo(payload)
      
      // 成功后显示自定义弹窗
      lastTaskName.value = currentTaskName.value || payload.submitMonth
      successVisible.value = true
      
    } catch (e) {
      console.error('提交出错:', e)
    } finally {
      submitting.value = false
    }
  })
}


const handleReset = () => {
  formRef.value.resetFields()
  hasDegreeUpdate.value = false
  hasIpUpdate.value = false
  hasCompetition.value = false
  hasTraining.value = false
  hasReport.value = false
  hasBook.value = false
  hasAward.value = false
  hasPaper.value = false
  hasVerticalProject.value = false
  hasHorizontalProject.value = false
  hasInnovationProject.value = false
}

const loadCurrentTaskContext = async (taskId = null) => {
  const taskRes = await getCurrentTask(taskId ? { taskId } : undefined)
  const taskData = taskRes.data || null

  if (taskData?.task) {
    currentTaskId.value = taskData.task.id
    currentTaskName.value = taskData.task.taskName || ''
  }

  return taskData
}

const redirectExpiredResubmit = () => {
  ElMessage.warning('当前任务已截止，无法重新提交')
  router.replace('/teacher/history')
}

/**
 * 重新修改：从 Detail API 加载被退回的提交数据并回填表单
 */
onMounted(async () => {
  const resubmitId = route.query.resubmitId ? Number(route.query.resubmitId) : null
  const queryTaskId = route.query.taskId ? Number(route.query.taskId) : null
  let currentTaskData = null

  if (queryTaskId) {
    currentTaskId.value = queryTaskId
  }

  if (resubmitId) {
    try {
      currentTaskData = await loadCurrentTaskContext(queryTaskId)
    } catch (e) {
      console.error('获取当前任务失败', e)
      redirectExpiredResubmit()
      return
    }

    const activeTaskId = currentTaskData?.task?.id || null
    const taskMatches = !queryTaskId || activeTaskId === queryTaskId
    if (!activeTaskId || !taskMatches || currentTaskData?.canResubmit !== true) {
      redirectExpiredResubmit()
      return
    }

    alreadySubmitted.value = false
    noActiveTask.value = false
  } else if (!queryTaskId) {
    try {
      currentTaskData = await loadCurrentTaskContext(queryTaskId)
      if (currentTaskData?.task) {
        if (currentTaskData.hasSubmitted) {
          alreadySubmitted.value = true
          return
        }
      } else {
        noActiveTask.value = true
        return
      }
    } catch (e) {
      console.error('获取当前任务失败', e)
      noActiveTask.value = true
      return
    }
  } else if (!currentTaskName.value) {
    try {
      currentTaskData = await loadCurrentTaskContext(queryTaskId)
      if (currentTaskData?.task && currentTaskData.task.id === queryTaskId) {
        if (currentTaskData.hasSubmitted) {
          alreadySubmitted.value = true
          return
        }
      }
    } catch (e) {
      // 非关键错误，忽略
    }
  }

  if (!resubmitId) return

  try {
    const res = await getSubmissionDetail(resubmitId)
    const d = res.data
    if (!d) return

    isResubmit.value = true
    alreadySubmitted.value = false
    resubmitMonth.value = d.submitMonth || ''
    resubmitRemark.value = d.auditRemark || ''


    // 回填知识产权
    if (d.ipList && d.ipList.length) {
      hasIpUpdate.value = true
      formData.ipList = d.ipList.map(ip => ({
        name: ip.name || '',
        type: ip.type || '',
        date: ip.obtainDate || '',
        rank: ip.rank || 1,
        otherParticipants: ip.otherParticipants ? ip.otherParticipants.split(',').filter(s => s) : ['']
      }))
    }

    // 回填竞赛
    if (d.competition) {
      hasCompetition.value = true
      const c = d.competition
      formData.competition.category = c.category || ''
      formData.competition.name = c.name || ''
      formData.competition.organizer = c.organizer || ''
      formData.competition.awardDate = c.awardDate || ''
      formData.competition.certNo = c.certNo || ''
      formData.competition.certName = c.certName || ''
      formData.competition.awardLevel = c.awardLevel || ''
      formData.competition.awardGrade = c.awardGrade || ''
      formData.competition.students = c.students ? c.students.split(',').filter(s => s) : ['']
      formData.competition.advisorTeachers = c.advisorTeachers ? c.advisorTeachers.split(',').filter(s => s) : ['']
    }

    // 回填培训
    if (d.training) {
      hasTraining.value = true
      const t = d.training
      formData.training.type = t.type || ''
      formData.training.name = t.name || ''
      formData.training.form = t.form || ''
      formData.training.hours = t.hours || 0
      formData.training.organizer = t.organizer || ''
      formData.training.startDate = t.startDate || ''
      formData.training.endDate = t.endDate || ''
    }

    // 回填咨询报告
    if (d.report) {
      hasReport.value = true
      const r = d.report
      formData.report.name = r.name || ''
      formData.report.level = r.level || ''
      formData.report.date = r.adoptDate || ''
      formData.report.rank = r.rank || 1
      formData.report.others = r.others ? r.others.split(',').filter(s => s) : ['']
    }

    // 回填著作教材
    if (d.book) {
      hasBook.value = true
      const bk = d.book
      formData.book.name = bk.name || ''
      formData.book.publisher = bk.publisher || ''
      formData.book.date = bk.publishDate || ''
      formData.book.level = bk.textbookLevel || ''
      formData.book.rank = bk.rank || 1
      formData.book.selectionDate = bk.selectionDate || ''
    }

    // 回填奖项
    if (d.award) {
      hasAward.value = true
      const a = d.award
      formData.award.name = a.name || ''
      formData.award.type = a.type || ''
      formData.award.level = a.level || ''
      formData.award.grade = a.grade || ''
      formData.award.rank = a.rank || 1
      formData.award.orgRank = a.orgRank || 1
      formData.award.date = a.awardDate || ''
      formData.award.certNo = a.certNo || ''
    }

    // 回填论文（【B04 修复】兼容 paperList 和旧的 paper 字段）
    const paperData = (d.paperList && d.paperList.length) ? d.paperList[0] : d.paper
    if (paperData) {
      hasPaper.value = true
      const p = paperData
      formData.paper.paperType = p.paperType || ''
      formData.paper.paperName = p.paperName || ''
      formData.paper.authorType = p.authorType || ''
      formData.paper.otherAuthors = p.otherAuthors ? p.otherAuthors.split(',').filter(s => s) : ['']
      formData.paper.journalName = p.journalName || ''
      formData.paper.indexCategory = p.indexCategory || ''
      formData.paper.publishDate = p.publishDate || ''
    }

    // 回填纵向项目
    if (d.verticalProject) {
      hasVerticalProject.value = true
      const vp = d.verticalProject
      formData.verticalProject.researchType = vp.researchType || ''
      formData.verticalProject.projectName = vp.projectName || ''
      formData.verticalProject.fundSource = vp.fundSource || ''
      formData.verticalProject.level = vp.level || ''
      formData.verticalProject.setupDate = vp.setupDate || ''
      formData.verticalProject.setupNo = vp.setupNo || ''
      formData.verticalProject.updateStatus = vp.updateStatus || ''
      formData.verticalProject.acceptDate = vp.acceptDate || ''
      formData.verticalProject.funds = vp.funds || ''
      formData.verticalProject.teamMembers = vp.teamMembers ? vp.teamMembers.split(',').filter(s => s) : ['']
    }

    // 回填横向项目
    if (d.horizontalProject) {
      hasHorizontalProject.value = true
      const hp = d.horizontalProject
      formData.horizontalProject.researchType = hp.researchType || ''
      formData.horizontalProject.projectName = hp.projectName || ''
      formData.horizontalProject.fundSource = hp.fundSource || ''
      formData.horizontalProject.level = hp.level || ''
      formData.horizontalProject.setupDate = hp.setupDate || ''
      formData.horizontalProject.setupNo = hp.setupNo || ''
      formData.horizontalProject.updateStatus = hp.updateStatus || ''
      formData.horizontalProject.acceptDate = hp.acceptDate || ''
      formData.horizontalProject.funds = hp.funds || ''
      formData.horizontalProject.teamMembers = hp.teamMembers ? hp.teamMembers.split(',').filter(s => s) : ['']
    }

    // 回填创新创业项目
    if (d.innovationProject) {
      hasInnovationProject.value = true
      const ip = d.innovationProject
      formData.innovationProject.status = ip.status || ''
      formData.innovationProject.level = ip.level || ''
      formData.innovationProject.projectName = ip.projectName || ''
      formData.innovationProject.startDate = ip.startDate || ''
      formData.innovationProject.completion = ip.completion || ''
      formData.innovationProject.leaderStudent = ip.leaderStudent || ''
      formData.innovationProject.funds = ip.funds || ''
      formData.innovationProject.paperInfo = ip.paperInfo || ''
      formData.innovationProject.otherStudents = ip.otherStudents ? ip.otherStudents.split(',').filter(s => s) : ['']
      formData.innovationProject.otherTeachers = ip.otherTeachers ? ip.otherTeachers.split(',').filter(s => s) : ['']
    }

    ElMessage.success('已加载被退回的提交数据，请修改后重新提交')
  } catch (error) {
    console.error('加载退回数据失败', error)
    ElMessage.error('加载退回数据失败')
  }
})
</script>

<style scoped lang="scss">
.info-fill-container {
  max-width: 1280px;
  margin: 0 auto;
  padding-bottom: 80px;

  .page-title {
    font-family: var(--font-heading);
    font-size: 24px;
    font-weight: 700;
    color: var(--color-text);
    margin-bottom: 28px;
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

  /* 1. Overview Card */
  .overview-card {
    border-radius: 4px; /* Sharper */
    border: 1px solid var(--color-border);
    box-shadow: none; /* Flat */
    background: #fff;
    margin-bottom: 28px;

    :deep(.el-card__header) {
      background: #F8FAFC;
      border-bottom: 1px solid var(--color-border);
      padding: 20px 24px;
      
      span {
        font-family: var(--font-heading);
        font-weight: 600;
        color: var(--color-text);
        font-size: 15px;
      }
    }
  }

  .switch-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;
    padding: 8px;

    :deep(.el-form-item) {
      margin-bottom: 0;
      background: #FFFFFF;
      padding: 14px 16px;
      border-radius: 4px;
      border: 1px solid var(--color-border);
      transition: all 0.2s ease;
      cursor: pointer;
      
      &:hover {
        background: #EEF2FF; /* Indigo 50 */
        border-color: #C7D2FE; /* Indigo 200 */
      }
      
      .el-form-item__label {
        font-weight: 500;
        color: var(--color-text);
        font-family: var(--font-body);
        font-size: 14px;
      }
    }
  }

  /* 2. Dynamic Forms */
  .form-card {
    border-radius: 4px; /* Sharper */
    border: 1px solid var(--color-border);
    box-shadow: none; /* Flat */
    margin-bottom: 24px;
    background: #fff;
    transition: all 0.2s ease;
    
    &:hover {
      box-shadow: var(--shadow-sm);
      border-color: var(--color-primary-light);
    }
    
    :deep(.el-card__header) {
      background: transparent;
      border-bottom: 1px solid var(--color-border);
      padding: 20px 24px;
      font-weight: 600;
      font-size: 16px;
      font-family: var(--font-heading);
      color: var(--color-text);
    }
    
    :deep(.el-card__body) {
      padding: 24px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .header-upload {
        display: flex;
        align-items: center;
      }
    }

    @media (max-width: 640px) {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
      
      .header-right {
        width: 100%;
        justify-content: space-between;
      }
    }
  }

  .card-header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span {
      font-weight: 600;
      color: var(--color-text);
    }
  }

  .dynamic-row {
    position: relative;
    background: #FAFAFA; /* Zinc 50 */
    border-radius: 4px;
    padding: 24px;
    margin-bottom: 16px;
    border: 1px solid var(--color-border);
    transition: border-color 0.2s;
    
    &:hover {
      border-color: #CBD5E1;
    }
    
    &:last-child {
      margin-bottom: 0;
    }
  }

  .row-action {
    display: flex;
    align-items: flex-end; 
    padding-bottom: 18px; 
  }

  /* Input Styling Override (Pro Max - Focus Ring) */
  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 4px;  
    box-shadow: 0 0 0 1px var(--color-border) inset;
    background-color: #fff;
    padding: 4px 12px; 
    transition: all 0.2s ease;
    
    &:hover {
      box-shadow: 0 0 0 1px #94A3B8 inset; /* Slate 400 */
    }
    
    &.is-focus, &:focus-within {
      box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 3px rgba(99, 102, 241, 0.15) !important; /* Indigo Ring */
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--color-text);
    margin-bottom: 6px;
    font-family: var(--font-body);
    font-size: 14px;
  }

  /* Button Refinement */
  :deep(.el-button--primary:not(.is-plain):not(.is-link):not(.is-text)) {
    background: var(--color-cta);
    border-color: var(--color-cta);
    border-radius: 4px;
    font-weight: 500;
    font-family: var(--font-body);
    box-shadow: none;
    padding: 10px 24px;
    
    &:hover {
      background: var(--color-cta-hover);
      border-color: var(--color-cta-hover);
      transform: translateY(-1px);
      box-shadow: var(--shadow-md);
    }
  }

  /* Excel Buttons — 极简·水墨 Style */
  :deep(.excel-link-btn) {
    background: transparent !important;
    border: 1px solid #E4E4E7 !important;
    color: #18181B !important;
    border-radius: 4px !important;
    font-weight: 500 !important;
    font-size: 13px !important;
    padding: 7px 14px !important;
    font-family: var(--font-body) !important;
    box-shadow: none !important;
    transition: all 0.2s ease !important;
    
    &:hover {
      border-color: #18181B !important;
      background: rgba(24, 24, 27, 0.04) !important;
    }

    span {
      color: #18181B !important;
    }
  }

  :deep(.excel-upload-btn) {
    background: #18181B !important;
    border: 1px solid #18181B !important;
    color: #fff !important;
    border-radius: 4px !important;
    font-weight: 500 !important;
    font-size: 13px !important;
    padding: 7px 14px !important;
    font-family: var(--font-body) !important;
    box-shadow: none !important;
    transition: all 0.2s ease !important;
    
    &:hover {
      background: #27272A !important;
      border-color: #27272A !important;
      transform: translateY(-1px);
    }

    span {
      color: #fff !important;
    }

    .el-icon {
      color: #fff !important;
    }
  }

  :deep(.el-button--default) {
    border-radius: 4px;
    border-color: var(--color-border);
    color: var(--color-text);
    font-family: var(--font-body);
    
    &:hover {
      color: var(--color-primary);
      border-color: var(--color-primary);
      background: #EEF2FF;
    }
  }
  
  :deep(.el-button--danger.is-circle) {
      background: #FFF1F2;
      border-color: #FECDD3;
      color: #E11D48;
      
      &:hover {
          background: #E11D48;
          border-color: #E11D48;
          color: #fff;
      }
  }

  /* Sticky Footer for Mobile */
  .form-actions {
    margin-top: 32px;
    display: flex;
    gap: 16px;
    justify-content: flex-end;
    padding-top: 24px;
    border-top: 1px solid var(--color-border);
    
    &.sticky-footer {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      background: rgba(255,255,255,0.95);
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      padding: 16px 24px;
      box-shadow: 0 -1px 0 0 var(--color-border);
      z-index: 99;
      margin-top: 0;
      border-top: 1px solid var(--color-border);
    }
  }

  /* Switch Styling */
  :deep(.el-switch.is-checked .el-switch__core) {
    background-color: var(--color-cta);
    border-color: var(--color-cta);
  }

  /* Responsive adjustment */
  &.mobile-form {
    :deep(.el-form-item) {
      margin-bottom: 24px;
    }
  }
}


</style>

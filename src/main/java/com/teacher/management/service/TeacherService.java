package com.teacher.management.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teacher.management.dto.*;
import com.teacher.management.dto.excel.*;
import com.teacher.management.dto.excel.export.*;
import com.teacher.management.entity.*;
import com.teacher.management.handler.TemplateStyleHandler;
import com.teacher.management.mapper.*;
import com.teacher.management.vo.AchievementVO;
import com.teacher.management.vo.AchievementGroupVO;
import com.teacher.management.vo.SubmissionDetailVO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    @Autowired
    private SubmissionMapper submissionMapper;
    @Autowired
    private IpRecordMapper ipRecordMapper;
    @Autowired
    private CompetitionRecordMapper competitionRecordMapper;
    @Autowired
    private TrainingRecordMapper trainingRecordMapper;
    @Autowired
    private ReportRecordMapper reportRecordMapper;
    @Autowired
    private TextbookRecordMapper textbookRecordMapper;
    @Autowired
    private AwardRecordMapper awardRecordMapper;
    @Autowired
    private PaperRecordMapper paperRecordMapper;
    @Autowired
    private VerticalProjectMapper verticalProjectMapper;
    @Autowired
    private HorizontalProjectMapper horizontalProjectMapper;
    @Autowired
    private InnovationProjectMapper innovationProjectMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private com.teacher.management.mapper.DepartmentMapper departmentMapper;
    @Autowired
    private CollectionTaskMapper collectionTaskMapper;

    /**
     * 教师提交信息（任务驱动模式）
     */
    @Transactional
    public Long submitInfo(Long userId, TeacherSubmitDTO dto) {
        Long taskId = dto.getTaskId();

        // 1. taskId 必填校验
        if (taskId == null) {
            throw new RuntimeException("请通过采集任务入口提交信息");
        }

        // 2. 先查找该用户在此任务下是否已有提交记录（判断是否为驳回后重新提交）
        QueryWrapper<Submission> subQuery = new QueryWrapper<>();
        subQuery.eq("user_id", userId);
        subQuery.eq("task_id", taskId);
        Submission existing = submissionMapper.selectOne(subQuery);

        boolean isRejectedResubmit = (existing != null
                && (existing.getStatus() == Submission.STATUS_REJECTED
                        || existing.getStatus() == Submission.STATUS_FINAL_REJECTED));

        // 3. 统一校验任务状态和截止时间，任务截止后不再允许重新提交
        CollectionTask task = collectionTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在或未开放填报");
        }
        LocalDateTime now = LocalDateTime.now();
        if (task.getStartTime() != null && now.isBefore(task.getStartTime())) {
            throw new RuntimeException("任务尚未开始，无法提交");
        }
        if (task.getEndTime() != null && now.isAfter(task.getEndTime())) {
            throw new RuntimeException("任务已截止，无法提交");
        }
        if (!isRejectedResubmit && task.getStatus() != 1) {
            throw new RuntimeException("任务不存在或未开放填报");
        }

        // 4. 处理已有提交记录
        if (existing != null) {
            if (!isRejectedResubmit) {
                // 如果不是已被驳回的状态，则不允许重复提交
                throw new RuntimeException("该任务您已提交过，且当前状态不允许重复提交");
            } else {
                // 如果是被驳回的任务，我们允许重新提交，先删除该提交下所有的级联数据
                Long existingSubId = existing.getId();
                ipRecordMapper.delete(new QueryWrapper<IpRecord>().eq("submission_id", existingSubId));
                competitionRecordMapper
                        .delete(new QueryWrapper<CompetitionRecord>().eq("submission_id", existingSubId));
                trainingRecordMapper.delete(new QueryWrapper<TrainingRecord>().eq("submission_id", existingSubId));
                reportRecordMapper.delete(new QueryWrapper<ReportRecord>().eq("submission_id", existingSubId));
                textbookRecordMapper.delete(new QueryWrapper<TextbookRecord>().eq("submission_id", existingSubId));
                awardRecordMapper.delete(new QueryWrapper<AwardRecord>().eq("submission_id", existingSubId));
                paperRecordMapper.delete(new QueryWrapper<PaperRecord>().eq("submission_id", existingSubId));
                verticalProjectMapper.delete(new QueryWrapper<VerticalProject>().eq("submission_id", existingSubId));
                horizontalProjectMapper
                        .delete(new QueryWrapper<HorizontalProject>().eq("submission_id", existingSubId));
                innovationProjectMapper
                        .delete(new QueryWrapper<InnovationProject>().eq("submission_id", existingSubId));

                // 删除原有的提交记录本身，下面会重新创建
                submissionMapper.deleteById(existingSubId);
            }
        }

        // 3. 自动填充 submitMonth
        String month = dto.getSubmitMonth();
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // 4. 插入新的提交记录
        // 判断提交者是否为部门主任(roleId=3)，若是则跳过部门初审，直接进入待终审
        User submitter = userMapper.selectById(userId);
        int initialStatus = Submission.STATUS_PENDING; // 默认：待部门初审
        if (submitter != null && submitter.getRoleId() != null && submitter.getRoleId().equals(3L)) {
            initialStatus = Submission.STATUS_DEPT_APPROVED; // 部门主任自己提交，跳过部门初审
        }

        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setTaskId(taskId);
        submission.setSubmitMonth(month);
        submission.setStatus(initialStatus);
        submission.setCreateTime(LocalDateTime.now());
        submissionMapper.insert(submission);
        Long submissionId = submission.getId();

        // 4. 保存知识产权列表
        if (dto.getIpList() != null && !dto.getIpList().isEmpty()) {
            for (IpDTO ip : dto.getIpList()) {
                IpRecord record = new IpRecord();
                record.setSubmissionId(submissionId);
                record.setName(ip.getName());
                record.setType(ip.getType());
                record.setObtainDate(ip.getDate());
                record.setRank(ip.getRank());
                if (ip.getOtherParticipants() != null) {
                    record.setOtherParticipants(String.join(",", ip.getOtherParticipants()));
                }
                ipRecordMapper.insert(record);
            }
        }

        // 5. 保存竞赛
        if (dto.getCompetition() != null) {
            CompetitionDTO c = dto.getCompetition();
            CompetitionRecord record = new CompetitionRecord();
            record.setSubmissionId(submissionId);
            record.setCategory(c.getCategory());
            record.setName(c.getName());
            record.setOrganizer(c.getOrganizer());
            record.setAwardDate(c.getAwardDate());
            record.setCertNo(c.getCertNo());
            record.setCertName(c.getCertName());
            record.setAwardLevel(c.getAwardLevel());
            record.setAwardGrade(c.getAwardGrade());
            if (c.getStudents() != null) {
                record.setStudents(String.join(",", c.getStudents()));
            }
            if (c.getAdvisorTeachers() != null) {
                record.setAdvisorTeachers(String.join(",", c.getAdvisorTeachers()));
            }
            competitionRecordMapper.insert(record);
        }

        // 6. 保存培训
        if (dto.getTraining() != null) {
            TrainingDTO t = dto.getTraining();
            TrainingRecord record = new TrainingRecord();
            record.setSubmissionId(submissionId);
            record.setType(t.getType());
            record.setName(t.getName());
            record.setForm(t.getForm());
            record.setHours(t.getHours());
            record.setOrganizer(t.getOrganizer());
            record.setStartDate(t.getStartDate());
            record.setEndDate(t.getEndDate());
            trainingRecordMapper.insert(record);
        }

        // 7. 保存咨询报告
        if (dto.getReport() != null) {
            ReportDTO r = dto.getReport();
            ReportRecord record = new ReportRecord();
            record.setSubmissionId(submissionId);
            record.setName(r.getName());
            record.setLevel(r.getLevel());
            record.setAdoptDate(r.getDate());
            record.setRank(r.getRank());
            if (r.getOthers() != null) {
                record.setOthers(String.join(",", r.getOthers()));
            }
            reportRecordMapper.insert(record);
        }

        // 8. 保存出版著作
        if (dto.getBook() != null) {
            BookDTO bk = dto.getBook();
            TextbookRecord record = new TextbookRecord();
            record.setSubmissionId(submissionId);
            record.setName(bk.getName());
            record.setPublisher(bk.getPublisher());
            record.setPublishDate(bk.getDate());
            record.setTextbookLevel(bk.getLevel());
            record.setRank(bk.getRank());
            record.setSelectionDate(bk.getSelectionDate());
            textbookRecordMapper.insert(record);
        }

        // 9. 保存奖项
        if (dto.getAward() != null) {
            AwardDTO a = dto.getAward();
            AwardRecord record = new AwardRecord();
            record.setSubmissionId(submissionId);
            record.setName(a.getName());
            record.setType(a.getType());
            record.setLevel(a.getLevel());
            record.setGrade(a.getGrade());
            record.setRank(a.getRank());
            record.setOrgRank(a.getOrgRank());
            record.setAwardDate(a.getDate());
            record.setCertNo(a.getCertNo());
            awardRecordMapper.insert(record);
        }

        // 10. 保存论文
        if (dto.getPaperList() != null && !dto.getPaperList().isEmpty()) {
            for (PaperDTO pd : dto.getPaperList()) {
                PaperRecord p = new PaperRecord();
                p.setSubmissionId(submissionId);
                p.setPaperType(pd.getPaperType());
                p.setPaperName(pd.getPaperName());
                p.setAuthorType(pd.getAuthorType());
                if (pd.getOtherAuthors() != null) {
                    p.setOtherAuthors(String.join(",", pd.getOtherAuthors()));
                }
                p.setJournalName(pd.getJournalName());
                p.setIndexCategory(pd.getIndexCategory());
                p.setPublishDate(pd.getPublishDate());
                paperRecordMapper.insert(p);
            }
        }

        // 11. 保存纵向项目
        if (dto.getVerticalProject() != null) {
            ResearchProjectDTO vp = dto.getVerticalProject();
            VerticalProject record = new VerticalProject();
            record.setSubmissionId(submissionId);
            record.setResearchType(vp.getResearchType());
            record.setProjectName(vp.getProjectName());
            record.setFundSource(vp.getFundSource());
            record.setLevel(vp.getLevel());
            if (vp.getTeamMembers() != null) {
                record.setTeamMembers(String.join(",", vp.getTeamMembers()));
            }
            record.setSetupDate(vp.getSetupDate());
            record.setSetupNo(vp.getSetupNo());
            record.setUpdateStatus(vp.getUpdateStatus());
            record.setAcceptDate(vp.getAcceptDate());
            record.setFunds(vp.getFunds());
            verticalProjectMapper.insert(record);
        }

        // 12. 保存横向项目
        if (dto.getHorizontalProject() != null) {
            ResearchProjectDTO hp = dto.getHorizontalProject();
            HorizontalProject record = new HorizontalProject();
            record.setSubmissionId(submissionId);
            record.setResearchType(hp.getResearchType());
            record.setProjectName(hp.getProjectName());
            record.setFundSource(hp.getFundSource());
            record.setLevel(hp.getLevel());
            if (hp.getTeamMembers() != null) {
                record.setTeamMembers(String.join(",", hp.getTeamMembers()));
            }
            record.setSetupDate(hp.getSetupDate());
            record.setSetupNo(hp.getSetupNo());
            record.setUpdateStatus(hp.getUpdateStatus());
            record.setAcceptDate(hp.getAcceptDate());
            record.setFunds(hp.getFunds());
            horizontalProjectMapper.insert(record);
        }

        // 13. 保存创新创业项目
        if (dto.getInnovationProject() != null) {
            InnovationProjectDTO ip = dto.getInnovationProject();
            InnovationProject record = new InnovationProject();
            record.setSubmissionId(submissionId);
            record.setStatus(ip.getStatus());
            record.setLevel(ip.getLevel());
            record.setProjectName(ip.getProjectName());
            record.setStartDate(ip.getStartDate());
            record.setCompletion(ip.getCompletion());
            record.setLeaderStudent(ip.getLeaderStudent());
            if (ip.getOtherStudents() != null) {
                record.setOtherStudents(String.join(",", ip.getOtherStudents()));
            }
            record.setFunds(ip.getFunds());
            record.setPaperInfo(ip.getPaperInfo());
            if (ip.getOtherTeachers() != null) {
                record.setOtherTeachers(String.join(",", ip.getOtherTeachers()));
            }
            innovationProjectMapper.insert(record);
        }

        return submissionId;
    }

    /**
     * 根据 submissionId 删除所有附表记录
     */
    private void deleteDetailsBySubmissionId(Long submissionId) {
        ipRecordMapper.delete(new QueryWrapper<IpRecord>().eq("submission_id", submissionId));
        competitionRecordMapper.delete(new QueryWrapper<CompetitionRecord>().eq("submission_id", submissionId));
        trainingRecordMapper.delete(new QueryWrapper<TrainingRecord>().eq("submission_id", submissionId));
        reportRecordMapper.delete(new QueryWrapper<ReportRecord>().eq("submission_id", submissionId));
        textbookRecordMapper.delete(new QueryWrapper<TextbookRecord>().eq("submission_id", submissionId));
        awardRecordMapper.delete(new QueryWrapper<AwardRecord>().eq("submission_id", submissionId));
        paperRecordMapper.delete(new QueryWrapper<PaperRecord>().eq("submission_id", submissionId));
        verticalProjectMapper.delete(new QueryWrapper<VerticalProject>().eq("submission_id", submissionId));
        horizontalProjectMapper.delete(new QueryWrapper<HorizontalProject>().eq("submission_id", submissionId));
        innovationProjectMapper.delete(new QueryWrapper<InnovationProject>().eq("submission_id", submissionId));
    }

    /**
     * 查询某次提交的详情（聚合所有子表数据）
     */
    public SubmissionDetailVO getSubmissionDetail(Long submissionId, Long userId) {
        // 1. 查询提交记录并验证归属
        Submission sub = submissionMapper.selectById(submissionId);
        if (sub == null || !sub.getUserId().equals(userId)) {
            return null;
        }

        SubmissionDetailVO vo = new SubmissionDetailVO();
        vo.setId(sub.getId());
        vo.setSubmitMonth(sub.getSubmitMonth());
        vo.setStatus(sub.getStatus());
        vo.setCreateTime(sub.getCreateTime());

        // 填充任务名称
        if (sub.getTaskId() != null) {
            CollectionTask task = collectionTaskMapper.selectById(sub.getTaskId());
            if (task != null) {
                vo.setTaskName(task.getTaskName());
            } else {
                vo.setTaskName("任务已删除");
            }
        } else {
            vo.setTaskName("历史提交");
        }

        // 2. 查询各子表数据

        QueryWrapper<IpRecord> iq = new QueryWrapper<>();
        iq.eq("submission_id", submissionId);
        vo.setIpList(ipRecordMapper.selectList(iq));

        QueryWrapper<CompetitionRecord> cq = new QueryWrapper<>();
        cq.eq("submission_id", submissionId);
        vo.setCompetition(competitionRecordMapper.selectOne(cq));

        QueryWrapper<TrainingRecord> tq = new QueryWrapper<>();
        tq.eq("submission_id", submissionId);
        vo.setTraining(trainingRecordMapper.selectOne(tq));

        QueryWrapper<ReportRecord> rq = new QueryWrapper<>();
        rq.eq("submission_id", submissionId);
        vo.setReport(reportRecordMapper.selectOne(rq));

        QueryWrapper<TextbookRecord> tbq = new QueryWrapper<>();
        tbq.eq("submission_id", submissionId);
        vo.setBook(textbookRecordMapper.selectOne(tbq));

        QueryWrapper<AwardRecord> aq = new QueryWrapper<>();
        aq.eq("submission_id", submissionId);
        vo.setAward(awardRecordMapper.selectOne(aq));

        QueryWrapper<PaperRecord> pq = new QueryWrapper<>();
        pq.eq("submission_id", submissionId);
        vo.setPaperList(paperRecordMapper.selectList(pq));

        QueryWrapper<VerticalProject> vpq = new QueryWrapper<>();
        vpq.eq("submission_id", submissionId);
        vo.setVerticalProject(verticalProjectMapper.selectOne(vpq));

        QueryWrapper<HorizontalProject> hpq = new QueryWrapper<>();
        hpq.eq("submission_id", submissionId);
        vo.setHorizontalProject(horizontalProjectMapper.selectOne(hpq));

        QueryWrapper<InnovationProject> ipq = new QueryWrapper<>();
        ipq.eq("submission_id", submissionId);
        vo.setInnovationProject(innovationProjectMapper.selectOne(ipq));

        return vo;
    }

    /**
     * 管理员查询提交详情（不限用户归属）
     */
    public SubmissionDetailVO getSubmissionDetailForAdmin(Long submissionId) {
        Submission sub = submissionMapper.selectById(submissionId);
        if (sub == null) {
            return null;
        }

        SubmissionDetailVO vo = new SubmissionDetailVO();
        vo.setId(sub.getId());
        vo.setSubmitMonth(sub.getSubmitMonth());
        vo.setStatus(sub.getStatus());
        vo.setAuditRemark(sub.getAuditRemark());
        vo.setCreateTime(sub.getCreateTime());

        // 填充任务名称
        if (sub.getTaskId() != null) {
            CollectionTask task = collectionTaskMapper.selectById(sub.getTaskId());
            if (task != null) {
                vo.setTaskName(task.getTaskName());
            } else {
                vo.setTaskName("任务已删除");
            }
        } else {
            vo.setTaskName("历史提交");
        }

        // 查询教师信息
        com.teacher.management.entity.User user = userMapper.selectById(sub.getUserId());
        if (user != null) {
            vo.setTeacherName(user.getRealName());
            vo.setEmployeeNo(user.getEmployeeNo());
            if (user.getDeptId() != null) {
                com.teacher.management.entity.Department dept = departmentMapper.selectById(user.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getName());
                }
            }
        }

        // 查询各子表数据

        QueryWrapper<IpRecord> iq = new QueryWrapper<>();
        iq.eq("submission_id", submissionId);
        vo.setIpList(ipRecordMapper.selectList(iq));

        QueryWrapper<CompetitionRecord> cq = new QueryWrapper<>();
        cq.eq("submission_id", submissionId);
        vo.setCompetition(competitionRecordMapper.selectOne(cq));

        QueryWrapper<TrainingRecord> tq = new QueryWrapper<>();
        tq.eq("submission_id", submissionId);
        vo.setTraining(trainingRecordMapper.selectOne(tq));

        QueryWrapper<ReportRecord> rq = new QueryWrapper<>();
        rq.eq("submission_id", submissionId);
        vo.setReport(reportRecordMapper.selectOne(rq));

        QueryWrapper<TextbookRecord> tbq = new QueryWrapper<>();
        tbq.eq("submission_id", submissionId);
        vo.setBook(textbookRecordMapper.selectOne(tbq));

        QueryWrapper<AwardRecord> aq = new QueryWrapper<>();
        aq.eq("submission_id", submissionId);
        vo.setAward(awardRecordMapper.selectOne(aq));

        QueryWrapper<PaperRecord> pq = new QueryWrapper<>();
        pq.eq("submission_id", submissionId);
        vo.setPaperList(paperRecordMapper.selectList(pq));

        QueryWrapper<VerticalProject> vpq = new QueryWrapper<>();
        vpq.eq("submission_id", submissionId);
        vo.setVerticalProject(verticalProjectMapper.selectOne(vpq));

        QueryWrapper<HorizontalProject> hpq = new QueryWrapper<>();
        hpq.eq("submission_id", submissionId);
        vo.setHorizontalProject(horizontalProjectMapper.selectOne(hpq));

        QueryWrapper<InnovationProject> ipq = new QueryWrapper<>();
        ipq.eq("submission_id", submissionId);
        vo.setInnovationProject(innovationProjectMapper.selectOne(ipq));

        return vo;
    }

    /**
     * 获取用户的全部成果聚合数据，按审核状态分组
     */
    public AchievementGroupVO getAchievements(Long userId) {
        // 查出该用户所有提交记录（含 id 和 status）
        QueryWrapper<Submission> sq = new QueryWrapper<>();
        sq.eq("user_id", userId).select("id", "status");
        List<Submission> subs = submissionMapper.selectList(sq);

        AchievementGroupVO group = new AchievementGroupVO();

        // 按 status 分组
        Map<Integer, List<Long>> statusIdMap = new java.util.HashMap<>();
        statusIdMap.put(0, new ArrayList<>()); // 审核中
        statusIdMap.put(1, new ArrayList<>()); // 已通过
        statusIdMap.put(2, new ArrayList<>()); // 已退回

        for (Submission sub : subs) {
            int st = sub.getStatus() != null ? sub.getStatus() : 0;
            int groupKey = st;
            if (st == Submission.STATUS_DEPT_APPROVED) {
                groupKey = Submission.STATUS_PENDING;
            } else if (st == Submission.STATUS_FINAL_REJECTED) {
                groupKey = Submission.STATUS_REJECTED;
            }
            statusIdMap.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(sub.getId());
        }

        group.setApproved(buildAchievementVO(statusIdMap.get(1)));
        group.setPending(buildAchievementVO(statusIdMap.get(0)));
        group.setRejected(buildAchievementVO(statusIdMap.get(2)));

        return group;
    }

    /** 根据一组 submissionId 查询所有子表，返回 AchievementVO */
    private AchievementVO buildAchievementVO(List<Long> ids) {
        AchievementVO vo = new AchievementVO();
        if (ids == null || ids.isEmpty()) {
            vo.setPapers(Collections.emptyList());
            vo.setAwards(Collections.emptyList());
            vo.setIps(Collections.emptyList());
            vo.setCompetitions(Collections.emptyList());
            vo.setTrainings(Collections.emptyList());
            vo.setReports(Collections.emptyList());
            vo.setTextbooks(Collections.emptyList());
            vo.setVerticalProjects(Collections.emptyList());
            vo.setHorizontalProjects(Collections.emptyList());
            vo.setInnovationProjects(Collections.emptyList());
            return vo;
        }

        vo.setPapers(paperRecordMapper.selectList(new QueryWrapper<PaperRecord>().in("submission_id", ids)));
        vo.setAwards(awardRecordMapper.selectList(new QueryWrapper<AwardRecord>().in("submission_id", ids)));
        vo.setIps(ipRecordMapper.selectList(new QueryWrapper<IpRecord>().in("submission_id", ids)));
        vo.setCompetitions(
                competitionRecordMapper.selectList(new QueryWrapper<CompetitionRecord>().in("submission_id", ids)));
        vo.setTrainings(trainingRecordMapper.selectList(new QueryWrapper<TrainingRecord>().in("submission_id", ids)));
        vo.setReports(reportRecordMapper.selectList(new QueryWrapper<ReportRecord>().in("submission_id", ids)));
        vo.setTextbooks(textbookRecordMapper.selectList(new QueryWrapper<TextbookRecord>().in("submission_id", ids)));
        vo.setVerticalProjects(
                verticalProjectMapper.selectList(new QueryWrapper<VerticalProject>().in("submission_id", ids)));
        vo.setHorizontalProjects(
                horizontalProjectMapper.selectList(new QueryWrapper<HorizontalProject>().in("submission_id", ids)));
        vo.setInnovationProjects(
                innovationProjectMapper.selectList(new QueryWrapper<InnovationProject>().in("submission_id", ids)));

        return vo;
    }

    /**
     * 统计用户的累计成果数（所有子表非空记录之和）
     */
    public int countAchievements(Long userId) {
        // 先查出该用户所有提交 ID
        QueryWrapper<Submission> sq = new QueryWrapper<>();
        sq.eq("user_id", userId).select("id");
        List<Submission> subs = submissionMapper.selectList(sq);
        if (subs.isEmpty())
            return 0;

        List<Long> ids = subs.stream().map(Submission::getId).collect(java.util.stream.Collectors.toList());
        int count = 0;

        count += ipRecordMapper.selectCount(new QueryWrapper<IpRecord>().in("submission_id", ids)).intValue();
        count += competitionRecordMapper.selectCount(new QueryWrapper<CompetitionRecord>().in("submission_id", ids))
                .intValue();
        count += trainingRecordMapper.selectCount(new QueryWrapper<TrainingRecord>().in("submission_id", ids))
                .intValue();
        count += reportRecordMapper.selectCount(new QueryWrapper<ReportRecord>().in("submission_id", ids)).intValue();
        count += textbookRecordMapper.selectCount(new QueryWrapper<TextbookRecord>().in("submission_id", ids))
                .intValue();
        count += awardRecordMapper.selectCount(new QueryWrapper<AwardRecord>().in("submission_id", ids)).intValue();
        count += paperRecordMapper.selectCount(new QueryWrapper<PaperRecord>().in("submission_id", ids)).intValue();
        count += verticalProjectMapper.selectCount(new QueryWrapper<VerticalProject>().in("submission_id", ids))
                .intValue();
        count += horizontalProjectMapper.selectCount(new QueryWrapper<HorizontalProject>().in("submission_id", ids))
                .intValue();
        count += innovationProjectMapper.selectCount(new QueryWrapper<InnovationProject>().in("submission_id", ids))
                .intValue();

        return count;
    }

    /**
     * 统计系统中所有成果总数（管理员仪表盘用）
     */
    public int countAllAchievements() {
        int count = 0;
        count += ipRecordMapper.selectCount(null).intValue();
        count += competitionRecordMapper.selectCount(null).intValue();
        count += trainingRecordMapper.selectCount(null).intValue();
        count += reportRecordMapper.selectCount(null).intValue();
        count += textbookRecordMapper.selectCount(null).intValue();
        count += awardRecordMapper.selectCount(null).intValue();
        count += paperRecordMapper.selectCount(null).intValue();
        count += verticalProjectMapper.selectCount(null).intValue();
        count += horizontalProjectMapper.selectCount(null).intValue();
        count += innovationProjectMapper.selectCount(null).intValue();
        return count;
    }

    /**
     * 下载 Excel 标准模板（10 个 Sheet，双行表头 + 空白填写区）
     * 使用编程式表头保留提示行（ExcelDTO 中已去除第二行提示）
     */
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("教资填报标准模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        // 表头基础样式（蓝色背景、加粗微软雅黑）—— 第二行会被 TemplateStyleHandler 覆盖为弱化样式
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        headStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        WriteFont headFont = new WriteFont();
        headFont.setFontHeightInPoints((short) 12);
        headFont.setBold(true);
        headFont.setFontName("微软雅黑");
        headStyle.setWriteFont(headFont);

        // 内容行样式（微软雅黑、四周框线）
        WriteCellStyle contentStyle = new WriteCellStyle();
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentStyle.setBorderBottom(BorderStyle.THIN);
        contentStyle.setBorderLeft(BorderStyle.THIN);
        contentStyle.setBorderRight(BorderStyle.THIN);
        contentStyle.setBorderTop(BorderStyle.THIN);
        WriteFont contentFont = new WriteFont();
        contentFont.setFontHeightInPoints((short) 11);
        contentFont.setFontName("微软雅黑");
        contentStyle.setWriteFont(contentFont);

        HorizontalCellStyleStrategy styleStrategy = new HorizontalCellStyleStrategy(headStyle, contentStyle);

        try (ExcelWriter writer = EasyExcel.write(response.getOutputStream())
                .registerWriteHandler(styleStrategy)
                .registerWriteHandler(new TemplateStyleHandler()).build()) {

            // 知识产权
            writer.write(emptyDataRows(5, 1), EasyExcel.writerSheet(0, "知识产权")
                    .head(twoRowHead(
                            new String[] { "知识产权名称", "请填写完整名称" },
                            new String[] { "类型", "发明专利/实用新型/软件著作权" },
                            new String[] { "取得日期", "格式：2025-06" },
                            new String[] { "本人排名", "填写数字，如 1" },
                            new String[] { "其他参与人", "多人用逗号分隔，如：张三,李四" }))
                    .build());

            // 指导竞赛
            writer.write(emptyDataRows(10, 1), EasyExcel.writerSheet(1, "指导竞赛")
                    .head(twoRowHead(
                            new String[] { "竞赛类别", "A类/B类/C类/D类" },
                            new String[] { "竞赛名称", "填写竞赛全称" },
                            new String[] { "主办单位", "以获奖证书为准" },
                            new String[] { "获奖时间", "格式：2025-06-15" },
                            new String[] { "证书编号", "获奖证书上的编号" },
                            new String[] { "证书名称", "获奖证书完整名称" },
                            new String[] { "奖项级别", "国家级/省级" },
                            new String[] { "奖项等级", "特等奖/一等奖/二等奖/三等奖" },
                            new String[] { "参赛学生", "多人用逗号分隔" },
                            new String[] { "指导教师", "多人用逗号分隔" }))
                    .build());

            // 教师培训
            writer.write(emptyDataRows(7, 1), EasyExcel.writerSheet(2, "教师培训")
                    .head(twoRowHead(
                            new String[] { "培训类型", "如：骨干教师培训" },
                            new String[] { "培训名称", "填写培训项目全称" },
                            new String[] { "培训形式", "线上/线下/混合" },
                            new String[] { "培训学时", "填写数字" },
                            new String[] { "组织单位", "主办方全称" },
                            new String[] { "开始日期", "格式：2025-06-01" },
                            new String[] { "结束日期", "格式：2025-06-30" }))
                    .build());

            // 咨询报告
            writer.write(emptyDataRows(5, 1), EasyExcel.writerSheet(3, "咨询报告")
                    .head(twoRowHead(
                            new String[] { "报告名称", "填写报告完整名称" },
                            new String[] { "采纳级别", "国家级/省部级/地厅级/县处级" },
                            new String[] { "采纳日期", "格式：2025-06" },
                            new String[] { "本人排名", "填写数字，如 1" },
                            new String[] { "其他参与人", "多人用逗号分隔，如：张三,李四" }))
                    .build());

            // 出版著作
            writer.write(emptyDataRows(6, 1), EasyExcel.writerSheet(4, "出版著作")
                    .head(twoRowHead(
                            new String[] { "著作名称", "填写著作完整名称" },
                            new String[] { "出版社", "出版社全称" },
                            new String[] { "出版日期", "格式：2025-06" },
                            new String[] { "教材等级", "国家级/省级/校级/未入选" },
                            new String[] { "本人排名", "填写数字，如 1" },
                            new String[] { "入选日期", "格式：2025-06" }))
                    .build());

            // 获奖
            writer.write(emptyDataRows(8, 1), EasyExcel.writerSheet(5, "获奖")
                    .head(twoRowHead(
                            new String[] { "获奖名称", "填写获奖成果完整名称" },
                            new String[] { "获奖类型", "教学成果奖/科研成果奖/其他" },
                            new String[] { "获奖级别", "国家级/省级/市级/校级" },
                            new String[] { "获奖等级", "特等奖/一等奖/二等奖/三等奖" },
                            new String[] { "本人排名", "填写数字，如 1" },
                            new String[] { "集体排名", "填写数字，如 1" },
                            new String[] { "获奖日期", "格式：2025-06-15" },
                            new String[] { "证书编号", "获奖证书上的编号" }))
                    .build());

            // 发表论文
            writer.write(emptyDataRows(7, 1), EasyExcel.writerSheet(6, "发表论文")
                    .head(twoRowHead(
                            new String[] { "论文类型", "学术论文/会议论文/综述/其他" },
                            new String[] { "论文名称", "填写论文完整标题" },
                            new String[] { "作者类型", "第一作者/通讯作者/其他" },
                            new String[] { "其他作者", "多人用逗号分隔，如：张三,李四" },
                            new String[] { "期刊名称", "填写发表期刊全称" },
                            new String[] { "收录类别", "SCI/SSCI/EI/CSCD/北核/南核/普刊" },
                            new String[] { "发表日期", "格式：2025-06" }))
                    .build());

            // 纵向项目
            writer.write(emptyDataRows(10, 1), EasyExcel.writerSheet(7, "纵向项目")
                    .head(twoRowHead(
                            new String[] { "教研/科研", "教研 或 科研" },
                            new String[] { "项目名称", "仅填本时间段立项或结项的项目" },
                            new String[] { "基金来源", "项目基金来源渠道" },
                            new String[] { "项目级别", "国家级/省部级重点/省部级一般/市厅级/院级" },
                            new String[] { "团队成员", "第一个为负责人，逗号分隔" },
                            new String[] { "立项时间", "格式：2025-06-15" },
                            new String[] { "立项编号", "立项编号或文号" },
                            new String[] { "项目状态", "立项 或 结项" },
                            new String[] { "验收时间", "格式：2025-06-15" },
                            new String[] { "项目经费（元）", "填写金额数字" }))
                    .build());

            // 横向项目
            writer.write(emptyDataRows(10, 1), EasyExcel.writerSheet(8, "横向项目")
                    .head(twoRowHead(
                            new String[] { "教研/科研", "教研 或 科研" },
                            new String[] { "项目名称", "仅填本时间段立项或结项的项目" },
                            new String[] { "基金来源", "项目基金来源渠道" },
                            new String[] { "项目级别", "国家级/省部级重点/省部级一般/市厅级/院级" },
                            new String[] { "团队成员", "第一个为负责人，逗号分隔" },
                            new String[] { "立项时间", "格式：2025-06-15" },
                            new String[] { "立项编号", "立项编号或文号" },
                            new String[] { "项目状态", "立项 或 结项" },
                            new String[] { "验收时间", "格式：2025-06-15" },
                            new String[] { "项目经费（元）", "填写金额数字" }))
                    .build());

            // 创新创业项目
            writer.write(emptyDataRows(10, 1), EasyExcel.writerSheet(9, "创新创业项目")
                    .head(twoRowHead(
                            new String[] { "项目状态", "立项 或 结项" },
                            new String[] { "项目级别", "国家级/省级/院级" },
                            new String[] { "项目名称", "填写项目完整名称" },
                            new String[] { "起始时间", "格式：2025-06-15" },
                            new String[] { "是否结题", "已结题/未结题/已放弃" },
                            new String[] { "负责学生", "姓名+学号" },
                            new String[] { "其他学生", "多人用逗号分隔" },
                            new String[] { "项目经费（元）", "填写金额数字" },
                            new String[] { "论文发表情况", "作者、论文题目、期刊、页码" },
                            new String[] { "其他指导教师", "多人用逗号分隔" }))
                    .build());
        }
    }

    /** 构建模板用的双行表头 */
    private List<List<String>> twoRowHead(String[]... columns) {
        List<List<String>> head = new ArrayList<>();
        for (String[] col : columns) {
            head.add(Arrays.asList(col));
        }
        return head;
    }

    /** 生成指定行列数的空白数据行 */
    private List<List<String>> emptyDataRows(int cols, int rows) {
        List<List<String>> data = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            data.add(new ArrayList<>(Collections.nCopies(cols, "")));
        }
        return data;
    }

    /**
     * 导入 Excel 数据，转换为 TeacherSubmitDTO 后调用 submitInfo 落库
     */
    @Transactional
    public Long importExcel(Long userId, Long taskId, MultipartFile file) throws IOException {
        // 1. 分别读取 10 个 Sheet
        List<IpExcelDTO> ipRows = readSheet(file, IpExcelDTO.class, 0);
        ipRows.removeIf(r -> isBlank(r.getName()) || isTemplateExample(r.getName(), "请填写完整名称"));
        List<CompetitionExcelDTO> compRows = readSheet(file, CompetitionExcelDTO.class, 1);
        compRows.removeIf(r -> isBlank(r.getName()) || isTemplateExample(r.getName(), "填写竞赛全称"));
        List<TrainingExcelDTO> trainRows = readSheet(file, TrainingExcelDTO.class, 2);
        trainRows.removeIf(r -> isBlank(r.getName()) || isTemplateExample(r.getName(), "填写培训项目全称"));
        List<ReportExcelDTO> reportRows = readSheet(file, ReportExcelDTO.class, 3);
        reportRows.removeIf(r -> isBlank(r.getName()) || isTemplateExample(r.getName(), "填写报告完整名称"));
        List<BookExcelDTO> bookRows = readSheet(file, BookExcelDTO.class, 4);
        bookRows.removeIf(r -> isBlank(r.getName()) || isTemplateExample(r.getName(), "填写著作完整名称"));
        List<AwardExcelDTO> awardRows = readSheet(file, AwardExcelDTO.class, 5);
        awardRows.removeIf(r -> isBlank(r.getName()) || isTemplateExample(r.getName(), "填写获奖成果完整名称"));
        List<PaperExcelDTO> paperRows = readSheet(file, PaperExcelDTO.class, 6);
        paperRows.removeIf(r -> isBlank(r.getPaperName()) || isTemplateExample(r.getPaperName(), "填写论文完整标题"));
        List<ResearchProjectExcelDTO> vpRows = readSheet(file, ResearchProjectExcelDTO.class, 7);
        vpRows.removeIf(r -> isBlank(r.getProjectName()) || isTemplateExample(r.getProjectName(), "仅填本时间段立项或结项的项目"));
        List<ResearchProjectExcelDTO> hpRows = readSheet(file, ResearchProjectExcelDTO.class, 8);
        hpRows.removeIf(r -> isBlank(r.getProjectName()) || isTemplateExample(r.getProjectName(), "仅填本时间段立项或结项的项目"));
        List<InnovationProjectExcelDTO> innovRows = readSheet(file, InnovationProjectExcelDTO.class, 9);
        innovRows.removeIf(r -> isBlank(r.getProjectName()) || isTemplateExample(r.getProjectName(), "填写项目完整名称"));

        // 2. 组装 TeacherSubmitDTO
        TeacherSubmitDTO dto = new TeacherSubmitDTO();
        dto.setTaskId(taskId);

        // 知识产权（1:N）
        if (ipRows != null && !ipRows.isEmpty()) {
            List<IpDTO> ipList = new ArrayList<>();
            for (IpExcelDTO row : ipRows) {
                IpDTO ip = new IpDTO();
                ip.setName(row.getName());
                ip.setType(row.getType());
                ip.setDate(normalizeExcelDate(row.getDate()));
                ip.setRank(safeParseInt(row.getRank(), 1));
                ip.setOtherParticipants(splitStr(row.getOtherParticipantsStr()));
                ipList.add(ip);
            }
            dto.setIpList(ipList);
        }

        // 指导竞赛（1:1，取第一行）
        if (compRows != null && !compRows.isEmpty()) {
            CompetitionExcelDTO row = compRows.get(0);
            CompetitionDTO c = new CompetitionDTO();
            c.setCategory(row.getCategory());
            c.setName(row.getName());
            c.setOrganizer(row.getOrganizer());
            c.setAwardDate(normalizeExcelDate(row.getAwardDate()));
            c.setCertNo(row.getCertNo());
            c.setCertName(row.getCertName());
            c.setAwardLevel(row.getAwardLevel());
            c.setAwardGrade(row.getAwardGrade());
            c.setStudents(splitStr(row.getStudentsStr()));
            c.setAdvisorTeachers(splitStr(row.getAdvisorTeachersStr()));
            dto.setCompetition(c);
        }

        // 教师培训（1:1，取第一行）
        if (trainRows != null && !trainRows.isEmpty()) {
            TrainingExcelDTO row = trainRows.get(0);
            TrainingDTO t = new TrainingDTO();
            t.setType(row.getType());
            t.setName(row.getName());
            t.setForm(row.getForm());
            t.setHours(safeParseInt(row.getHours(), 0));
            t.setOrganizer(row.getOrganizer());
            t.setStartDate(normalizeExcelDate(row.getStartDate()));
            t.setEndDate(normalizeExcelDate(row.getEndDate()));
            dto.setTraining(t);
        }

        // 咨询报告（1:1，取第一行）
        if (reportRows != null && !reportRows.isEmpty()) {
            ReportExcelDTO row = reportRows.get(0);
            ReportDTO r = new ReportDTO();
            r.setName(row.getName());
            r.setLevel(row.getLevel());
            r.setDate(normalizeExcelDate(row.getDate()));
            r.setRank(safeParseInt(row.getRank(), 1));
            r.setOthers(splitStr(row.getOthersStr()));
            dto.setReport(r);
        }

        // 出版著作（1:1，取第一行）
        if (bookRows != null && !bookRows.isEmpty()) {
            BookExcelDTO row = bookRows.get(0);
            BookDTO b = new BookDTO();
            b.setName(row.getName());
            b.setPublisher(row.getPublisher());
            b.setDate(normalizeExcelDate(row.getDate()));
            b.setLevel(row.getLevel());
            b.setRank(safeParseInt(row.getRank(), 1));
            b.setSelectionDate(normalizeExcelDate(row.getSelectionDate()));
            dto.setBook(b);
        }

        // 获奖（1:1，取第一行）
        if (awardRows != null && !awardRows.isEmpty()) {
            AwardExcelDTO row = awardRows.get(0);
            AwardDTO a = new AwardDTO();
            a.setName(row.getName());
            a.setType(row.getType());
            a.setLevel(row.getLevel());
            a.setGrade(row.getGrade());
            a.setRank(safeParseInt(row.getRank(), 1));
            a.setOrgRank(safeParseInt(row.getOrgRank(), 1));
            a.setDate(normalizeExcelDate(row.getDate()));
            a.setCertNo(row.getCertNo());
            dto.setAward(a);
        }

        // 发表论文（1:N）
        if (paperRows != null && !paperRows.isEmpty()) {
            List<PaperDTO> paperList = new ArrayList<>();
            for (PaperExcelDTO row : paperRows) {
                PaperDTO p = new PaperDTO();
                p.setPaperType(row.getPaperType());
                p.setPaperName(row.getPaperName());
                p.setAuthorType(row.getAuthorType());
                p.setOtherAuthors(splitStr(row.getOtherAuthorsStr()));
                p.setJournalName(row.getJournalName());
                p.setIndexCategory(row.getIndexCategory());
                p.setPublishDate(normalizeExcelDate(row.getPublishDate()));
                paperList.add(p);
            }
            dto.setPaperList(paperList);
        }

        // 纵向项目（1:1，取第一行）
        if (vpRows != null && !vpRows.isEmpty()) {
            dto.setVerticalProject(convertResearchProject(vpRows.get(0)));
        }

        // 横向项目（1:1，取第一行）
        if (hpRows != null && !hpRows.isEmpty()) {
            dto.setHorizontalProject(convertResearchProject(hpRows.get(0)));
        }

        // 创新创业项目（1:1，取第一行）
        if (innovRows != null && !innovRows.isEmpty()) {
            InnovationProjectExcelDTO row = innovRows.get(0);
            InnovationProjectDTO ip = new InnovationProjectDTO();
            ip.setStatus(row.getStatus());
            ip.setLevel(row.getLevel());
            ip.setProjectName(row.getProjectName());
            ip.setStartDate(normalizeExcelDate(row.getStartDate()));
            ip.setCompletion(row.getCompletion());
            ip.setLeaderStudent(row.getLeaderStudent());
            ip.setOtherStudents(splitStr(row.getOtherStudentsStr()));
            ip.setFunds(row.getFunds());
            ip.setPaperInfo(row.getPaperInfo());
            ip.setOtherTeachers(splitStr(row.getOtherTeachersStr()));
            dto.setInnovationProject(ip);
        }

        // 3. 复用 submitInfo 落库
        if (!hasImportedContent(dto)) {
            throw new RuntimeException("未识别到有效导入数据，请确认已覆盖模板示例行或从示例行下一行开始填写");
        }

        return submitInfo(userId, dto);
    }

    /** 逗号分隔字符串 → List，空/null 返回 null */
    /** 判断字符串是否为空（用于过滤模板空白行） */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isTemplateExample(String value, String example) {
        return !isBlank(value) && value.trim().equals(example);
    }

    private <T> List<T> readSheet(MultipartFile file, Class<T> headClass, int sheetNo) throws IOException {
        return EasyExcel.read(file.getInputStream())
                .head(headClass)
                .sheet(sheetNo)
                .headRowNumber(1)
                .doReadSync();
    }

    private Integer safeParseInt(String value, int defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String normalizeExcelDate(String value) {
        if (isBlank(value)) {
            return value;
        }
        String normalized = value.trim();
        if (normalized.contains(" ")) {
            normalized = normalized.substring(0, normalized.indexOf(' '));
        }
        normalized = normalized.replace("/", "-").replace(".", "-");

        String[] parts = normalized.split("-");
        if (parts.length == 3) {
            try {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                return String.format("%04d-%02d-%02d", year, month, day);
            } catch (NumberFormatException ignored) {
                return normalized.length() > 10 ? normalized.substring(0, 10) : normalized;
            }
        }
        if (parts.length == 2) {
            try {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                return String.format("%04d-%02d", year, month);
            } catch (NumberFormatException ignored) {
                return normalized.length() > 7 ? normalized.substring(0, 7) : normalized;
            }
        }
        return normalized.length() > 10 ? normalized.substring(0, 10) : normalized;
    }

    private boolean hasImportedContent(TeacherSubmitDTO dto) {
        return (dto.getIpList() != null && !dto.getIpList().isEmpty())
                || dto.getCompetition() != null
                || dto.getTraining() != null
                || dto.getReport() != null
                || dto.getBook() != null
                || dto.getAward() != null
                || (dto.getPaperList() != null && !dto.getPaperList().isEmpty())
                || dto.getVerticalProject() != null
                || dto.getHorizontalProject() != null
                || dto.getInnovationProject() != null;
    }

    private List<String> splitStr(String str) {
        if (str == null || str.trim().isEmpty())
            return null;
        return Arrays.asList(str.split("[,，]"));
    }

    /** ResearchProjectExcelDTO → ResearchProjectDTO */
    private ResearchProjectDTO convertResearchProject(ResearchProjectExcelDTO row) {
        ResearchProjectDTO rp = new ResearchProjectDTO();
        rp.setResearchType(row.getResearchType());
        rp.setProjectName(row.getProjectName());
        rp.setFundSource(row.getFundSource());
        rp.setLevel(row.getLevel());
        rp.setTeamMembers(splitStr(row.getTeamMembersStr()));
        rp.setSetupDate(normalizeExcelDate(row.getSetupDate()));
        rp.setSetupNo(row.getSetupNo());
        rp.setUpdateStatus(row.getUpdateStatus());
        rp.setAcceptDate(normalizeExcelDate(row.getAcceptDate()));
        rp.setFunds(row.getFunds());
        return rp;
    }

    // ==================== 数据导出 ====================

    /**
     * 导出单条提交详情为 Excel（10 个 Sheet）
     */
    public void exportSingleSubmission(Long submissionId, HttpServletResponse response) throws IOException {
        SubmissionDetailVO detail = getSubmissionDetailForAdmin(submissionId);
        if (detail == null) {
            throw new RuntimeException("提交记录不存在");
        }

        String teacherName = detail.getTeacherName() != null ? detail.getTeacherName() : "未知";
        String month = detail.getSubmitMonth() != null ? detail.getSubmitMonth() : "";
        String rawFileName = teacherName + "_" + month + "_教学科研数据";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        // 使用 POI 直接生成单 Sheet
        org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("教学科研数据");

        // 样式：分类标题
        org.apache.poi.xssf.usermodel.XSSFCellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.PALE_BLUE.getIndex());
        titleStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
        titleStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        titleStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        titleStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        titleStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        titleStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        org.apache.poi.xssf.usermodel.XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setFontName("微软雅黑");
        titleStyle.setFont(titleFont);

        // 样式：列头
        org.apache.poi.xssf.usermodel.XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        org.apache.poi.xssf.usermodel.XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setFontName("微软雅黑");
        headerStyle.setFont(headerFont);

        // 样式：数据行
        org.apache.poi.xssf.usermodel.XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
        dataStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        dataStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        dataStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        dataStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        dataStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        org.apache.poi.xssf.usermodel.XSSFFont dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 11);
        dataFont.setFontName("微软雅黑");
        dataStyle.setFont(dataFont);

        int rowIdx = 0;
        sheet.setDefaultColumnWidth(18);

        // 知识产权
        if (detail.getIpList() != null && !detail.getIpList().isEmpty()) {
            rowIdx = writeSectionTitle(sheet, rowIdx, "【知识产权】", titleStyle);
            rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "知识产权名称", "类型", "取得日期", "本人排名", "其他参与人");
            for (IpRecord r : detail.getIpList()) {
                rowIdx = writeDataRow(sheet, rowIdx, dataStyle, r.getName(), r.getType(), r.getObtainDate(),
                        str(r.getRank()), r.getOtherParticipants());
            }
            rowIdx++;
        }

        // 指导竞赛
        if (detail.getCompetition() != null) {
            CompetitionRecord c = detail.getCompetition();
            rowIdx = writeSectionTitle(sheet, rowIdx, "【指导竞赛】", titleStyle);
            rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "竞赛类别", "竞赛名称", "主办单位", "获奖时间", "证书编号", "证书名称", "奖项级别",
                    "奖项等级", "参赛学生", "指导教师");
            rowIdx = writeDataRow(sheet, rowIdx, dataStyle, c.getCategory(), c.getName(), c.getOrganizer(),
                    c.getAwardDate(), c.getCertNo(), c.getCertName(), c.getAwardLevel(), c.getAwardGrade(),
                    c.getStudents(), c.getAdvisorTeachers());
            rowIdx++;
        }

        // 教师培训
        if (detail.getTraining() != null) {
            TrainingRecord t = detail.getTraining();
            rowIdx = writeSectionTitle(sheet, rowIdx, "【教师培训】", titleStyle);
            rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "培训类型", "培训名称", "培训形式", "培训学时", "组织单位", "开始日期", "结束日期");
            rowIdx = writeDataRow(sheet, rowIdx, dataStyle, t.getType(), t.getName(), t.getForm(), str(t.getHours()),
                    t.getOrganizer(), t.getStartDate(), t.getEndDate());
            rowIdx++;
        }

        // 咨询报告
        if (detail.getReport() != null) {
            ReportRecord r = detail.getReport();
            rowIdx = writeSectionTitle(sheet, rowIdx, "【咨询报告】", titleStyle);
            rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "报告名称", "采纳级别", "采纳日期", "本人排名", "其他参与人");
            rowIdx = writeDataRow(sheet, rowIdx, dataStyle, r.getName(), r.getLevel(), r.getAdoptDate(),
                    str(r.getRank()), r.getOthers());
            rowIdx++;
        }

        // 出版著作
        if (detail.getBook() != null) {
            TextbookRecord b = detail.getBook();
            rowIdx = writeSectionTitle(sheet, rowIdx, "【出版著作】", titleStyle);
            rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "著作名称", "出版社", "出版日期", "教材等级", "本人排名", "入选日期");
            rowIdx = writeDataRow(sheet, rowIdx, dataStyle, b.getName(), b.getPublisher(), b.getPublishDate(),
                    b.getTextbookLevel(), str(b.getRank()), b.getSelectionDate());
            rowIdx++;
        }

        // 获奖
        if (detail.getAward() != null) {
            AwardRecord a = detail.getAward();
            rowIdx = writeSectionTitle(sheet, rowIdx, "【获奖】", titleStyle);
            rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "获奖名称", "获奖类型", "获奖级别", "获奖等级", "本人排名", "集体排名", "获奖日期",
                    "证书编号");
            rowIdx = writeDataRow(sheet, rowIdx, dataStyle, a.getName(), a.getType(), a.getLevel(), a.getGrade(),
                    str(a.getRank()), str(a.getOrgRank()), a.getAwardDate(), a.getCertNo());
            rowIdx++;
        }

        // 发表论文
        if (detail.getPaper() != null) {
            PaperRecord p = detail.getPaper();
            rowIdx = writeSectionTitle(sheet, rowIdx, "【发表论文】", titleStyle);
            rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "论文类型", "论文名称", "作者类型", "其他作者", "期刊名称", "收录类别", "发表日期");
            rowIdx = writeDataRow(sheet, rowIdx, dataStyle, p.getPaperType(), p.getPaperName(), p.getAuthorType(),
                    p.getOtherAuthors(), p.getJournalName(), p.getIndexCategory(), p.getPublishDate());
            rowIdx++;
        }

        // 纵向项目
        if (detail.getVerticalProject() != null) {
            VerticalProject vp = detail.getVerticalProject();
            rowIdx = writeSectionTitle(sheet, rowIdx, "【纵向项目】", titleStyle);
            rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "教研/科研", "项目名称", "基金来源", "项目级别", "团队成员", "立项时间", "立项编号",
                    "项目状态", "验收时间", "项目经费（元）");
            rowIdx = writeDataRow(sheet, rowIdx, dataStyle, vp.getResearchType(), vp.getProjectName(),
                    vp.getFundSource(), vp.getLevel(), vp.getTeamMembers(), vp.getSetupDate(), vp.getSetupNo(),
                    vp.getUpdateStatus(), vp.getAcceptDate(), vp.getFunds());
            rowIdx++;
        }

        // 横向项目
        if (detail.getHorizontalProject() != null) {
            HorizontalProject hp = detail.getHorizontalProject();
            rowIdx = writeSectionTitle(sheet, rowIdx, "【横向项目】", titleStyle);
            rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "教研/科研", "项目名称", "基金来源", "项目级别", "团队成员", "立项时间", "立项编号",
                    "项目状态", "验收时间", "项目经费（元）");
            rowIdx = writeDataRow(sheet, rowIdx, dataStyle, hp.getResearchType(), hp.getProjectName(),
                    hp.getFundSource(), hp.getLevel(), hp.getTeamMembers(), hp.getSetupDate(), hp.getSetupNo(),
                    hp.getUpdateStatus(), hp.getAcceptDate(), hp.getFunds());
            rowIdx++;
        }

        // 创新创业项目
        if (detail.getInnovationProject() != null) {
            InnovationProject ip = detail.getInnovationProject();
            rowIdx = writeSectionTitle(sheet, rowIdx, "【创新创业项目】", titleStyle);
            rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "项目状态", "项目级别", "项目名称", "起始时间", "是否结题", "负责学生", "其他学生",
                    "项目经费（元）", "论文发表情况", "其他指导教师");
            rowIdx = writeDataRow(sheet, rowIdx, dataStyle, ip.getStatus(), ip.getLevel(), ip.getProjectName(),
                    ip.getStartDate(), ip.getCompletion(), ip.getLeaderStudent(), ip.getOtherStudents(), ip.getFunds(),
                    ip.getPaperInfo(), ip.getOtherTeachers());
        }

        // 如果完全没有数据
        if (rowIdx == 0) {
            org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(0);
            org.apache.poi.xssf.usermodel.XSSFCell cell = row.createCell(0);
            cell.setCellValue("该提交记录无数据");
            cell.setCellStyle(titleStyle);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 导出教师全部已通过的成果数据为 Excel（单 Sheet，分类别展示）
     */
    public void exportAllAchievements(Long userId, HttpServletResponse response) throws IOException {
        // 1. 查询该用户所有已通过（status=1）的提交记录
        QueryWrapper<Submission> sq = new QueryWrapper<>();
        sq.eq("user_id", userId).eq("status", 1).select("id");
        List<Submission> subs = submissionMapper.selectList(sq);
        List<Long> subIds = subs.stream().map(Submission::getId).collect(Collectors.toList());

        // 获取用户姓名用于文件名
        User user = userMapper.selectById(userId);
        String teacherName = (user != null && user.getRealName() != null) ? user.getRealName() : "未知";
        String rawFileName = teacherName + "_全部成果数据";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        // 使用 POI 直接生成单 Sheet
        org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("全部成果");

        // 样式：分类标题（蓝色背景、加粗 14 号）
        org.apache.poi.xssf.usermodel.XSSFCellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.PALE_BLUE.getIndex());
        titleStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
        titleStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        titleStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        titleStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        titleStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        titleStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        org.apache.poi.xssf.usermodel.XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setFontName("微软雅黑");
        titleStyle.setFont(titleFont);

        // 样式：列头（浅灰背景、加粗 11 号）
        org.apache.poi.xssf.usermodel.XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        org.apache.poi.xssf.usermodel.XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setFontName("微软雅黑");
        headerStyle.setFont(headerFont);

        // 样式：数据行
        org.apache.poi.xssf.usermodel.XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
        dataStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        dataStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        dataStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        dataStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        dataStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        org.apache.poi.xssf.usermodel.XSSFFont dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 11);
        dataFont.setFontName("微软雅黑");
        dataStyle.setFont(dataFont);

        int rowIdx = 0;
        // 设置默认列宽
        sheet.setDefaultColumnWidth(18);

        if (!subIds.isEmpty()) {
            // 2. 批量查询各子表
            List<IpRecord> allIp = ipRecordMapper.selectList(new QueryWrapper<IpRecord>().in("submission_id", subIds));
            List<CompetitionRecord> allComp = competitionRecordMapper
                    .selectList(new QueryWrapper<CompetitionRecord>().in("submission_id", subIds));
            List<TrainingRecord> allTrain = trainingRecordMapper
                    .selectList(new QueryWrapper<TrainingRecord>().in("submission_id", subIds));
            List<ReportRecord> allReport = reportRecordMapper
                    .selectList(new QueryWrapper<ReportRecord>().in("submission_id", subIds));
            List<TextbookRecord> allBook = textbookRecordMapper
                    .selectList(new QueryWrapper<TextbookRecord>().in("submission_id", subIds));
            List<AwardRecord> allAward = awardRecordMapper
                    .selectList(new QueryWrapper<AwardRecord>().in("submission_id", subIds));
            List<PaperRecord> allPaper = paperRecordMapper
                    .selectList(new QueryWrapper<PaperRecord>().in("submission_id", subIds));
            List<VerticalProject> allVp = verticalProjectMapper
                    .selectList(new QueryWrapper<VerticalProject>().in("submission_id", subIds));
            List<HorizontalProject> allHp = horizontalProjectMapper
                    .selectList(new QueryWrapper<HorizontalProject>().in("submission_id", subIds));
            List<InnovationProject> allInnov = innovationProjectMapper
                    .selectList(new QueryWrapper<InnovationProject>().in("submission_id", subIds));

            // 3. 逐类别写入
            // 知识产权
            if (!allIp.isEmpty()) {
                rowIdx = writeSectionTitle(sheet, rowIdx, "【知识产权】", titleStyle);
                rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "知识产权名称", "类型", "取得日期", "本人排名", "其他参与人");
                for (IpRecord r : allIp) {
                    rowIdx = writeDataRow(sheet, rowIdx, dataStyle, r.getName(), r.getType(), r.getObtainDate(),
                            str(r.getRank()), r.getOtherParticipants());
                }
                rowIdx++; // 空行分隔
            }

            // 指导竞赛
            if (!allComp.isEmpty()) {
                rowIdx = writeSectionTitle(sheet, rowIdx, "【指导竞赛】", titleStyle);
                rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "竞赛类别", "竞赛名称", "主办单位", "获奖时间", "证书编号", "证书名称",
                        "奖项级别", "奖项等级", "参赛学生", "指导教师");
                for (CompetitionRecord c : allComp) {
                    rowIdx = writeDataRow(sheet, rowIdx, dataStyle, c.getCategory(), c.getName(), c.getOrganizer(),
                            c.getAwardDate(), c.getCertNo(), c.getCertName(), c.getAwardLevel(), c.getAwardGrade(),
                            c.getStudents(), c.getAdvisorTeachers());
                }
                rowIdx++;
            }

            // 教师培训
            if (!allTrain.isEmpty()) {
                rowIdx = writeSectionTitle(sheet, rowIdx, "【教师培训】", titleStyle);
                rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "培训类型", "培训名称", "培训形式", "培训学时", "组织单位", "开始日期",
                        "结束日期");
                for (TrainingRecord t : allTrain) {
                    rowIdx = writeDataRow(sheet, rowIdx, dataStyle, t.getType(), t.getName(), t.getForm(),
                            str(t.getHours()), t.getOrganizer(), t.getStartDate(), t.getEndDate());
                }
                rowIdx++;
            }

            // 咨询报告
            if (!allReport.isEmpty()) {
                rowIdx = writeSectionTitle(sheet, rowIdx, "【咨询报告】", titleStyle);
                rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "报告名称", "采纳级别", "采纳日期", "本人排名", "其他参与人");
                for (ReportRecord r : allReport) {
                    rowIdx = writeDataRow(sheet, rowIdx, dataStyle, r.getName(), r.getLevel(), r.getAdoptDate(),
                            str(r.getRank()), r.getOthers());
                }
                rowIdx++;
            }

            // 出版著作
            if (!allBook.isEmpty()) {
                rowIdx = writeSectionTitle(sheet, rowIdx, "【出版著作】", titleStyle);
                rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "著作名称", "出版社", "出版日期", "教材等级", "本人排名", "入选日期");
                for (TextbookRecord b : allBook) {
                    rowIdx = writeDataRow(sheet, rowIdx, dataStyle, b.getName(), b.getPublisher(), b.getPublishDate(),
                            b.getTextbookLevel(), str(b.getRank()), b.getSelectionDate());
                }
                rowIdx++;
            }

            // 获奖
            if (!allAward.isEmpty()) {
                rowIdx = writeSectionTitle(sheet, rowIdx, "【获奖】", titleStyle);
                rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "获奖名称", "获奖类型", "获奖级别", "获奖等级", "本人排名", "集体排名",
                        "获奖日期", "证书编号");
                for (AwardRecord a : allAward) {
                    rowIdx = writeDataRow(sheet, rowIdx, dataStyle, a.getName(), a.getType(), a.getLevel(),
                            a.getGrade(), str(a.getRank()), str(a.getOrgRank()), a.getAwardDate(), a.getCertNo());
                }
                rowIdx++;
            }

            // 发表论文
            if (!allPaper.isEmpty()) {
                rowIdx = writeSectionTitle(sheet, rowIdx, "【发表论文】", titleStyle);
                rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "论文类型", "论文名称", "作者类型", "其他作者", "期刊名称", "收录类别",
                        "发表日期");
                for (PaperRecord p : allPaper) {
                    rowIdx = writeDataRow(sheet, rowIdx, dataStyle, p.getPaperType(), p.getPaperName(),
                            p.getAuthorType(), p.getOtherAuthors(), p.getJournalName(), p.getIndexCategory(),
                            p.getPublishDate());
                }
                rowIdx++;
            }

            // 纵向项目
            if (!allVp.isEmpty()) {
                rowIdx = writeSectionTitle(sheet, rowIdx, "【纵向项目】", titleStyle);
                rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "教研/科研", "项目名称", "基金来源", "项目级别", "团队成员", "立项时间",
                        "立项编号", "项目状态", "验收时间", "项目经费（元）");
                for (VerticalProject vp : allVp) {
                    rowIdx = writeDataRow(sheet, rowIdx, dataStyle, vp.getResearchType(), vp.getProjectName(),
                            vp.getFundSource(), vp.getLevel(), vp.getTeamMembers(), vp.getSetupDate(), vp.getSetupNo(),
                            vp.getUpdateStatus(), vp.getAcceptDate(), vp.getFunds());
                }
                rowIdx++;
            }

            // 横向项目
            if (!allHp.isEmpty()) {
                rowIdx = writeSectionTitle(sheet, rowIdx, "【横向项目】", titleStyle);
                rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "教研/科研", "项目名称", "基金来源", "项目级别", "团队成员", "立项时间",
                        "立项编号", "项目状态", "验收时间", "项目经费（元）");
                for (HorizontalProject hp : allHp) {
                    rowIdx = writeDataRow(sheet, rowIdx, dataStyle, hp.getResearchType(), hp.getProjectName(),
                            hp.getFundSource(), hp.getLevel(), hp.getTeamMembers(), hp.getSetupDate(), hp.getSetupNo(),
                            hp.getUpdateStatus(), hp.getAcceptDate(), hp.getFunds());
                }
                rowIdx++;
            }

            // 创新创业项目
            if (!allInnov.isEmpty()) {
                rowIdx = writeSectionTitle(sheet, rowIdx, "【创新创业项目】", titleStyle);
                rowIdx = writeHeaderRow(sheet, rowIdx, headerStyle, "项目状态", "项目级别", "项目名称", "起始时间", "是否结题", "负责学生",
                        "其他学生", "项目经费（元）", "论文发表情况", "其他指导教师");
                for (InnovationProject ip : allInnov) {
                    rowIdx = writeDataRow(sheet, rowIdx, dataStyle, ip.getStatus(), ip.getLevel(), ip.getProjectName(),
                            ip.getStartDate(), ip.getCompletion(), ip.getLeaderStudent(), ip.getOtherStudents(),
                            ip.getFunds(), ip.getPaperInfo(), ip.getOtherTeachers());
                }
            }
        }

        // 如果完全没有数据，留个提示
        if (rowIdx == 0) {
            org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(0);
            org.apache.poi.xssf.usermodel.XSSFCell cell = row.createCell(0);
            cell.setCellValue("暂无已通过的成果数据");
            cell.setCellStyle(titleStyle);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /** 写入分类标题行 */
    private int writeSectionTitle(org.apache.poi.xssf.usermodel.XSSFSheet sheet, int rowIdx,
            String title, org.apache.poi.xssf.usermodel.XSSFCellStyle style) {
        org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(rowIdx);
        row.setHeightInPoints(28);
        org.apache.poi.xssf.usermodel.XSSFCell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        return rowIdx + 1;
    }

    /** 写入列头行 */
    private int writeHeaderRow(org.apache.poi.xssf.usermodel.XSSFSheet sheet, int rowIdx,
            org.apache.poi.xssf.usermodel.XSSFCellStyle style, String... headers) {
        org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(rowIdx);
        row.setHeightInPoints(24);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.xssf.usermodel.XSSFCell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
        return rowIdx + 1;
    }

    /** 写入数据行 */
    private int writeDataRow(org.apache.poi.xssf.usermodel.XSSFSheet sheet, int rowIdx,
            org.apache.poi.xssf.usermodel.XSSFCellStyle style, String... values) {
        org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(rowIdx);
        row.setHeightInPoints(22);
        for (int i = 0; i < values.length; i++) {
            org.apache.poi.xssf.usermodel.XSSFCell cell = row.createCell(i);
            cell.setCellValue(values[i] != null ? values[i] : "");
            cell.setCellStyle(style);
        }
        return rowIdx + 1;
    }

    /** 安全转 String */
    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    /**
     * 批量导出提交数据为 Excel（管理员）
     * 
     * @param status 审核状态筛选（可选）
     * @param deptId 部门筛选（可选）
     */
    public void exportSubmissions(Integer status, Long deptId,
            HttpServletResponse response) throws IOException {

        // 1. 构建查询条件
        QueryWrapper<Submission> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }

        // 如果指定了部门，先查出该部门所有用户ID
        if (deptId != null) {
            QueryWrapper<User> uq = new QueryWrapper<>();
            uq.eq("dept_id", deptId).select("id");
            List<User> deptUsers = userMapper.selectList(uq);
            if (deptUsers.isEmpty()) {
                // 该部门无用户，直接导出空文件
                writeEmptyExport(response);
                return;
            }
            List<Long> userIds = deptUsers.stream().map(User::getId).collect(Collectors.toList());
            wrapper.in("user_id", userIds);
        }

        wrapper.orderByDesc("create_time");
        List<Submission> submissions = submissionMapper.selectList(wrapper);

        if (submissions.isEmpty()) {
            writeEmptyExport(response);
            return;
        }

        // 2. 批量查询用户信息
        List<Long> userIds = submissions.stream().map(Submission::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
        }

        // 3. 收集所有 submissionId
        List<Long> subIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

        // 建立 submissionId -> User 的映射
        Map<Long, User> subUserMap = new HashMap<>();
        for (Submission sub : submissions) {
            subUserMap.put(sub.getId(), userMap.get(sub.getUserId()));
        }

        // 4. 批量查询各子表
        List<IpRecord> allIp = ipRecordMapper.selectList(new QueryWrapper<IpRecord>().in("submission_id", subIds));
        List<CompetitionRecord> allComp = competitionRecordMapper
                .selectList(new QueryWrapper<CompetitionRecord>().in("submission_id", subIds));
        List<TrainingRecord> allTrain = trainingRecordMapper
                .selectList(new QueryWrapper<TrainingRecord>().in("submission_id", subIds));
        List<ReportRecord> allReport = reportRecordMapper
                .selectList(new QueryWrapper<ReportRecord>().in("submission_id", subIds));
        List<TextbookRecord> allBook = textbookRecordMapper
                .selectList(new QueryWrapper<TextbookRecord>().in("submission_id", subIds));
        List<AwardRecord> allAward = awardRecordMapper
                .selectList(new QueryWrapper<AwardRecord>().in("submission_id", subIds));
        List<PaperRecord> allPaper = paperRecordMapper
                .selectList(new QueryWrapper<PaperRecord>().in("submission_id", subIds));
        List<VerticalProject> allVp = verticalProjectMapper
                .selectList(new QueryWrapper<VerticalProject>().in("submission_id", subIds));
        List<HorizontalProject> allHp = horizontalProjectMapper
                .selectList(new QueryWrapper<HorizontalProject>().in("submission_id", subIds));
        List<InnovationProject> allInnov = innovationProjectMapper
                .selectList(new QueryWrapper<InnovationProject>().in("submission_id", subIds));

        // 5. 转换为 ExportDTO
        List<IpExportDTO> ipExport = new ArrayList<>();
        for (IpRecord r : allIp) {
            User u = subUserMap.get(r.getSubmissionId());
            IpExportDTO d = new IpExportDTO();
            d.setTeacherName(u != null ? u.getRealName() : "");
            d.setEmployeeNo(u != null ? u.getEmployeeNo() : "");
            d.setName(r.getName());
            d.setType(r.getType());
            d.setObtainDate(r.getObtainDate());
            d.setRank(r.getRank());
            d.setOtherParticipants(r.getOtherParticipants());
            ipExport.add(d);
        }

        List<CompetitionExportDTO> compExport = new ArrayList<>();
        for (CompetitionRecord c : allComp) {
            User u = subUserMap.get(c.getSubmissionId());
            CompetitionExportDTO d = new CompetitionExportDTO();
            d.setTeacherName(u != null ? u.getRealName() : "");
            d.setEmployeeNo(u != null ? u.getEmployeeNo() : "");
            d.setCategory(c.getCategory());
            d.setName(c.getName());
            d.setOrganizer(c.getOrganizer());
            d.setAwardDate(c.getAwardDate());
            d.setCertNo(c.getCertNo());
            d.setCertName(c.getCertName());
            d.setAwardLevel(c.getAwardLevel());
            d.setAwardGrade(c.getAwardGrade());
            d.setStudents(c.getStudents());
            d.setAdvisorTeachers(c.getAdvisorTeachers());
            compExport.add(d);
        }

        List<TrainingExportDTO> trainExport = new ArrayList<>();
        for (TrainingRecord t : allTrain) {
            User u = subUserMap.get(t.getSubmissionId());
            TrainingExportDTO d = new TrainingExportDTO();
            d.setTeacherName(u != null ? u.getRealName() : "");
            d.setEmployeeNo(u != null ? u.getEmployeeNo() : "");
            d.setType(t.getType());
            d.setName(t.getName());
            d.setForm(t.getForm());
            d.setHours(t.getHours());
            d.setOrganizer(t.getOrganizer());
            d.setStartDate(t.getStartDate());
            d.setEndDate(t.getEndDate());
            trainExport.add(d);
        }

        List<ReportExportDTO> reportExport = new ArrayList<>();
        for (ReportRecord r : allReport) {
            User u = subUserMap.get(r.getSubmissionId());
            ReportExportDTO d = new ReportExportDTO();
            d.setTeacherName(u != null ? u.getRealName() : "");
            d.setEmployeeNo(u != null ? u.getEmployeeNo() : "");
            d.setName(r.getName());
            d.setLevel(r.getLevel());
            d.setAdoptDate(r.getAdoptDate());
            d.setRank(r.getRank());
            d.setOthers(r.getOthers());
            reportExport.add(d);
        }

        List<BookExportDTO> bookExport = new ArrayList<>();
        for (TextbookRecord b : allBook) {
            User u = subUserMap.get(b.getSubmissionId());
            BookExportDTO d = new BookExportDTO();
            d.setTeacherName(u != null ? u.getRealName() : "");
            d.setEmployeeNo(u != null ? u.getEmployeeNo() : "");
            d.setName(b.getName());
            d.setPublisher(b.getPublisher());
            d.setPublishDate(b.getPublishDate());
            d.setTextbookLevel(b.getTextbookLevel());
            d.setRank(b.getRank());
            d.setSelectionDate(b.getSelectionDate());
            bookExport.add(d);
        }

        List<AwardExportDTO> awardExport = new ArrayList<>();
        for (AwardRecord a : allAward) {
            User u = subUserMap.get(a.getSubmissionId());
            AwardExportDTO d = new AwardExportDTO();
            d.setTeacherName(u != null ? u.getRealName() : "");
            d.setEmployeeNo(u != null ? u.getEmployeeNo() : "");
            d.setName(a.getName());
            d.setType(a.getType());
            d.setLevel(a.getLevel());
            d.setGrade(a.getGrade());
            d.setRank(a.getRank());
            d.setOrgRank(a.getOrgRank());
            d.setAwardDate(a.getAwardDate());
            d.setCertNo(a.getCertNo());
            awardExport.add(d);
        }

        List<PaperExportDTO> paperExport = new ArrayList<>();
        for (PaperRecord p : allPaper) {
            User u = subUserMap.get(p.getSubmissionId());
            PaperExportDTO d = new PaperExportDTO();
            d.setTeacherName(u != null ? u.getRealName() : "");
            d.setEmployeeNo(u != null ? u.getEmployeeNo() : "");
            d.setPaperType(p.getPaperType());
            d.setPaperName(p.getPaperName());
            d.setAuthorType(p.getAuthorType());
            d.setOtherAuthors(p.getOtherAuthors());
            d.setJournalName(p.getJournalName());
            d.setIndexCategory(p.getIndexCategory());
            d.setPublishDate(p.getPublishDate());
            paperExport.add(d);
        }

        List<VerticalProjectExportDTO> vpExport = new ArrayList<>();
        for (VerticalProject vp : allVp) {
            User u = subUserMap.get(vp.getSubmissionId());
            VerticalProjectExportDTO d = new VerticalProjectExportDTO();
            d.setTeacherName(u != null ? u.getRealName() : "");
            d.setEmployeeNo(u != null ? u.getEmployeeNo() : "");
            d.setResearchType(vp.getResearchType());
            d.setProjectName(vp.getProjectName());
            d.setFundSource(vp.getFundSource());
            d.setLevel(vp.getLevel());
            d.setTeamMembers(vp.getTeamMembers());
            d.setSetupDate(vp.getSetupDate());
            d.setSetupNo(vp.getSetupNo());
            d.setUpdateStatus(vp.getUpdateStatus());
            d.setAcceptDate(vp.getAcceptDate());
            d.setFunds(vp.getFunds());
            vpExport.add(d);
        }

        List<HorizontalProjectExportDTO> hpExport = new ArrayList<>();
        for (HorizontalProject hp : allHp) {
            User u = subUserMap.get(hp.getSubmissionId());
            HorizontalProjectExportDTO d = new HorizontalProjectExportDTO();
            d.setTeacherName(u != null ? u.getRealName() : "");
            d.setEmployeeNo(u != null ? u.getEmployeeNo() : "");
            d.setResearchType(hp.getResearchType());
            d.setProjectName(hp.getProjectName());
            d.setFundSource(hp.getFundSource());
            d.setLevel(hp.getLevel());
            d.setTeamMembers(hp.getTeamMembers());
            d.setSetupDate(hp.getSetupDate());
            d.setSetupNo(hp.getSetupNo());
            d.setUpdateStatus(hp.getUpdateStatus());
            d.setAcceptDate(hp.getAcceptDate());
            d.setFunds(hp.getFunds());
            hpExport.add(d);
        }

        List<InnovationProjectExportDTO> innovExport = new ArrayList<>();
        for (InnovationProject ip : allInnov) {
            User u = subUserMap.get(ip.getSubmissionId());
            InnovationProjectExportDTO d = new InnovationProjectExportDTO();
            d.setTeacherName(u != null ? u.getRealName() : "");
            d.setEmployeeNo(u != null ? u.getEmployeeNo() : "");
            d.setStatus(ip.getStatus());
            d.setLevel(ip.getLevel());
            d.setProjectName(ip.getProjectName());
            d.setStartDate(ip.getStartDate());
            d.setCompletion(ip.getCompletion());
            d.setLeaderStudent(ip.getLeaderStudent());
            d.setOtherStudents(ip.getOtherStudents());
            d.setFunds(ip.getFunds());
            d.setPaperInfo(ip.getPaperInfo());
            d.setOtherTeachers(ip.getOtherTeachers());
            innovExport.add(d);
        }

        // 6. 写入 Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String rawFileName = "教学科研数据导出";
        String fileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        HorizontalCellStyleStrategy styleStrategy = buildExportStyleStrategy();

        try (ExcelWriter writer = EasyExcel.write(response.getOutputStream())
                .registerWriteHandler(styleStrategy).build()) {
            writer.write(ipExport, EasyExcel.writerSheet(0, "知识产权").head(IpExportDTO.class).build());
            writer.write(compExport, EasyExcel.writerSheet(1, "指导竞赛").head(CompetitionExportDTO.class).build());
            writer.write(trainExport, EasyExcel.writerSheet(2, "教师培训").head(TrainingExportDTO.class).build());
            writer.write(reportExport, EasyExcel.writerSheet(3, "咨询报告").head(ReportExportDTO.class).build());
            writer.write(bookExport, EasyExcel.writerSheet(4, "出版著作").head(BookExportDTO.class).build());
            writer.write(awardExport, EasyExcel.writerSheet(5, "获奖").head(AwardExportDTO.class).build());
            writer.write(paperExport, EasyExcel.writerSheet(6, "发表论文").head(PaperExportDTO.class).build());
            writer.write(vpExport, EasyExcel.writerSheet(7, "纵向项目").head(VerticalProjectExportDTO.class).build());
            writer.write(hpExport, EasyExcel.writerSheet(8, "横向项目").head(HorizontalProjectExportDTO.class).build());
            writer.write(innovExport,
                    EasyExcel.writerSheet(9, "创新创业项目").head(InnovationProjectExportDTO.class).build());
        }
    }

    /** 构建导出样式策略 */
    private HorizontalCellStyleStrategy buildExportStyleStrategy() {
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        headStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        WriteFont headFont = new WriteFont();
        headFont.setFontHeightInPoints((short) 12);
        headFont.setBold(true);
        headFont.setFontName("微软雅黑");
        headStyle.setWriteFont(headFont);

        WriteCellStyle contentStyle = new WriteCellStyle();
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentStyle.setBorderBottom(BorderStyle.THIN);
        contentStyle.setBorderLeft(BorderStyle.THIN);
        contentStyle.setBorderRight(BorderStyle.THIN);
        contentStyle.setBorderTop(BorderStyle.THIN);
        WriteFont contentFont = new WriteFont();
        contentFont.setFontHeightInPoints((short) 11);
        contentFont.setFontName("微软雅黑");
        contentStyle.setWriteFont(contentFont);

        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }

    /** 写入空的导出文件 */
    private void writeEmptyExport(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("教学科研数据导出_无数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        HorizontalCellStyleStrategy styleStrategy = buildExportStyleStrategy();
        try (ExcelWriter writer = EasyExcel.write(response.getOutputStream())
                .registerWriteHandler(styleStrategy).build()) {
            writer.write(new ArrayList<>(), EasyExcel.writerSheet(0, "知识产权").head(IpExportDTO.class).build());
        }
    }
}

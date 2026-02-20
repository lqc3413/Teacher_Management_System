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
import java.util.List;
import java.util.Map;
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

        CollectionTask task = collectionTaskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 1) {
            throw new RuntimeException("任务不存在或未开放填报");
        }
        if (LocalDateTime.now().isAfter(task.getEndTime())) {
            throw new RuntimeException("任务已截止，无法提交");
        }

        // 2. 查找该用户在此任务下是否已有提交记录
        QueryWrapper<Submission> subQuery = new QueryWrapper<>();
        subQuery.eq("user_id", userId);
        subQuery.eq("task_id", taskId);
        Submission existing = submissionMapper.selectOne(subQuery);

        if (existing != null) {
            throw new RuntimeException("该任务您已提交过，不允许重复提交");
        }

        // 3. 自动填充 submitMonth
        String month = dto.getSubmitMonth();
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // 4. 插入新的提交记录
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setTaskId(taskId);
        submission.setSubmitMonth(month);
        submission.setStatus(0); // 审核中
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
        vo.setPaper(paperRecordMapper.selectOne(pq));

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
        vo.setPaper(paperRecordMapper.selectOne(pq));

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
        statusIdMap.put(0, new ArrayList<>());  // 审核中
        statusIdMap.put(1, new ArrayList<>());  // 已通过
        statusIdMap.put(2, new ArrayList<>());  // 已退回

        for (Submission sub : subs) {
            int st = sub.getStatus() != null ? sub.getStatus() : 0;
            statusIdMap.computeIfAbsent(st, k -> new ArrayList<>()).add(sub.getId());
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
        vo.setCompetitions(competitionRecordMapper.selectList(new QueryWrapper<CompetitionRecord>().in("submission_id", ids)));
        vo.setTrainings(trainingRecordMapper.selectList(new QueryWrapper<TrainingRecord>().in("submission_id", ids)));
        vo.setReports(reportRecordMapper.selectList(new QueryWrapper<ReportRecord>().in("submission_id", ids)));
        vo.setTextbooks(textbookRecordMapper.selectList(new QueryWrapper<TextbookRecord>().in("submission_id", ids)));
        vo.setVerticalProjects(verticalProjectMapper.selectList(new QueryWrapper<VerticalProject>().in("submission_id", ids)));
        vo.setHorizontalProjects(horizontalProjectMapper.selectList(new QueryWrapper<HorizontalProject>().in("submission_id", ids)));
        vo.setInnovationProjects(innovationProjectMapper.selectList(new QueryWrapper<InnovationProject>().in("submission_id", ids)));

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
        if (subs.isEmpty()) return 0;

        List<Long> ids = subs.stream().map(Submission::getId).collect(java.util.stream.Collectors.toList());
        int count = 0;

        count += ipRecordMapper.selectCount(new QueryWrapper<IpRecord>().in("submission_id", ids)).intValue();
        count += competitionRecordMapper.selectCount(new QueryWrapper<CompetitionRecord>().in("submission_id", ids)).intValue();
        count += trainingRecordMapper.selectCount(new QueryWrapper<TrainingRecord>().in("submission_id", ids)).intValue();
        count += reportRecordMapper.selectCount(new QueryWrapper<ReportRecord>().in("submission_id", ids)).intValue();
        count += textbookRecordMapper.selectCount(new QueryWrapper<TextbookRecord>().in("submission_id", ids)).intValue();
        count += awardRecordMapper.selectCount(new QueryWrapper<AwardRecord>().in("submission_id", ids)).intValue();
        count += paperRecordMapper.selectCount(new QueryWrapper<PaperRecord>().in("submission_id", ids)).intValue();
        count += verticalProjectMapper.selectCount(new QueryWrapper<VerticalProject>().in("submission_id", ids)).intValue();
        count += horizontalProjectMapper.selectCount(new QueryWrapper<HorizontalProject>().in("submission_id", ids)).intValue();
        count += innovationProjectMapper.selectCount(new QueryWrapper<InnovationProject>().in("submission_id", ids)).intValue();

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
     * 下载 Excel 标准模板（10 个 Sheet，双行表头 + 20 行带框线的空白填写区）
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

        HorizontalCellStyleStrategy styleStrategy =
                new HorizontalCellStyleStrategy(headStyle, contentStyle);

        try (ExcelWriter writer = EasyExcel.write(response.getOutputStream())
                .registerWriteHandler(styleStrategy)
                .registerWriteHandler(new TemplateStyleHandler()).build()) {
            writer.write(emptyDataRows(5, 1),
                EasyExcel.writerSheet(0, "知识产权").head(IpExcelDTO.class).build());
            writer.write(emptyDataRows(10, 1),
                EasyExcel.writerSheet(1, "指导竞赛").head(CompetitionExcelDTO.class).build());
            writer.write(emptyDataRows(7, 1),
                EasyExcel.writerSheet(2, "教师培训").head(TrainingExcelDTO.class).build());
            writer.write(emptyDataRows(5, 1),
                EasyExcel.writerSheet(3, "咨询报告").head(ReportExcelDTO.class).build());
            writer.write(emptyDataRows(6, 1),
                EasyExcel.writerSheet(4, "出版著作").head(BookExcelDTO.class).build());
            writer.write(emptyDataRows(8, 1),
                EasyExcel.writerSheet(5, "获奖").head(AwardExcelDTO.class).build());
            writer.write(emptyDataRows(7, 1),
                EasyExcel.writerSheet(6, "发表论文").head(PaperExcelDTO.class).build());
            writer.write(emptyDataRows(10, 1),
                EasyExcel.writerSheet(7, "纵向项目").head(ResearchProjectExcelDTO.class).build());
            writer.write(emptyDataRows(10, 1),
                EasyExcel.writerSheet(8, "横向项目").head(ResearchProjectExcelDTO.class).build());
            writer.write(emptyDataRows(10, 1),
                EasyExcel.writerSheet(9, "创新创业项目").head(InnovationProjectExcelDTO.class).build());
        }
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
        List<IpExcelDTO> ipRows = EasyExcel.read(file.getInputStream())
                .head(IpExcelDTO.class).sheet(0).doReadSync();
        ipRows.removeIf(r -> isBlank(r.getName()));
        List<CompetitionExcelDTO> compRows = EasyExcel.read(file.getInputStream())
                .head(CompetitionExcelDTO.class).sheet(1).doReadSync();
        compRows.removeIf(r -> isBlank(r.getName()));
        List<TrainingExcelDTO> trainRows = EasyExcel.read(file.getInputStream())
                .head(TrainingExcelDTO.class).sheet(2).doReadSync();
        trainRows.removeIf(r -> isBlank(r.getName()));
        List<ReportExcelDTO> reportRows = EasyExcel.read(file.getInputStream())
                .head(ReportExcelDTO.class).sheet(3).doReadSync();
        reportRows.removeIf(r -> isBlank(r.getName()));
        List<BookExcelDTO> bookRows = EasyExcel.read(file.getInputStream())
                .head(BookExcelDTO.class).sheet(4).doReadSync();
        bookRows.removeIf(r -> isBlank(r.getName()));
        List<AwardExcelDTO> awardRows = EasyExcel.read(file.getInputStream())
                .head(AwardExcelDTO.class).sheet(5).doReadSync();
        awardRows.removeIf(r -> isBlank(r.getName()));
        List<PaperExcelDTO> paperRows = EasyExcel.read(file.getInputStream())
                .head(PaperExcelDTO.class).sheet(6).doReadSync();
        paperRows.removeIf(r -> isBlank(r.getPaperName()));
        List<ResearchProjectExcelDTO> vpRows = EasyExcel.read(file.getInputStream())
                .head(ResearchProjectExcelDTO.class).sheet(7).doReadSync();
        vpRows.removeIf(r -> isBlank(r.getProjectName()));
        List<ResearchProjectExcelDTO> hpRows = EasyExcel.read(file.getInputStream())
                .head(ResearchProjectExcelDTO.class).sheet(8).doReadSync();
        hpRows.removeIf(r -> isBlank(r.getProjectName()));
        List<InnovationProjectExcelDTO> innovRows = EasyExcel.read(file.getInputStream())
                .head(InnovationProjectExcelDTO.class).sheet(9).doReadSync();
        innovRows.removeIf(r -> isBlank(r.getProjectName()));

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
                ip.setDate(row.getDate());
                ip.setRank(row.getRank());
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
            c.setAwardDate(row.getAwardDate());
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
            t.setHours(row.getHours());
            t.setOrganizer(row.getOrganizer());
            t.setStartDate(row.getStartDate());
            t.setEndDate(row.getEndDate());
            dto.setTraining(t);
        }

        // 咨询报告（1:1，取第一行）
        if (reportRows != null && !reportRows.isEmpty()) {
            ReportExcelDTO row = reportRows.get(0);
            ReportDTO r = new ReportDTO();
            r.setName(row.getName());
            r.setLevel(row.getLevel());
            r.setDate(row.getDate());
            r.setRank(row.getRank());
            r.setOthers(splitStr(row.getOthersStr()));
            dto.setReport(r);
        }

        // 出版著作（1:1，取第一行）
        if (bookRows != null && !bookRows.isEmpty()) {
            BookExcelDTO row = bookRows.get(0);
            BookDTO b = new BookDTO();
            b.setName(row.getName());
            b.setPublisher(row.getPublisher());
            b.setDate(row.getDate());
            b.setLevel(row.getLevel());
            b.setRank(row.getRank());
            b.setSelectionDate(row.getSelectionDate());
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
            a.setRank(row.getRank());
            a.setOrgRank(row.getOrgRank());
            a.setDate(row.getDate());
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
                p.setPublishDate(row.getPublishDate());
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
            ip.setStartDate(row.getStartDate());
            ip.setCompletion(row.getCompletion());
            ip.setLeaderStudent(row.getLeaderStudent());
            ip.setOtherStudents(splitStr(row.getOtherStudentsStr()));
            ip.setFunds(row.getFunds());
            ip.setPaperInfo(row.getPaperInfo());
            ip.setOtherTeachers(splitStr(row.getOtherTeachersStr()));
            dto.setInnovationProject(ip);
        }

        // 3. 复用 submitInfo 落库
        return submitInfo(userId, dto);
    }

    /** 逗号分隔字符串 → List，空/null 返回 null */
    /** 判断字符串是否为空（用于过滤模板空白行） */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private List<String> splitStr(String str) {
        if (str == null || str.trim().isEmpty()) return null;
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
        rp.setSetupDate(row.getSetupDate());
        rp.setSetupNo(row.getSetupNo());
        rp.setUpdateStatus(row.getUpdateStatus());
        rp.setAcceptDate(row.getAcceptDate());
        rp.setFunds(row.getFunds());
        return rp;
    }
}


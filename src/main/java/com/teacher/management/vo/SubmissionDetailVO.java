package com.teacher.management.vo;

import com.teacher.management.entity.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 提交详情聚合 VO
 */
public class SubmissionDetailVO {

    // --- 提交基本信息 ---
    private Long id;
    private String submitMonth;
    private Integer status;
    private String auditRemark;
    private LocalDateTime createTime;
    private String taskName;

    // --- 教师信息（管理员视角） ---
    private String teacherName;
    private String employeeNo;
    private String deptName;

    // --- 各子表数据 ---
    private List<IpRecord> ipList;
    private CompetitionRecord competition;
    private TrainingRecord training;
    private ReportRecord report;
    private TextbookRecord book;
    private AwardRecord award;
    private List<PaperRecord> paperList;
    private VerticalProject verticalProject;
    private HorizontalProject horizontalProject;
    private InnovationProject innovationProject;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubmitMonth() { return submitMonth; }
    public void setSubmitMonth(String submitMonth) { this.submitMonth = submitMonth; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }



    public List<IpRecord> getIpList() { return ipList; }
    public void setIpList(List<IpRecord> ipList) { this.ipList = ipList; }

    public CompetitionRecord getCompetition() { return competition; }
    public void setCompetition(CompetitionRecord competition) { this.competition = competition; }

    public TrainingRecord getTraining() { return training; }
    public void setTraining(TrainingRecord training) { this.training = training; }

    public ReportRecord getReport() { return report; }
    public void setReport(ReportRecord report) { this.report = report; }

    public TextbookRecord getBook() { return book; }
    public void setBook(TextbookRecord book) { this.book = book; }

    public AwardRecord getAward() { return award; }
    public void setAward(AwardRecord award) { this.award = award; }

    public List<PaperRecord> getPaperList() { return paperList; }
    public void setPaperList(List<PaperRecord> paperList) { this.paperList = paperList; }
    /** 向后兼容：返回第一条论文（如有），JSON 序列化时自动输出 paper 字段 */
    public PaperRecord getPaper() {
        return (paperList != null && !paperList.isEmpty()) ? paperList.get(0) : null;
    }

    public VerticalProject getVerticalProject() { return verticalProject; }
    public void setVerticalProject(VerticalProject verticalProject) { this.verticalProject = verticalProject; }

    public HorizontalProject getHorizontalProject() { return horizontalProject; }
    public void setHorizontalProject(HorizontalProject horizontalProject) { this.horizontalProject = horizontalProject; }

    public InnovationProject getInnovationProject() { return innovationProject; }
    public void setInnovationProject(InnovationProject innovationProject) { this.innovationProject = innovationProject; }
}

package com.teacher.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("sys_innovation_project")
public class InnovationProject implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private String status;         // 项目状态: 立项/结项
    private String level;          // 级别: 国家级/省级/院级
    private String projectName;    // 项目名称
    private String startDate;      // 起始时间
    private String completion;     // 是否结题: 已结题/未结题/已放弃
    private String leaderStudent;  // 项目负责学生
    private String otherStudents;  // 其他参与学生
    private String funds;          // 项目经费（元）
    private String paperInfo;      // 该项目论文发表情况
    private String otherTeachers;  // 其他指导教师

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getCompletion() { return completion; }
    public void setCompletion(String completion) { this.completion = completion; }

    public String getLeaderStudent() { return leaderStudent; }
    public void setLeaderStudent(String leaderStudent) { this.leaderStudent = leaderStudent; }

    public String getOtherStudents() { return otherStudents; }
    public void setOtherStudents(String otherStudents) { this.otherStudents = otherStudents; }

    public String getFunds() { return funds; }
    public void setFunds(String funds) { this.funds = funds; }

    public String getPaperInfo() { return paperInfo; }
    public void setPaperInfo(String paperInfo) { this.paperInfo = paperInfo; }

    public String getOtherTeachers() { return otherTeachers; }
    public void setOtherTeachers(String otherTeachers) { this.otherTeachers = otherTeachers; }
}

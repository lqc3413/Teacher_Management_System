package com.teacher.management.dto;

import java.util.List;

public class InnovationProjectDTO {
    private String status;         // 项目状态: 立项/结项
    private String level;          // 级别: 国家级/省级/院级
    private String projectName;    // 项目名称
    private String startDate;      // 起始时间
    private String completion;     // 是否结题: 已结题/未结题/已放弃
    private String leaderStudent;  // 项目负责学生
    private List<String> otherStudents;  // 其他参与学生
    private String funds;          // 项目经费（元）
    private String paperInfo;      // 该项目论文发表情况
    private List<String> otherTeachers;  // 其他指导教师

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

    public List<String> getOtherStudents() { return otherStudents; }
    public void setOtherStudents(List<String> otherStudents) { this.otherStudents = otherStudents; }

    public String getFunds() { return funds; }
    public void setFunds(String funds) { this.funds = funds; }

    public String getPaperInfo() { return paperInfo; }
    public void setPaperInfo(String paperInfo) { this.paperInfo = paperInfo; }

    public List<String> getOtherTeachers() { return otherTeachers; }
    public void setOtherTeachers(List<String> otherTeachers) { this.otherTeachers = otherTeachers; }
}

package com.teacher.management.dto;

import java.util.List;

public class CompetitionDTO {
    private String category;        // 竞赛类别
    private String name;            // 竞赛名称
    private String organizer;       // 主办单位或发证单位
    private String awardDate;       // 获奖时间 (yyyymmdd)
    private String certNo;          // 证书编号
    private String certName;        // 证书完整名称
    private String awardLevel;      // 奖项级别
    private String awardGrade;      // 奖项等级
    private List<String> students;        // 参赛学生个人姓名或团队成员
    private List<String> advisorTeachers; // 指导教师个人姓名或团队成员

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getAwardDate() { return awardDate; }
    public void setAwardDate(String awardDate) { this.awardDate = awardDate; }

    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }

    public String getCertName() { return certName; }
    public void setCertName(String certName) { this.certName = certName; }

    public String getAwardLevel() { return awardLevel; }
    public void setAwardLevel(String awardLevel) { this.awardLevel = awardLevel; }

    public String getAwardGrade() { return awardGrade; }
    public void setAwardGrade(String awardGrade) { this.awardGrade = awardGrade; }

    public List<String> getStudents() { return students; }
    public void setStudents(List<String> students) { this.students = students; }

    public List<String> getAdvisorTeachers() { return advisorTeachers; }
    public void setAdvisorTeachers(List<String> advisorTeachers) { this.advisorTeachers = advisorTeachers; }
}

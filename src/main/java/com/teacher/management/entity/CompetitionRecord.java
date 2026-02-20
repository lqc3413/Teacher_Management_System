package com.teacher.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("sys_competition_record")
public class CompetitionRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private String category;        // 竞赛类别
    private String name;            // 竞赛名称
    private String organizer;       // 主办单位或发证单位
    private String awardDate;       // 获奖时间
    private String certNo;          // 证书编号
    private String certName;        // 证书完整名称
    private String awardLevel;      // 奖项级别
    private String awardGrade;      // 奖项等级
    private String students;        // 参赛学生个人姓名或团队成员
    private String advisorTeachers; // 指导教师个人姓名或团队成员

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

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

    public String getStudents() { return students; }
    public void setStudents(String students) { this.students = students; }

    public String getAdvisorTeachers() { return advisorTeachers; }
    public void setAdvisorTeachers(String advisorTeachers) { this.advisorTeachers = advisorTeachers; }
}

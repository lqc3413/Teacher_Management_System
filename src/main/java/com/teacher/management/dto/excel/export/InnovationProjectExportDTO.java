package com.teacher.management.dto.excel.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class InnovationProjectExportDTO {
    @ExcelProperty("教师姓名")
    @ColumnWidth(16)
    private String teacherName;

    @ExcelProperty("工号")
    @ColumnWidth(14)
    private String employeeNo;

    @ExcelProperty("项目状态")
    @ColumnWidth(14)
    private String status;

    @ExcelProperty("项目级别")
    @ColumnWidth(16)
    private String level;

    @ExcelProperty("项目名称")
    @ColumnWidth(32)
    private String projectName;

    @ExcelProperty("起始时间")
    @ColumnWidth(18)
    private String startDate;

    @ExcelProperty("是否结题")
    @ColumnWidth(16)
    private String completion;

    @ExcelProperty("负责学生")
    @ColumnWidth(20)
    private String leaderStudent;

    @ExcelProperty("其他学生")
    @ColumnWidth(28)
    private String otherStudents;

    @ExcelProperty("项目经费（元）")
    @ColumnWidth(16)
    private String funds;

    @ExcelProperty("论文发表情况")
    @ColumnWidth(34)
    private String paperInfo;

    @ExcelProperty("其他指导教师")
    @ColumnWidth(28)
    private String otherTeachers;

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getCompletion() {
        return completion;
    }

    public void setCompletion(String completion) {
        this.completion = completion;
    }

    public String getLeaderStudent() {
        return leaderStudent;
    }

    public void setLeaderStudent(String leaderStudent) {
        this.leaderStudent = leaderStudent;
    }

    public String getOtherStudents() {
        return otherStudents;
    }

    public void setOtherStudents(String otherStudents) {
        this.otherStudents = otherStudents;
    }

    public String getFunds() {
        return funds;
    }

    public void setFunds(String funds) {
        this.funds = funds;
    }

    public String getPaperInfo() {
        return paperInfo;
    }

    public void setPaperInfo(String paperInfo) {
        this.paperInfo = paperInfo;
    }

    public String getOtherTeachers() {
        return otherTeachers;
    }

    public void setOtherTeachers(String otherTeachers) {
        this.otherTeachers = otherTeachers;
    }
}

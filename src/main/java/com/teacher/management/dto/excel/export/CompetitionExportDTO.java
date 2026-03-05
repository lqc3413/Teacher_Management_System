package com.teacher.management.dto.excel.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class CompetitionExportDTO {
    @ExcelProperty("教师姓名")
    @ColumnWidth(16)
    private String teacherName;

    @ExcelProperty("工号")
    @ColumnWidth(14)
    private String employeeNo;

    @ExcelProperty("竞赛类别")
    @ColumnWidth(16)
    private String category;

    @ExcelProperty("竞赛名称")
    @ColumnWidth(28)
    private String name;

    @ExcelProperty("主办单位")
    @ColumnWidth(24)
    private String organizer;

    @ExcelProperty("获奖时间")
    @ColumnWidth(18)
    private String awardDate;

    @ExcelProperty("证书编号")
    @ColumnWidth(20)
    private String certNo;

    @ExcelProperty("证书名称")
    @ColumnWidth(28)
    private String certName;

    @ExcelProperty("奖项级别")
    @ColumnWidth(14)
    private String awardLevel;

    @ExcelProperty("奖项等级")
    @ColumnWidth(18)
    private String awardGrade;

    @ExcelProperty("参赛学生")
    @ColumnWidth(28)
    private String students;

    @ExcelProperty("指导教师")
    @ColumnWidth(28)
    private String advisorTeachers;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(String awardDate) {
        this.awardDate = awardDate;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }

    public String getCertName() {
        return certName;
    }

    public void setCertName(String certName) {
        this.certName = certName;
    }

    public String getAwardLevel() {
        return awardLevel;
    }

    public void setAwardLevel(String awardLevel) {
        this.awardLevel = awardLevel;
    }

    public String getAwardGrade() {
        return awardGrade;
    }

    public void setAwardGrade(String awardGrade) {
        this.awardGrade = awardGrade;
    }

    public String getStudents() {
        return students;
    }

    public void setStudents(String students) {
        this.students = students;
    }

    public String getAdvisorTeachers() {
        return advisorTeachers;
    }

    public void setAdvisorTeachers(String advisorTeachers) {
        this.advisorTeachers = advisorTeachers;
    }
}

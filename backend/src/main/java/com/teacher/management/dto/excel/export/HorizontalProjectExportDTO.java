package com.teacher.management.dto.excel.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class HorizontalProjectExportDTO {
    @ExcelProperty("教师姓名")
    @ColumnWidth(16)
    private String teacherName;

    @ExcelProperty("工号")
    @ColumnWidth(14)
    private String employeeNo;

    @ExcelProperty("教研/科研")
    @ColumnWidth(14)
    private String researchType;

    @ExcelProperty("项目名称")
    @ColumnWidth(32)
    private String projectName;

    @ExcelProperty("基金来源")
    @ColumnWidth(20)
    private String fundSource;

    @ExcelProperty("项目级别")
    @ColumnWidth(22)
    private String level;

    @ExcelProperty("团队成员")
    @ColumnWidth(30)
    private String teamMembers;

    @ExcelProperty("立项时间")
    @ColumnWidth(18)
    private String setupDate;

    @ExcelProperty("立项编号")
    @ColumnWidth(20)
    private String setupNo;

    @ExcelProperty("项目状态")
    @ColumnWidth(14)
    private String updateStatus;

    @ExcelProperty("验收时间")
    @ColumnWidth(18)
    private String acceptDate;

    @ExcelProperty("项目经费（元）")
    @ColumnWidth(16)
    private String funds;

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

    public String getResearchType() {
        return researchType;
    }

    public void setResearchType(String researchType) {
        this.researchType = researchType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getFundSource() {
        return fundSource;
    }

    public void setFundSource(String fundSource) {
        this.fundSource = fundSource;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(String teamMembers) {
        this.teamMembers = teamMembers;
    }

    public String getSetupDate() {
        return setupDate;
    }

    public void setSetupDate(String setupDate) {
        this.setupDate = setupDate;
    }

    public String getSetupNo() {
        return setupNo;
    }

    public void setSetupNo(String setupNo) {
        this.setupNo = setupNo;
    }

    public String getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(String updateStatus) {
        this.updateStatus = updateStatus;
    }

    public String getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(String acceptDate) {
        this.acceptDate = acceptDate;
    }

    public String getFunds() {
        return funds;
    }

    public void setFunds(String funds) {
        this.funds = funds;
    }
}

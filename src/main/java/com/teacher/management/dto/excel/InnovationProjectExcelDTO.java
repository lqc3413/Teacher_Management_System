package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class InnovationProjectExcelDTO {
    @ExcelProperty(value = {"项目状态", "立项 或 结项"})
    @ColumnWidth(14)
    private String status;

    @ExcelProperty(value = {"项目级别", "国家级/省级/院级"})
    @ColumnWidth(16)
    private String level;

    @ExcelProperty(value = {"项目名称", "填写项目完整名称"})
    @ColumnWidth(32)
    private String projectName;

    @ExcelProperty(value = {"起始时间", "格式：2025-06-15"})
    @ColumnWidth(18)
    private String startDate;

    @ExcelProperty(value = {"是否结题", "已结题/未结题/已放弃"})
    @ColumnWidth(16)
    private String completion;

    @ExcelProperty(value = {"负责学生", "姓名+学号"})
    @ColumnWidth(20)
    private String leaderStudent;

    @ExcelProperty(value = {"其他学生", "多人用逗号分隔"})
    @ColumnWidth(28)
    private String otherStudentsStr;

    @ExcelProperty(value = {"项目经费（元）", "填写金额数字"})
    @ColumnWidth(16)
    private String funds;

    @ExcelProperty(value = {"论文发表情况", "作者、论文题目、期刊、页码"})
    @ColumnWidth(34)
    private String paperInfo;

    @ExcelProperty(value = {"其他指导教师", "多人用逗号分隔"})
    @ColumnWidth(28)
    private String otherTeachersStr;

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

    public String getOtherStudentsStr() { return otherStudentsStr; }
    public void setOtherStudentsStr(String otherStudentsStr) { this.otherStudentsStr = otherStudentsStr; }

    public String getFunds() { return funds; }
    public void setFunds(String funds) { this.funds = funds; }

    public String getPaperInfo() { return paperInfo; }
    public void setPaperInfo(String paperInfo) { this.paperInfo = paperInfo; }

    public String getOtherTeachersStr() { return otherTeachersStr; }
    public void setOtherTeachersStr(String otherTeachersStr) { this.otherTeachersStr = otherTeachersStr; }
}

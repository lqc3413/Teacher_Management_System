package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

/**
 * 纵向项目/横向项目共用 ExcelDTO
 */
@HeadRowHeight(30)
@ContentRowHeight(22)
public class ResearchProjectExcelDTO {
    @ExcelProperty(value = {"教研/科研", "教研 或 科研"})
    @ColumnWidth(14)
    private String researchType;

    @ExcelProperty(value = {"项目名称", "仅填本时间段立项或结项的项目"})
    @ColumnWidth(32)
    private String projectName;

    @ExcelProperty(value = {"基金来源", "项目基金来源渠道"})
    @ColumnWidth(20)
    private String fundSource;

    @ExcelProperty(value = {"项目级别", "国家级/省部级重点/省部级一般/市厅级/院级"})
    @ColumnWidth(22)
    private String level;

    @ExcelProperty(value = {"团队成员", "第一个为负责人，逗号分隔"})
    @ColumnWidth(30)
    private String teamMembersStr;

    @ExcelProperty(value = {"立项时间", "格式：2025-06-15"})
    @ColumnWidth(18)
    private String setupDate;

    @ExcelProperty(value = {"立项编号", "立项编号或文号"})
    @ColumnWidth(20)
    private String setupNo;

    @ExcelProperty(value = {"项目状态", "立项 或 结项"})
    @ColumnWidth(14)
    private String updateStatus;

    @ExcelProperty(value = {"验收时间", "格式：2025-06-15"})
    @ColumnWidth(18)
    private String acceptDate;

    @ExcelProperty(value = {"项目经费（元）", "填写金额数字"})
    @ColumnWidth(16)
    private String funds;

    public String getResearchType() { return researchType; }
    public void setResearchType(String researchType) { this.researchType = researchType; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getFundSource() { return fundSource; }
    public void setFundSource(String fundSource) { this.fundSource = fundSource; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getTeamMembersStr() { return teamMembersStr; }
    public void setTeamMembersStr(String teamMembersStr) { this.teamMembersStr = teamMembersStr; }

    public String getSetupDate() { return setupDate; }
    public void setSetupDate(String setupDate) { this.setupDate = setupDate; }

    public String getSetupNo() { return setupNo; }
    public void setSetupNo(String setupNo) { this.setupNo = setupNo; }

    public String getUpdateStatus() { return updateStatus; }
    public void setUpdateStatus(String updateStatus) { this.updateStatus = updateStatus; }

    public String getAcceptDate() { return acceptDate; }
    public void setAcceptDate(String acceptDate) { this.acceptDate = acceptDate; }

    public String getFunds() { return funds; }
    public void setFunds(String funds) { this.funds = funds; }
}

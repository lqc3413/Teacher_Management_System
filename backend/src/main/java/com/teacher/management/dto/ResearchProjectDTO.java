package com.teacher.management.dto;

import java.util.List;

/**
 * 纵向项目/横向项目共用DTO
 */
public class ResearchProjectDTO {
    private String researchType;   // 教研或科研
    private String projectName;    // 项目名称
    private String fundSource;     // 项目基金来源
    private String level;          // 项目级别
    private List<String> teamMembers; // 项目团队成员(列表)
    private String setupDate;      // 立项时间
    private String setupNo;        // 立项编号或文号
    private String updateStatus;   // 项目更新状态: 立项/结项
    private String acceptDate;     // 结题验收或鉴定时间
    private String funds;          // 项目经费金额(元)

    public String getResearchType() { return researchType; }
    public void setResearchType(String researchType) { this.researchType = researchType; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getFundSource() { return fundSource; }
    public void setFundSource(String fundSource) { this.fundSource = fundSource; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public List<String> getTeamMembers() { return teamMembers; }
    public void setTeamMembers(List<String> teamMembers) { this.teamMembers = teamMembers; }

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

package com.teacher.management.dto;

/**
 * 部门主任审核请求 DTO
 */
public class DeptDirectorAuditRequest {

    /** 是否通过 */
    private Boolean approved;

    /** 审核意见 */
    private String remark;

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

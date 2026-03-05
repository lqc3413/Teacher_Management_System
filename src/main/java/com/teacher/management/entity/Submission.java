package com.teacher.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("sys_submission")
public class Submission implements Serializable {

    /** 审核状态：待部门初审 */
    public static final int STATUS_PENDING = 0;
    /** 审核状态：已归档/终审通过 */
    public static final int STATUS_APPROVED = 1;
    /** 审核状态：已驳回（任一环节） */
    public static final int STATUS_REJECTED = 2;
    /** 审核状态：部门已通过，待终审 */
    public static final int STATUS_DEPT_APPROVED = 3;
    /** 审核状态：终审退回（退回给部门主任） */
    public static final int STATUS_FINAL_REJECTED = 4;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long taskId;
    private String submitMonth;
    private Integer status;
    private String auditRemark;
    private LocalDateTime createTime;

    // 部门初审相关字段
    private Integer deptAuditStatus;
    private Long deptAuditorId;
    private LocalDateTime deptAuditTime;
    private String deptAuditRemark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getSubmitMonth() {
        return submitMonth;
    }

    public void setSubmitMonth(String submitMonth) {
        this.submitMonth = submitMonth;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getDeptAuditStatus() {
        return deptAuditStatus;
    }

    public void setDeptAuditStatus(Integer deptAuditStatus) {
        this.deptAuditStatus = deptAuditStatus;
    }

    public Long getDeptAuditorId() {
        return deptAuditorId;
    }

    public void setDeptAuditorId(Long deptAuditorId) {
        this.deptAuditorId = deptAuditorId;
    }

    public LocalDateTime getDeptAuditTime() {
        return deptAuditTime;
    }

    public void setDeptAuditTime(LocalDateTime deptAuditTime) {
        this.deptAuditTime = deptAuditTime;
    }

    public String getDeptAuditRemark() {
        return deptAuditRemark;
    }

    public void setDeptAuditRemark(String deptAuditRemark) {
        this.deptAuditRemark = deptAuditRemark;
    }
}

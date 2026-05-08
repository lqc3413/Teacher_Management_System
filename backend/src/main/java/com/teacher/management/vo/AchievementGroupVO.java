package com.teacher.management.vo;

/**
 * 按审核状态分组的成果聚合 VO
 * approved  = status 1 (已归档/通过)
 * pending   = status 0 (审核中)
 * rejected  = status 2 (被退回)
 */
public class AchievementGroupVO {

    private AchievementVO approved;
    private AchievementVO pending;
    private AchievementVO rejected;

    // --- Getters & Setters ---
    public AchievementVO getApproved() { return approved; }
    public void setApproved(AchievementVO approved) { this.approved = approved; }

    public AchievementVO getPending() { return pending; }
    public void setPending(AchievementVO pending) { this.pending = pending; }

    public AchievementVO getRejected() { return rejected; }
    public void setRejected(AchievementVO rejected) { this.rejected = rejected; }
}

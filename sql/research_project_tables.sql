-- ============================================================
-- 纵向项目表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_vertical_project` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id`    BIGINT       NOT NULL,
    `research_type`    VARCHAR(20)  COMMENT '教研或科研类型',
    `project_name`     VARCHAR(200) COMMENT '项目名称',
    `fund_source`      VARCHAR(200) COMMENT '项目基金来源',
    `level`            VARCHAR(50)  COMMENT '项目级别',
    `team_members`     VARCHAR(2000) COMMENT '项目团队成员(JSON数组)',
    `setup_date`       VARCHAR(20)  COMMENT '立项时间',
    `setup_no`         VARCHAR(100) COMMENT '立项编号或文号',
    `update_status`    VARCHAR(20)  COMMENT '项目更新状态: 立项/结项',
    `accept_date`      VARCHAR(20)  COMMENT '结题验收或鉴定时间',
    `funds`            VARCHAR(50)  COMMENT '项目经费金额(元)',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 横向项目表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_horizontal_project` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id`    BIGINT       NOT NULL,
    `research_type`    VARCHAR(20)  COMMENT '教研或科研类型',
    `project_name`     VARCHAR(200) COMMENT '项目名称',
    `fund_source`      VARCHAR(200) COMMENT '项目基金来源',
    `level`            VARCHAR(50)  COMMENT '项目级别',
    `team_members`     VARCHAR(2000) COMMENT '项目团队成员(JSON数组)',
    `setup_date`       VARCHAR(20)  COMMENT '立项时间',
    `setup_no`         VARCHAR(100) COMMENT '立项编号或文号',
    `update_status`    VARCHAR(20)  COMMENT '项目更新状态: 立项/结项',
    `accept_date`      VARCHAR(20)  COMMENT '结题验收或鉴定时间',
    `funds`            VARCHAR(50)  COMMENT '项目经费金额(元)',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

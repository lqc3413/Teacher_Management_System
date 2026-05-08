-- ============================================================
-- 任务驱动模式：采集任务表 + 提交表改造
-- ============================================================

-- 1. 新建采集任务表
CREATE TABLE IF NOT EXISTS `sys_collection_task` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_name`   VARCHAR(200)  NOT NULL COMMENT '任务名称，如"2024年10-11月信息收集"',
    `start_time`  DATETIME      NOT NULL COMMENT '开始时间',
    `end_time`    DATETIME      NOT NULL COMMENT '截止时间',
    `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '0-未发布 1-进行中 2-已结束',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信息采集任务表';

-- 2. 提交主表新增 task_id 字段（允许 NULL 以兼容历史数据）
ALTER TABLE `sys_submission`
    ADD COLUMN `task_id` BIGINT NULL COMMENT '关联 sys_collection_task' AFTER `user_id`;

-- 3. 添加联合唯一索引，防止同一教师在同一任务中重复提交
-- 注意：task_id 为 NULL 的旧数据不受此约束影响
ALTER TABLE `sys_submission`
    ADD UNIQUE KEY `uk_user_task` (`user_id`, `task_id`);

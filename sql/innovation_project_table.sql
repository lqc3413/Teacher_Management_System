-- ============================================================
-- 指导学生参加创新创业项目表
-- ============================================================

CREATE TABLE IF NOT EXISTS `sys_innovation_project` (
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id`    BIGINT       NOT NULL,
    `status`           VARCHAR(20)  COMMENT '项目状态: 立项/结项',
    `level`            VARCHAR(20)  COMMENT '级别: 国家级/省级/院级',
    `project_name`     VARCHAR(200) COMMENT '项目名称',
    `start_date`       VARCHAR(10)  COMMENT '起始时间',
    `completion`       VARCHAR(20)  COMMENT '是否结题: 已结题/未结题/已放弃',
    `leader_student`   VARCHAR(200) COMMENT '项目负责学生（姓名学号）',
    `other_students`   VARCHAR(500) COMMENT '其他参与学生',
    `funds`            VARCHAR(50)  COMMENT '项目经费（元）',
    `paper_info`       VARCHAR(1000) COMMENT '该项目论文发表情况',
    `other_teachers`   VARCHAR(500) COMMENT '其他指导教师',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指导学生参加创新创业项目';

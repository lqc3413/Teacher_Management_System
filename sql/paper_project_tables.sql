-- ============================================================
-- 论文 / 项目立项 / 项目结项  (追加到现有数据库)
-- ============================================================

-- 1. 论文表
CREATE TABLE IF NOT EXISTS `sys_paper_record` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id`   BIGINT       NOT NULL,
    `paper_name`      VARCHAR(300) COMMENT '论文名称',
    `journal_name`    VARCHAR(200) COMMENT '发表期刊/会议',
    `publish_date`    VARCHAR(7)   COMMENT '发表时间',
    `publish_type`    VARCHAR(20)  COMMENT '刊物类型: SCI/EI/北核/南核/普刊',
    `rank`            INT          COMMENT '本人排名',
    `is_first_author` TINYINT      DEFAULT 0 COMMENT '是否第一/通讯作者 0否 1是',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发表论文';

-- 2. 项目立项表
CREATE TABLE IF NOT EXISTS `sys_project_setup_record` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id`   BIGINT       NOT NULL,
    `project_name`    VARCHAR(300) COMMENT '项目名称',
    `project_no`      VARCHAR(100) COMMENT '项目编号',
    `source`          VARCHAR(200) COMMENT '项目来源/主管单位',
    `funds`           DECIMAL(10,2) COMMENT '立项经费(万元)',
    `level`           VARCHAR(20)  COMMENT '项目级别',
    `start_date`      VARCHAR(10)  COMMENT '开始时间',
    `end_date`        VARCHAR(10)  COMMENT '计划结束时间',
    `rank`            INT          COMMENT '本人排名',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目立项';

-- 3. 项目结项表
CREATE TABLE IF NOT EXISTS `sys_project_close_record` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id`   BIGINT       NOT NULL,
    `project_name`    VARCHAR(300) COMMENT '项目名称',
    `project_no`      VARCHAR(100) COMMENT '项目编号',
    `close_date`      VARCHAR(10)  COMMENT '结项时间',
    `close_level`     VARCHAR(20)  COMMENT '结项等级: 优秀/合格/不合格',
    `rank`            INT          COMMENT '本人排名',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目结项';

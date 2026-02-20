-- Drop and recreate both tables to ensure correct schema
DROP TABLE IF EXISTS `sys_project_setup_record`;
CREATE TABLE `sys_project_setup_record` (
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

DROP TABLE IF EXISTS `sys_project_close_record`;
CREATE TABLE `sys_project_close_record` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id`   BIGINT       NOT NULL,
    `project_name`    VARCHAR(300) COMMENT '项目名称',
    `project_no`      VARCHAR(100) COMMENT '项目编号',
    `close_date`      VARCHAR(10)  COMMENT '结项时间',
    `close_level`     VARCHAR(20)  COMMENT '结项等级',
    `rank`            INT          COMMENT '本人排名',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目结项';

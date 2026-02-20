-- ============================================================
-- 教师提交业务表 (追加到现有数据库)
-- ============================================================

-- 1. 提交记录主表
CREATE TABLE IF NOT EXISTS `sys_submission` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`      BIGINT       NOT NULL COMMENT '关联 users 表',
    `submit_month` VARCHAR(7)   NOT NULL COMMENT '提交月份 如 2026-02',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0:审核中 1:已归档 2:被退回',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_month` (`user_id`, `submit_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提交记录主表';

-- 2. 基本信息更新表
CREATE TABLE IF NOT EXISTS `sys_teacher_basic_update` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id`   BIGINT       NOT NULL COMMENT '关联 sys_submission',
    `highest_education` VARCHAR(50)  COMMENT '最高学历',
    `major`           VARCHAR(100) COMMENT '所学专业',
    `school`          VARCHAR(100) COMMENT '毕业院校',
    `highest_degree`  VARCHAR(50)  COMMENT '最高学位',
    `degree_date`     VARCHAR(7)   COMMENT '取得学位时间 如 2020-06',
    `title`           VARCHAR(50)  COMMENT '专业技术职称',
    `is_dual_teacher` TINYINT      DEFAULT 0 COMMENT '是否双师型 0否 1是',
    `skill_cert`      VARCHAR(200) COMMENT '职业技能证书名称',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基本信息更新';

-- 3. 知识产权表
CREATE TABLE IF NOT EXISTS `sys_ip_record` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id` BIGINT       NOT NULL,
    `name`          VARCHAR(200) COMMENT '名称',
    `type`          VARCHAR(50)  COMMENT '类型: 发明专利/实用新型/软件著作权',
    `obtain_date`   VARCHAR(10)  COMMENT '获得时间',
    `rank`          INT          COMMENT '本人排名',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识产权';

-- 4. 指导竞赛表
CREATE TABLE IF NOT EXISTS `sys_competition_record` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id` BIGINT       NOT NULL,
    `name`          VARCHAR(200) COMMENT '竞赛名称',
    `level`         VARCHAR(20)  COMMENT '获奖等级: 国家级/省级/校级',
    `award_date`    VARCHAR(10)  COMMENT '获奖时间',
    `students`      VARCHAR(500) COMMENT '学生名单',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指导竞赛';

-- 5. 培训进修表
CREATE TABLE IF NOT EXISTS `sys_training_record` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id` BIGINT       NOT NULL,
    `type`          VARCHAR(50)  COMMENT '培训类型',
    `name`          VARCHAR(200) COMMENT '培训名称',
    `form`          VARCHAR(50)  COMMENT '培训形式',
    `hours`         INT          COMMENT '学时',
    `organizer`     VARCHAR(200) COMMENT '主办方',
    `start_date`    VARCHAR(10)  COMMENT '开始时间',
    `end_date`      VARCHAR(10)  COMMENT '结束时间',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训进修';

-- 6. 咨询调研报告表
CREATE TABLE IF NOT EXISTS `sys_report_record` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id` BIGINT       NOT NULL,
    `name`          VARCHAR(200) COMMENT '报告名称',
    `level`         VARCHAR(20)  COMMENT '采纳单位级别',
    `adopt_date`    VARCHAR(7)   COMMENT '采纳时间',
    `rank`          INT          COMMENT '本人排名',
    `others`        VARCHAR(500) COMMENT '其他参与人员',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询调研报告';

-- 7. 出版著作表
CREATE TABLE IF NOT EXISTS `sys_textbook_record` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id` BIGINT       NOT NULL,
    `name`          VARCHAR(200) COMMENT '著作名称',
    `publisher`     VARCHAR(200) COMMENT '出版社',
    `publish_date`  VARCHAR(7)   COMMENT '出版时间',
    `textbook_level` VARCHAR(50) COMMENT '教材入选情况',
    `rank`          INT          COMMENT '本人排名',
    `selection_date` VARCHAR(7)  COMMENT '入选时间',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出版著作';

-- 8. 教学科研成果奖表
CREATE TABLE IF NOT EXISTS `sys_award_record` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id` BIGINT       NOT NULL,
    `name`          VARCHAR(200) COMMENT '获奖成果名称',
    `type`          VARCHAR(50)  COMMENT '成果类型',
    `level`         VARCHAR(20)  COMMENT '获奖级别',
    `grade`         VARCHAR(20)  COMMENT '获奖等级',
    `rank`          INT          COMMENT '本人排名',
    `org_rank`      INT          COMMENT '所在单位排名',
    `award_date`    VARCHAR(10)  COMMENT '获奖时间',
    `cert_no`       VARCHAR(100) COMMENT '获奖证书编号',
    INDEX `idx_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学科研成果奖';

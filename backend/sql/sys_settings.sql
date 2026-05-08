-- ============================================================
-- 系统设置模块 - 数据库脚本
-- ============================================================

USE teacher_mgmt;

-- 1. 系统参数配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '配置ID',
    config_key   VARCHAR(100)     NOT NULL                 COMMENT '配置键',
    config_value VARCHAR(500)     DEFAULT NULL              COMMENT '配置值',
    config_desc  VARCHAR(200)     DEFAULT NULL              COMMENT '配置描述',
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统参数配置表';

-- 预置配置项
INSERT INTO sys_config (config_key, config_value, config_desc) VALUES
    ('system_name',         '高校教师资质管理系统', '系统名称'),
    ('default_password',    '123456',             '新增用户默认密码'),
    ('submission_deadline', '25',                 '每月填报截止日（号）'),
    ('system_notice',       '请各位教师按时完成每月教学科研信息填报', '系统公告/首页公告');

-- 2. 通知公告表
CREATE TABLE IF NOT EXISTS notice (
    id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '公告ID',
    title         VARCHAR(200)     NOT NULL                 COMMENT '公告标题',
    content       TEXT             DEFAULT NULL              COMMENT '公告内容',
    category      VARCHAR(50)      NOT NULL DEFAULT '系统'   COMMENT '分类：教务/科研/人事/系统',
    status        TINYINT UNSIGNED NOT NULL DEFAULT 0        COMMENT '状态: 1-已发布, 0-草稿',
    publish_time  DATETIME         DEFAULT NULL              COMMENT '发布时间',
    created_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';

-- 预置通知
INSERT INTO notice (title, content, category, status, publish_time) VALUES
    ('关于做好2026年春季学期期初教学检查工作的通知',
     '各学院（部门）：为确保新学期教学工作正常运行，现将教学检查工作安排通知如下……',
     '教务', 1, '2026-02-14 10:00:00'),
    ('关于申报2026年度国家自然科学基金项目的通知',
     '各位老师：2026年度国家自然科学基金项目申报工作已经开始，请符合条件的教师积极申报……',
     '科研', 1, '2026-02-10 09:00:00'),
    ('关于开展2025年度教职工年度考核填报工作的通知',
     '全体教职工：根据学校工作安排，现开展2025年度教职工年度考核填报工作，请在规定时间内完成填报……',
     '人事', 1, '2026-02-05 14:00:00');

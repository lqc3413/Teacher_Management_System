-- ============================================================
-- 高校教师资质管理系统 - 数据库初始化脚本
-- 适用场景: 大学/高校教师教资资料管理
-- 创建时间: 2026-02-14
-- 数据库: MySQL 8.0+
-- 使用方式: 在 DBeaver 中全选后一次性执行即可
-- ============================================================

-- 删除旧库并重建（确保干净环境）
DROP DATABASE IF EXISTS teacher_mgmt;

CREATE DATABASE teacher_mgmt
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE teacher_mgmt;

-- ============================================================
-- 1. 角色表 (roles)
-- ============================================================
CREATE TABLE roles (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '角色ID',
    role_name   VARCHAR(50)      NOT NULL                 COMMENT '角色名称',
    role_desc   VARCHAR(200)     DEFAULT NULL              COMMENT '角色描述',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 1        COMMENT '状态: 1-启用, 0-禁用',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP                                COMMENT '创建时间',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP    COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

INSERT INTO roles (role_name, role_desc) VALUES
    ('admin',   '系统管理员，负责审核教师资料及系统管理'),
    ('teacher', '高校教师，可管理自己的教资资料');

-- ============================================================
-- 2. 学院/部门表 (departments)
-- ============================================================
CREATE TABLE departments (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '学院/部门ID',
    name        VARCHAR(100)     NOT NULL                 COMMENT '学院/部门名称',
    code        VARCHAR(20)      DEFAULT NULL              COMMENT '学院编码',
    sort_order  INT              NOT NULL DEFAULT 0        COMMENT '排序值',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 1        COMMENT '状态: 1-启用, 0-禁用',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP                                COMMENT '创建时间',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP    COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学院/部门表';

INSERT INTO departments (name, code, sort_order) VALUES
    ('计算机科学与技术学院', 'CS',   1),
    ('电子信息工程学院',     'EE',   2),
    ('数学与统计学院',       'MATH', 3),
    ('外国语学院',           'FL',   4),
    ('经济管理学院',         'EM',   5),
    ('马克思主义学院',       'MKS',  6),
    ('教务处',               'JWC',  7);

-- ============================================================
-- 3. 用户表 (users)
-- 依赖: roles, departments
-- ============================================================
CREATE TABLE users (
    id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    username      VARCHAR(50)      NOT NULL                 COMMENT '用户名（登录账号）',
    password      VARCHAR(255)     NOT NULL                 COMMENT '密码（BCrypt加密存储）',
    real_name     VARCHAR(50)      DEFAULT NULL              COMMENT '真实姓名',
    employee_no   VARCHAR(30)      DEFAULT NULL              COMMENT '教职工号',
    gender        TINYINT UNSIGNED DEFAULT NULL              COMMENT '性别: 1-男, 2-女',
    phone         VARCHAR(20)      DEFAULT NULL              COMMENT '手机号码',
    email         VARCHAR(100)     DEFAULT NULL              COMMENT '电子邮箱',
    avatar        VARCHAR(500)     DEFAULT NULL              COMMENT '头像URL',
    role_id       BIGINT UNSIGNED  NOT NULL                 COMMENT '角色ID',
    dept_id       BIGINT UNSIGNED  DEFAULT NULL              COMMENT '所属学院/部门ID',
    title         VARCHAR(50)      DEFAULT NULL              COMMENT '职称（教授、副教授、讲师、助教）',
    education     VARCHAR(50)      DEFAULT NULL              COMMENT '最高学历（博士研究生、硕士研究生）',
    degree        VARCHAR(50)      DEFAULT NULL              COMMENT '最高学位（博士、硕士、学士）',
    research_area VARCHAR(200)     DEFAULT NULL              COMMENT '研究方向',
    entry_date    DATE             DEFAULT NULL              COMMENT '入职日期',
    status        TINYINT UNSIGNED NOT NULL DEFAULT 1        COMMENT '状态: 1-正常, 0-禁用',
    last_login    DATETIME         DEFAULT NULL              COMMENT '最后登录时间',
    created_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP                                COMMENT '创建时间',
    updated_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP    COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_employee_no (employee_no),
    KEY idx_role_id (role_id),
    KEY idx_dept_id (dept_id),
    KEY idx_title (title),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)       ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_users_dept FOREIGN KEY (dept_id) REFERENCES departments (id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 预置管理员账号 (密码: admin123)
INSERT INTO users (username, password, real_name, role_id, dept_id) VALUES
    ('admin', '$2a$10$jv3/Z2EzboEYdR0twTrfluUHDZLIIJtJJigfua98LMRhUt2oTbGnOa', '超级管理员', 1, 7);

-- 预置教师账号 (密码需通过 /api/dev/reset-password 设置)
INSERT INTO users (username, password, real_name, employee_no, role_id, dept_id, title, gender) VALUES
    ('zhangsan', 'NEED_RESET', '张三', 'T2024001', 2, 1, '副教授',  1),
    ('lisi',     'NEED_RESET', '李四', 'T2024002', 2, 2, '讲师',    2),
    ('wangwu',   'NEED_RESET', '王五', 'T2024003', 2, 3, '教授',    1);

-- ============================================================
-- 4. 分类表 (categories)
-- 支持树形结构，parent_id = 0 表示顶级分类
-- ============================================================
CREATE TABLE categories (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    parent_id   BIGINT UNSIGNED  NOT NULL DEFAULT 0        COMMENT '父分类ID，0为顶级',
    name        VARCHAR(100)     NOT NULL                 COMMENT '分类名称',
    sort_order  INT              NOT NULL DEFAULT 0        COMMENT '排序值',
    icon        VARCHAR(200)     DEFAULT NULL              COMMENT '分类图标',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 1        COMMENT '状态: 1-启用, 0-禁用',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP                                COMMENT '创建时间',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP    COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教资资料分类表';

-- 顶级分类 (id: 1~7)
INSERT INTO categories (parent_id, name, sort_order) VALUES
    (0, '教师资格证',     1),
    (0, '学历学位证书',   2),
    (0, '职称证书',       3),
    (0, '科研成果',       4),
    (0, '教学成果',       5),
    (0, '培训进修',       6),
    (0, '获奖荣誉',       7);

-- 教师资格证 子分类 (parent_id=1)
INSERT INTO categories (parent_id, name, sort_order) VALUES
    (1, '高等学校教师资格证',     1),
    (1, '中等职业学校教师资格证', 2);

-- 学历学位证书 子分类 (parent_id=2)
INSERT INTO categories (parent_id, name, sort_order) VALUES
    (2, '博士学位证书', 1),
    (2, '硕士学位证书', 2),
    (2, '学士学位证书', 3),
    (2, '毕业证书',     4),
    (2, '留学归国证明', 5);

-- 职称证书 子分类 (parent_id=3)
INSERT INTO categories (parent_id, name, sort_order) VALUES
    (3, '正高级（教授）',     1),
    (3, '副高级（副教授）',   2),
    (3, '中级（讲师）',       3),
    (3, '初级（助教）',       4);

-- 科研成果 子分类 (parent_id=4)
INSERT INTO categories (parent_id, name, sort_order) VALUES
    (4, '科研项目立项',   1),
    (4, '论文发表',       2),
    (4, '专著/教材',      3),
    (4, '专利证书',       4),
    (4, '软件著作权',     5);

-- 教学成果 子分类 (parent_id=5)
INSERT INTO categories (parent_id, name, sort_order) VALUES
    (5, '精品课程',       1),
    (5, '教学改革项目',   2),
    (5, '指导学生获奖',   3),
    (5, '教学评估报告',   4);

-- 培训进修 子分类 (parent_id=6)
INSERT INTO categories (parent_id, name, sort_order) VALUES
    (6, '国内访学',   1),
    (6, '海外访学',   2),
    (6, '学术会议',   3),
    (6, '继续教育',   4),
    (6, '岗前培训',   5);

-- 获奖荣誉 子分类 (parent_id=7)
INSERT INTO categories (parent_id, name, sort_order) VALUES
    (7, '国家级奖项',   1),
    (7, '省部级奖项',   2),
    (7, '校级奖项',     3),
    (7, '其他荣誉',     4);

-- ============================================================
-- 5. 教资资料表 (teaching_materials)
-- 依赖: users, categories
-- ============================================================
CREATE TABLE teaching_materials (
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '资料ID',
    user_id         BIGINT UNSIGNED  NOT NULL                 COMMENT '所属教师ID',
    category_id     BIGINT UNSIGNED  NOT NULL                 COMMENT '分类ID',
    title           VARCHAR(200)     NOT NULL                 COMMENT '资料标题',
    description     TEXT             DEFAULT NULL              COMMENT '资料描述/备注',
    cert_no         VARCHAR(100)     DEFAULT NULL              COMMENT '证书编号',
    issue_date      DATE             DEFAULT NULL              COMMENT '颁发/获得日期',
    expiry_date     DATE             DEFAULT NULL              COMMENT '过期日期（NULL为永久有效）',
    issuing_org     VARCHAR(200)     DEFAULT NULL              COMMENT '颁发机构',
    file_url        VARCHAR(500)     DEFAULT NULL              COMMENT '附件文件URL',
    file_name       VARCHAR(200)     DEFAULT NULL              COMMENT '原始文件名',
    file_size       BIGINT UNSIGNED  DEFAULT NULL              COMMENT '文件大小（字节）',
    file_type       VARCHAR(50)      DEFAULT NULL              COMMENT '文件类型（pdf, jpg, png）',
    audit_status    TINYINT UNSIGNED NOT NULL DEFAULT 0        COMMENT '审核状态: 0-待审核, 1-已通过, 2-已驳回',
    audit_remark    VARCHAR(500)     DEFAULT NULL              COMMENT '审核备注',
    auditor_id      BIGINT UNSIGNED  DEFAULT NULL              COMMENT '审核人ID',
    audit_time      DATETIME         DEFAULT NULL              COMMENT '审核时间',
    status          TINYINT UNSIGNED NOT NULL DEFAULT 1        COMMENT '状态: 1-正常, 0-已删除',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP                                COMMENT '创建时间',
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP    COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_category_id (category_id),
    KEY idx_audit_status (audit_status),
    KEY idx_cert_no (cert_no),
    KEY idx_issue_date (issue_date),
    CONSTRAINT fk_materials_user     FOREIGN KEY (user_id)     REFERENCES users (id)      ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_materials_category FOREIGN KEY (category_id) REFERENCES categories (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_materials_auditor  FOREIGN KEY (auditor_id)  REFERENCES users (id)      ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教资资料表';

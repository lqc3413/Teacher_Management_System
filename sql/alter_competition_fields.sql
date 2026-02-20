-- ============================================================
-- 指导竞赛表字段更新：4字段 → 10字段
-- ============================================================

ALTER TABLE `sys_competition_record`
    ADD COLUMN `category`         VARCHAR(20)  COMMENT '竞赛类别: A类/B类/C类/D类' AFTER `submission_id`,
    ADD COLUMN `organizer`        VARCHAR(200) COMMENT '主办单位或发证单位' AFTER `name`,
    ADD COLUMN `cert_no`          VARCHAR(100) COMMENT '证书编号' AFTER `award_date`,
    ADD COLUMN `cert_name`        VARCHAR(200) COMMENT '证书完整名称' AFTER `cert_no`,
    ADD COLUMN `award_level`      VARCHAR(20)  COMMENT '奖项级别: 国家级/省级' AFTER `cert_name`,
    ADD COLUMN `award_grade`      VARCHAR(20)  COMMENT '奖项等级: 特等奖/一等奖/二等奖/三等奖' AFTER `award_level`,
    ADD COLUMN `advisor_teachers` VARCHAR(500) COMMENT '指导教师个人姓名或团队成员' AFTER `students`;

-- 删除旧的 level 列（已被 award_level + award_grade 替代）
ALTER TABLE `sys_competition_record` DROP COLUMN `level`;

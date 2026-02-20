-- ============================================================
-- 消息通知表 (sys_message)
-- ============================================================
USE teacher_mgmt;

CREATE TABLE IF NOT EXISTS sys_message (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '消息ID',
    type        TINYINT UNSIGNED NOT NULL                 COMMENT '消息类型: 1=提交通知, 2=审核通过, 3=审核驳回',
    title       VARCHAR(200)     NOT NULL                 COMMENT '消息标题',
    content     TEXT             DEFAULT NULL              COMMENT '消息内容',
    sender_id   BIGINT UNSIGNED  DEFAULT NULL              COMMENT '发送者用户ID',
    receiver_id BIGINT UNSIGNED  NOT NULL                 COMMENT '接收者用户ID',
    related_id  BIGINT UNSIGNED  DEFAULT NULL              COMMENT '关联的提交记录ID',
    is_read     TINYINT UNSIGNED NOT NULL DEFAULT 0        COMMENT '是否已读: 0-未读, 1-已读',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_receiver_id (receiver_id),
    KEY idx_is_read (is_read),
    KEY idx_created_at (created_at),
    CONSTRAINT fk_message_receiver FOREIGN KEY (receiver_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息通知表';

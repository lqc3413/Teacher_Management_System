CREATE TABLE IF NOT EXISTS user_info (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    real_name VARCHAR(50) DEFAULT NULL COMMENT 'real name',
    phone VARCHAR(20) DEFAULT NULL COMMENT 'phone',
    address VARCHAR(200) DEFAULT NULL COMMENT 'address',
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'fk users.id',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_info_user_id (user_id),
    CONSTRAINT fk_user_info_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='user extra info';

INSERT INTO user_info (real_name, phone, address, user_id)
VALUES ('Admin User', '13800000001', 'Lab-A101', 1)
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name),
    phone = VALUES(phone),
    address = VALUES(address);

-- 管理员用户表
CREATE TABLE IF NOT EXISTS `admin_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt加密）',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role_type` TINYINT DEFAULT 1 COMMENT '角色类型：0-超级管理员，1-普通管理员（预留扩展）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理员用户表';

-- 插入默认超级管理员（账号：admin，密码：admin123）
-- 密码使用BCrypt加密，强度10
INSERT INTO `admin_user` (`username`, `password`, `real_name`, `role_type`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 0, 1);


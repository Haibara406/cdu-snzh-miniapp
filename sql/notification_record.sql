-- 消息通知记录表
CREATE TABLE IF NOT EXISTS `notification_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '通知记录ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `openid` VARCHAR(128) NOT NULL COMMENT '用户openid',
    `notification_type` VARCHAR(50) NOT NULL COMMENT '通知类型',
    `template_id` VARCHAR(100) DEFAULT NULL COMMENT '模板ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '通知标题',
    `content` TEXT DEFAULT NULL COMMENT '通知内容JSON',
    `send_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '发送状态：0=发送失败，1=发送成功',
    `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
    `business_id` BIGINT(20) DEFAULT NULL COMMENT '关联业务ID（如订单ID）',
    `send_time` DATETIME NOT NULL COMMENT '发送时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_notification_type` (`notification_type`),
    KEY `idx_business_id` (`business_id`),
    KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知记录表';


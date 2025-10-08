-- ========================================
-- 成都蜀南竹海小程序数据库完整建表语句
-- 创建时间：2025-10-07
-- 字符集：utf8mb4
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `cdu_snzh_miniapp` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `cdu_snzh_miniapp`;

-- ========================================
-- 1. 用户相关表
-- ========================================

-- 用户表
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `gender` TINYINT(1) DEFAULT 0 COMMENT '性别：0=未知, 1=男, 2=女',
  `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 2. 景点相关表
-- ========================================

-- 景点表（包含景区和景点两级）
DROP TABLE IF EXISTS `scenic_spot`;
CREATE TABLE `scenic_spot` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '景点ID',
  `parent_id` BIGINT(20) DEFAULT 1 COMMENT '父级景区ID（NULL或1表示蜀南竹海景区，景点默认为1）',
  `spot_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '类型：0=景区（蜀南竹海）, 1=景点（景区内的景点，默认值）',
  `name` VARCHAR(100) NOT NULL COMMENT '景点名称',
  `description` TEXT DEFAULT NULL COMMENT '景点描述',
  `longitude` VARCHAR(50) DEFAULT NULL COMMENT '经度',
  `latitude` VARCHAR(50) DEFAULT NULL COMMENT '纬度',
  `poi_id` VARCHAR(100) DEFAULT NULL COMMENT '高德地图POI ID',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '文字地址',
  `model_3d_url` VARCHAR(500) DEFAULT NULL COMMENT '3D模型链接',
  `display_order` INT(11) DEFAULT 0 COMMENT '展示顺序',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_spot_type` (`spot_type`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`),
  KEY `idx_display_order` (`display_order`),
  KEY `idx_poi_id` (`poi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点表（景区和景点两级结构）';

-- 景点图片表
DROP TABLE IF EXISTS `scenic_image`;
CREATE TABLE `scenic_image` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `scenic_id` BIGINT(20) NOT NULL COMMENT '关联景点ID',
  `img_url` VARCHAR(500) NOT NULL COMMENT '图片地址',
  `type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '图片类型：0=封面, 1=详情, 2=头像',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `name` VARCHAR(100) DEFAULT NULL COMMENT '照片名称',
  `size` DOUBLE DEFAULT NULL COMMENT '照片体积大小(KB)',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_scenic_id` (`scenic_id`),
  KEY `idx_type` (`type`),
  KEY `idx_sort` (`sort`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点图片表';

-- ========================================
-- 3. 门票相关表
-- ========================================

-- 票种表
DROP TABLE IF EXISTS `ticket_type`;
CREATE TABLE `ticket_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '票种ID',
  `name` VARCHAR(50) NOT NULL COMMENT '票种名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '票种描述',
  `valid_days` INT(11) DEFAULT 1 COMMENT '有效天数',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='票种表';

-- 景点票种关联表
DROP TABLE IF EXISTS `scenic_ticket`;
CREATE TABLE `scenic_ticket` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `scenic_spot_id` BIGINT(20) NOT NULL COMMENT '景点ID',
  `ticket_type_id` BIGINT(20) NOT NULL COMMENT '票种ID',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
  `sold_count` INT(11) DEFAULT 0 COMMENT '门票售出数量',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_scenic_spot_id` (`scenic_spot_id`),
  KEY `idx_ticket_type_id` (`ticket_type_id`),
  KEY `idx_status` (`status`),
  KEY `idx_price` (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景点票种关联表';

-- ========================================
-- 4. 基础设施相关表
-- ========================================

-- 基础设施类型表
DROP TABLE IF EXISTS `facility_type`;
CREATE TABLE `facility_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '类型ID',
  `name` VARCHAR(50) NOT NULL COMMENT '类型名称',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='基础设施类型表';

-- 基础设施表
DROP TABLE IF EXISTS `facility`;
CREATE TABLE `facility` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '设施ID',
  `facility_type_id` BIGINT(20) NOT NULL COMMENT '设施类型ID',
  `name` VARCHAR(100) NOT NULL COMMENT '设施名称',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `longitude` VARCHAR(50) DEFAULT NULL COMMENT '经度',
  `latitude` VARCHAR(50) DEFAULT NULL COMMENT '纬度',
  `poi_id` VARCHAR(100) DEFAULT NULL COMMENT '高德地图POI ID',
  `open_time` VARCHAR(100) DEFAULT NULL COMMENT '开放时间描述',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_facility_type_id` (`facility_type_id`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`),
  KEY `idx_poi_id` (`poi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='基础设施表';

-- ========================================
-- 5. 订单相关表
-- ========================================

-- 订单表
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '下单人手机号',
  `order_type` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '订单类型：1=景点门票, 2=住宿, 3=餐饮, 4=文创商品',
  `visit_date` DATE DEFAULT NULL COMMENT '游玩日期',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `order_status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '订单状态：0=待支付, 1=已支付, 2=已取消, 3=已退款, 4=已完成',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '订单过期时间',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_order_type` (`order_type`),
  KEY `idx_visit_date` (`visit_date`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单明细表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `item_type` TINYINT(2) NOT NULL COMMENT '订单项类型：1=门票, 2=住宿, 3=餐饮, 4=商品',
  `item_id` BIGINT(20) NOT NULL COMMENT '对应商品ID',
  `item_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `quantity` INT(11) NOT NULL DEFAULT 1 COMMENT '数量',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ========================================
-- 6. 通知消息表
-- ========================================

-- 消息通知记录表
DROP TABLE IF EXISTS `notification_record`;
CREATE TABLE `notification_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '通知记录ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '用户openid',
  `notification_type` VARCHAR(50) NOT NULL COMMENT '通知类型',
  `template_id` VARCHAR(100) NOT NULL COMMENT '模板ID',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '通知标题',
  `content` TEXT DEFAULT NULL COMMENT '通知内容JSON',
  `send_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '发送状态：0=发送失败, 1=发送成功',
  `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `business_id` BIGINT(20) DEFAULT NULL COMMENT '关联业务ID（如订单ID）',
  `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_openid` (`openid`),
  KEY `idx_notification_type` (`notification_type`),
  KEY `idx_send_status` (`send_status`),
  KEY `idx_business_id` (`business_id`),
  KEY `idx_send_time` (`send_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知记录表';

-- ========================================
-- 7. AI智能客服相关表
-- ========================================

-- AI客服会话表
DROP TABLE IF EXISTS `ai_chat_session`;
CREATE TABLE `ai_chat_session` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话唯一标识',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '会话标题（首条消息摘要）',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=已结束, 1=进行中',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI客服会话表';

-- AI客服消息记录表
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：USER=用户, AI=助手, SYSTEM=系统',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `metadata` JSON DEFAULT NULL COMMENT '元数据（工具调用记录等）',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=已删除, 1=正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI客服消息记录表';

-- AI知识库文档表
DROP TABLE IF EXISTS `ai_knowledge_doc`;
CREATE TABLE `ai_knowledge_doc` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `doc_name` VARCHAR(255) NOT NULL COMMENT '文档名称',
  `doc_type` VARCHAR(50) NOT NULL COMMENT '文档类型：SCENIC=景点介绍, POLICY=政策规则, FAQ=常见问题',
  `content` TEXT NOT NULL COMMENT '文档内容（Markdown格式）',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_doc_type` (`doc_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI知识库文档表';

-- ========================================
-- 8. 插入示例知识库文档数据
-- ========================================

-- 景点介绍文档
INSERT INTO `ai_knowledge_doc` (`doc_name`, `doc_type`, `content`, `status`) VALUES
('蜀南竹海景点介绍', 'SCENIC', 
'# 蜀南竹海景点介绍

## 景区概况
蜀南竹海位于四川省宜宾市长宁县、江安县一带，是国家AAAA级旅游景区。景区面积120平方公里，核心景区44平方公里，楠竹覆盖面积达7万余亩，是我国最大的集山水、溶洞、湖泊、瀑布于一体的原生态竹林景区。

## 核心景点

### 忘忧谷
忘忧谷是蜀南竹海的核心景区之一，全长约2公里。谷内楠竹成林，空气清新，负氧离子含量极高，是天然的"氧吧"。漫步其中，竹海涛声，清风徐来，让人忘却烦恼。

**特色**：竹林密布、空气清新、养生圣地
**建议游玩时长**：2-3小时
**最佳时节**：四季皆宜，春夏尤佳

### 天宝寨
天宝寨建于明代，是蜀南竹海的制高点。登上天宝寨，可以俯瞰整个竹海全景，万顷竹波尽收眼底。寨内有古建筑群，历史悠久，文化底蕴深厚。

**特色**：登高望远、古建筑、竹海全景
**建议游玩时长**：1.5-2小时
**注意事项**：需爬山，建议穿舒适鞋子

### 七彩飞瀑
七彩飞瀑高达200余米，飞流直下，气势磅礴。在阳光照射下，瀑布水雾呈现七彩虹光，美不胜收，是拍照打卡的绝佳地点。

**特色**：瀑布景观、彩虹、拍照圣地
**建议游玩时长**：1小时
**最佳拍照时间**：上午10:00-11:00，下午14:00-15:00

### 翡翠长廊
翡翠长廊全长2.5公里，两旁楠竹林立，遮天蔽日。阳光透过竹叶，洒下斑驳光影，宛如翡翠般通透。漫步其中，宁静致远。

**特色**：竹林小道、光影效果、宁静悠远
**建议游玩时长**：1.5-2小时
**推荐活动**：徒步、摄影

### 天皇寺
天皇寺始建于唐代，寺庙依山而建，古木参天，香火鼎盛。寺内供奉观音菩萨、弥勒佛等，环境清幽，是竹海中一处静心祈福的净土。

**特色**：古寺禅意、竹林环绕、心灵洗涤
**建议游玩时长**：1小时
**最佳体验**：清晨或傍晚，竹林雾气缭绕时

### 龙吟台
龙吟台坐落于竹海高处，传说中此地常闻龙吟之声，因而得名。站在平台之上，竹浪翻滚，山风拂面，犹如置身云端。

**特色**：观景平台、竹海云雾、传说色彩
**建议游玩时长**：40分钟
**最佳观景时间**：清晨或雨后

### 苦竹寺
苦竹寺因寺旁生长一片罕见的苦竹而得名。寺内古钟悠悠，香客络绎不绝，是体验竹文化与佛禅文化融合的绝佳之地。

**特色**：苦竹奇观、佛教文化、古朴宁静
**建议游玩时长**：1-1.5小时
**最佳季节**：春秋两季，气候温和

### 七星海
七星海是由七个天然湖泊相连而成，湖面如镜，倒映青竹与白云。乘竹筏荡漾其间，仿佛进入仙境。

**特色**：竹筏泛舟、水上竹影、静谧清幽
**建议游玩时长**：1.5小时
**推荐活动**：划船、摄影、情侣漫游

### 仙寓洞
仙寓洞为天然溶洞，洞内钟乳石奇形怪状，色彩斑斓，传说古代仙人曾于此修炼。洞外竹林环抱，凉风习习。

**特色**：溶洞探秘、奇石景观、传说故事
**建议游玩时长**：1小时
**注意事项**：洞内湿滑，请注意安全

### 琉璃廊桥
琉璃廊桥横跨竹林溪流之上，以竹木结构建成，桥顶覆以琉璃瓦，在阳光下闪烁柔光，是游客驻足拍照的热门景点。

**特色**：竹木建筑、古风廊桥、打卡胜地
**建议游玩时长**：30分钟
**最佳拍照时间**：上午9:00-10:00

### 云水湖
云水湖是蜀南竹海最大的人工湖，湖畔翠竹倒影，水鸟成群。傍晚时分，霞光与竹影交织，美不胜收。

**特色**：湖光竹影、观鸟胜地、赏夕佳处
**建议游玩时长**：1小时
**推荐活动**：环湖漫步、摄影

### 仙女湖
仙女湖因湖心岛形似"仙女飘带"而得名。湖边竹影婆娑，微风拂面，是情侣和家庭游客的浪漫去处。

**特色**：湖心岛、竹影摇曳、浪漫氛围
**建议游玩时长**：1小时
**最佳时节**：春夏季，湖水最为清澈

### 竹影古道
竹影古道是一条穿行于密集竹林的小径，林间光影斑驳，竹香扑鼻，沿途设有古色小亭，供游客歇息。

**特色**：竹林幽径、光影斑驳、休闲徒步
**建议游玩时长**：1-1.5小时
**推荐活动**：徒步、摄影、散心

### 石笋峰
石笋峰因山顶岩石形似天然石笋而得名，攀登至峰顶，可俯瞰蜀南竹海的层层竹浪，视野开阔。

**特色**：登高远眺、奇石景观、自然探险
**建议游玩时长**：2小时
**注意事项**：山路陡峭，需穿舒适登山鞋

### 墨竹园
墨竹园内种植珍稀墨竹，竹杆漆黑如墨，叶色深绿。园区内设有科普展示，介绍竹文化与竹艺。

**特色**：珍稀竹种、竹文化展示、园林景观
**建议游玩时长**：1小时
**推荐活动**：观赏、科普学习、拍照

### 飞龙峡
飞龙峡以峡谷溪流著称，水声如龙吟，峡谷间布满翠竹，夏季溪水清凉，是避暑胜地。

**特色**：峡谷竹林、清凉溪水、探险休闲
**建议游玩时长**：1-1.5小时
**最佳季节**：夏季，气温适宜，水清凉

### 青龙湖
青龙湖水质清澈，湖面映照青竹和远山，湖中有青龙岛传说。湖畔设有游船码头，可乘船环湖一周。

**特色**：湖光山色、竹影倒映、游船体验
**建议游玩时长**：1.5小时
**推荐活动**：划船、摄影、湖边散步

### 凌云台
凌云台是蜀南竹海的观景高台之一，从台上可眺望远山与竹海相连的壮丽景象，云雾缭绕时尤为仙境般美丽。

**特色**：观景平台、云雾竹海、摄影胜地
**建议游玩时长**：40分钟
**最佳观景时间**：清晨或雨后云雾弥漫时

### 虹桥景区
虹桥景区以古木桥梁和溪流闻名，桥下溪水清澈，桥面彩色玻璃铺设，在阳光下折射彩虹光影，风景如画。

**特色**：古木桥、溪流景观、拍照打卡
**建议游玩时长**：30-40分钟
**最佳拍照时间**：上午10:00-11:00

## 开放时间
- 旺季（3月-11月）：08:00-18:00
- 淡季（12月-次年2月）：08:30-17:30

## 交通指南
- 自驾：成都出发约4小时，重庆出发约3.5小时
- 公共交通：宜宾客运站有直达班车
- 景区内交通：观光车、索道

## 温馨提示
1. 景区较大，建议购买观光车票
2. 穿着舒适的运动鞋
3. 携带防晒用品和饮用水
4. 尊重自然，不要破坏竹林
', 1);

-- 退款政策文档
INSERT INTO `ai_knowledge_doc` (`doc_name`, `doc_type`, `content`, `status`) VALUES
('门票退款政策', 'POLICY',
'# 门票退款政策

## 退款规则

### 全额退款
- 游玩日期前**3天（含）**以上申请退款，可获得全额退款
- 特殊天气（台风、暴雨等）导致景区关闭，可全额退款
- 突发公共卫生事件导致景区关闭，可全额退款

### 部分退款
- 游玩日期前**1-2天**申请退款，收取**10%手续费**
- 已购买但未使用的附加服务（观光车票等），可单独退款

### 不予退款
- 游玩日期当天申请退款，**不予退款**
- 已使用/核销的门票，不予退款
- 过期门票，不予退款

## 退款流程

### 第一步：提交申请
在小程序【我的订单】页面找到对应订单，点击【申请退款】按钮

### 第二步：选择原因
选择退款原因：
- 行程变更
- 天气原因
- 健康原因
- 其他原因

### 第三步：等待审核
- 工作日提交：1个工作日内审核
- 节假日提交：3个工作日内审核

### 第四步：退款到账
- 审核通过后：1-7个工作日原路退回
- 退款方式：原支付渠道（微信支付）

## 特殊情况处理

### 不可抗力因素
遇到以下情况，可特殊申请全额退款：
- 自然灾害（地震、洪水、台风等）
- 政府征用、管制
- 突发公共卫生事件
- 其他不可抗力因素

**处理方式**：请联系客服，提供相关证明材料

### 部分游览情况
如因个人原因提前离开景区，已使用门票不予退款。

## 注意事项
1. 退款申请一经提交，不可撤销
2. 请确保退款原因真实准确
3. 套票、优惠票退款按照实际支付金额计算
4. 团体票退款请联系团体负责人统一办理

## 联系客服
- 客服电话：400-xxx-xxxx
- 在线客服：小程序【联系客服】
- 服务时间：08:00-20:00
', 1);

-- 常见问题FAQ
INSERT INTO `ai_knowledge_doc` (`doc_name`, `doc_type`, `content`, `status`) VALUES
('游客常见问题FAQ', 'FAQ',
'# 游客常见问题FAQ

## 购票相关

### Q1: 门票价格是多少？
**A**: 
- 成人票：100元/人
- 学生票：60元/人（需出示有效学生证）
- 儿童票：50元/人（1.2米-1.5米儿童）
- 老年票：60元/人（60岁以上，需出示身份证）
- 本地人票：60元/人（需出示本地身份证）
- 家庭套票：220元/2大1小
- 1.2米以下儿童免票

### Q2: 门票包含哪些内容？
**A**: 门票包含景区大门票，不包含观光车、索道等交通工具费用。建议另行购买观光车票（30元/人），节省体力。

### Q3: 门票有效期多久？
**A**: 门票当日有效，一经使用不可重复入园。如需第二天继续游览，需重新购票。

### Q4: 可以提前多久购票？
**A**: 可提前30天购票。建议至少提前1天购买，避免现场排队。

### Q5: 购票后如何使用？
**A**: 
1. 购票成功后，订单详情中会显示电子票码
2. 到达景区后，在检票口出示电子票码
3. 扫码或人工核验后即可入园

## 游览相关

### Q6: 景区游览需要多长时间？
**A**: 
- 精华半日游：4小时左右
- 经典一日游：6-8小时
- 深度两日游：建议住宿一晚，慢慢体验

### Q7: 景区内有餐厅吗？
**A**: 有。景区内有多家餐厅和小吃店，提供川菜和当地特色竹笋宴。人均消费50-80元。也可以自带食物，景区有休息区。

### Q8: 可以带宠物入园吗？
**A**: 不可以。为保护景区环境和其他游客安全，禁止携带宠物入园（导盲犬除外）。

### Q9: 景区内有住宿吗？
**A**: 有。景区及周边有多家酒店、民宿可供选择，价格200-800元/晚不等。建议提前预订。

### Q10: 下雨天可以游览吗？
**A**: 可以，但需注意安全。雨天竹海别有一番风味，空气更加清新。建议携带雨具，穿防滑鞋。暴雨天气景区可能临时关闭，请关注官方通知。

## 交通相关

### Q11: 如何到达景区？
**A**: 
- **自驾**：导航"蜀南竹海景区"，有免费停车场
- **公共交通**：宜宾客运站每天有多班直达班车
- **高铁**：宜宾西站下车后，转乘班车或打车（约1小时）

### Q12: 景区内交通怎么解决？
**A**: 
- **观光车**：30元/人，串联主要景点
- **索道**：单程40元，往返70元
- **徒步**：景区内设有徒步道，适合喜欢徒步的游客

### Q13: 停车场收费吗？
**A**: 景区停车场免费，但节假日车位紧张，建议早到或选择公共交通。

## 注意事项

### Q14: 景区内可以吸烟吗？
**A**: 不可以。景区为竹林区域，严禁吸烟和明火，违者将被罚款并承担法律责任。

### Q15: 有轮椅/婴儿车租赁吗？
**A**: 有。游客中心提供轮椅和婴儿车免费借用服务，需支付押金，归还后退还。

### Q16: 老人和儿童游览需要注意什么？
**A**: 
- 景区部分路段有台阶，请陪同照顾
- 建议乘坐观光车，减少体力消耗
- 携带必备药品
- 注意防晒和补水

### Q17: 遇到紧急情况怎么办？
**A**: 
- 医疗急救：景区内有医务室
- 走失：就近联系工作人员或拨打景区服务电话
- 投诉：拨打景区投诉电话或通过小程序反馈

### Q18: 景区有WiFi吗？
**A**: 主要游客中心和休息区有免费WiFi，但覆盖范围有限。建议提前下载离线地图。

## 最佳游览时间

### Q19: 什么时候去最好？
**A**: 
- **春季（3-5月）**：竹笋破土，新绿满山
- **夏季（6-8月）**：避暑胜地，清凉舒适
- **秋季（9-11月）**：秋高气爽，色彩斑斓
- **冬季（12-2月）**：游客较少，宁静祥和

### Q20: 节假日人多吗？
**A**: 五一、国庆等法定节假日游客较多。建议：
- 提前购票
- 早点到达（8:00前）
- 错峰游览（避开上午10:00-12:00高峰）
', 1);

-- 游览注意事项文档
INSERT INTO `ai_knowledge_doc` (`doc_name`, `doc_type`, `content`, `status`) VALUES
('游览注意事项', 'POLICY',
'# 游览注意事项

## 安全须知

### 1. 森林防火
- ⚠️ **严禁吸烟、使用明火**
- ⚠️ 不得携带易燃易爆物品入园
- ⚠️ 违者将被处以罚款，情节严重者追究法律责任

### 2. 游览安全
- 请走规定游览路线，不要进入未开发区域
- 注意脚下，部分路段有台阶和坡道
- 雨天路滑，请慢行并使用扶手
- 不要攀爬护栏和景观设施

### 3. 儿童安全
- 儿童需有成人陪同
- 看管好孩子，防止走失
- 教育孩子不要破坏竹林

### 4. 野生动物
- 景区内可能有蛇类等野生动物
- 请不要主动靠近或挑逗野生动物
- 发现异常及时联系工作人员

## 文明游览

### 1. 保护环境
- 🌿 爱护竹林，不要折断竹子或刻字
- 🌿 垃圾请投入垃圾桶，分类投放
- 🌿 不要采摘花草、竹笋
- 🌿 保持景区清洁卫生

### 2. 尊重他人
- 不要大声喧哗，保持安静
- 拍照时注意不要影响他人
- 排队时请保持秩序
- 尊重工作人员的管理

### 3. 爱护设施
- 不要破坏景区标识和设施
- 正确使用公共卫生间
- 爱护观光车和其他交通工具

## 游览建议

### 1. 穿着装备
- 👟 穿舒适的运动鞋或登山鞋
- 👒 携带遮阳帽、太阳镜
- 🧴 涂抹防晒霜
- 🧥 根据天气携带外套（山区温差大）

### 2. 物品准备
- 💧 携带足够的饮用水
- 🍫 适量零食补充体力
- 💊 常备药品（感冒药、创可贴等）
- 🔋 充电宝（山区信号可能较弱，手机耗电快）
- 🌂 雨具（山区天气多变）

### 3. 拍照提示
- 📷 注意脚下安全，不要边走边拍
- 📷 使用三脚架需征得管理方同意
- 📷 禁止使用无人机（安全原因）
- 📷 尊重他人肖像权

### 4. 身体准备
- 💪 评估自己的体力，量力而行
- 💪 高血压、心脏病患者请慎重选择爬山项目
- 💪 适当休息，不要过度疲劳
- 💪 如感不适，立即就近休息或就医

## 禁止事项

### ❌ 严禁以下行为：
1. 吸烟、使用明火
2. 携带宠物入园（导盲犬除外）
3. 携带易燃易爆、有毒有害物品
4. 破坏竹林、采摘植物
5. 攀爬护栏、景观设施
6. 在景区内野炊、烧烤
7. 随地吐痰、乱扔垃圾
8. 使用无人机、风筝等飞行器
9. 商业拍摄（未经许可）
10. 其他违反景区规定的行为

## 天气提示

### ☀️ 晴天
- 注意防晒，多补水
- 上午10:00前和下午4:00后游览较舒适

### 🌧️ 雨天
- 携带雨具，穿防滑鞋
- 注意路面湿滑
- 避免在雷雨天登高或靠近大树

### 🌫️ 雾天
- 能见度低，请慢行
- 跟随导游或团队，不要单独行动
- 雾中竹海别有意境

## 紧急联系

- 🚨 **景区报警**：110
- 🚑 **医疗急救**：120
- 📞 **景区服务**：400-xxx-xxxx
- 💬 **在线客服**：小程序【联系客服】

## 投诉建议

如遇到问题或有建议，请通过以下方式反馈：
- 小程序【意见反馈】
- 景区游客中心
- 投诉电话：400-xxx-xxxx

我们将在24小时内回复您的反馈。

---

**温馨提示**：请提前了解景区信息，合理安排行程，祝您旅途愉快！ 🎋
', 1);

-- ========================================
-- 9. 插入示例基础数据
-- ========================================

-- 基础设施类型示例数据
INSERT INTO `facility_type` (`id`, `name`, `status`) VALUES
(1, '卫生间', 1),
(2, '停车场', 1),
(3, '餐厅', 1),
(4, '商店', 1),
(5, '游客中心', 1),
(6, '医务室', 1);

-- 票种示例数据
INSERT INTO `ticket_type` (`id`, `name`, `description`, `valid_days`, `status`) VALUES
(1, '成人票', '适用于年龄在18周岁至59周岁的普通游客，无特殊优惠条件。购票时无需额外证明，凭身份证或购票凭证入园。', 1, 1),
(2, '学生票', '适用于全日制在校学生（含小学、初中、高中、中专、大专、本科），凭本人有效学生证或电子学生证购票并入园。研究生及成人教育、自考、函授、网络教育等非全日制学生不享受学生票优惠。', 1, 1),
(3, '儿童票', '适用于身高在1.2米（含）至1.5米以下的儿童可购买半价儿童票；身高低于1.2米的儿童可免费入园，但须由监护人陪同并承担监护责任；身高1.5米及以上儿童按成人票标准购票。', 1, 1),
(4, '老年票', '适用于年满60周岁及以上的人群，凭本人有效身份证享受优惠。60至69周岁可购买半价票，70周岁及以上可免费入园。', 1, 1),
(5, '家庭套票', '适用于以家庭为单位的游客，通常包含2名成人及1名或2名儿童。入园时须全体成员同时入园，套票不可拆分使用。', 1, 1),
(6, '本地人票', '适用于景区所在地区的常住居民或持本地身份证、户口本的游客，凭有效本地身份证件购票可享受专属优惠。入园时需核验身份证件原件，非本地证件无效需补回差价或按规则执行退票流程。', 1, 1);

-- ========================================
-- 10. 插入景区和景点示例数据
-- ========================================

-- 蜀南竹海景区（唯一的顶级景区，ID固定为1）
INSERT INTO `scenic_spot` (`id`, `parent_id`, `spot_type`, `name`, `description`, `longitude`, `latitude`, `address`, `display_order`, `status`) VALUES
(1, NULL, 0, '蜀南竹海', '蜀南竹海位于四川省宜宾市长宁县、江安县一带，是国家AAAA级旅游景区。景区面积120平方公里，核心景区44平方公里，楠竹覆盖面积达7万余亩，是我国最大的集山水、溶洞、湖泊、瀑布于一体的原生态竹林景区。', '104.862274', '28.824562', '四川省宜宾市长宁县蜀南竹海', 1, 1);

-- 蜀南竹海内的景点（不单独售票，parent_id都指向1）
INSERT INTO `scenic_spot` (`id`, `parent_id`, `spot_type`, `name`, `description`, `longitude`, `latitude`, `address`, `display_order`, `status`) VALUES
(2, 1, 1, '忘忧谷', '忘忧谷是蜀南竹海的核心景区之一，全长约2公里。谷内楠竹成林，空气清新，负氧离子含量极高，是天然的"氧吧"。漫步其中，竹海涛声，清风徐来，让人忘却烦恼。', '104.863456', '28.825678', '四川省宜宾市长宁县蜀南竹海忘忧谷', 1, 1),
(3, 1, 1, '天宝寨', '天宝寨建于明代，是蜀南竹海的制高点。登上天宝寨，可以俯瞰整个竹海全景，万顷竹波尽收眼底。寨内有古建筑群，历史悠久，文化底蕴深厚。', '104.864567', '28.826789', '四川省宜宾市长宁县蜀南竹海天宝寨', 2, 1),
(4, 1, 1, '七彩飞瀑', '七彩飞瀑高达200余米，飞流直下，气势磅礴。在阳光照射下，瀑布水雾呈现七彩虹光，美不胜收，是拍照打卡的绝佳地点。', '104.865678', '28.827890', '四川省宜宾市长宁县蜀南竹海七彩飞瀑', 3, 1),
(5, 1, 1, '翡翠长廊', '翡翠长廊全长2.5公里，两旁楠竹林立，遮天蔽日。阳光透过竹叶，洒下斑驳光影，宛如翡翠般通透。漫步其中，宁静致远。', '104.866789', '28.828901', '四川省宜宾市长宁县蜀南竹海翡翠长廊', 4, 1),
(6, 1, 1, '天皇寺', '天皇寺始建于唐代，寺庙依山而建，古木参天，香火鼎盛。寺内供奉观音菩萨、弥勒佛等，环境清幽，是竹海中一处静心祈福的净土。', '104.867890', '28.829012', '四川省宜宾市长宁县蜀南竹海天皇寺', 5, 1),
(7, 1, 1, '龙吟台', '龙吟台坐落于竹海高处，传说中此地常闻龙吟之声，因而得名。站在平台之上，竹浪翻滚，山风拂面，犹如置身云端。', '104.868901', '28.830123', '四川省宜宾市长宁县蜀南竹海龙吟台', 6, 1),
(8, 1, 1, '苦竹寺', '苦竹寺因寺旁生长一片罕见的苦竹而得名。寺内古钟悠悠，香客络绎不绝，是体验竹文化与佛禅文化融合的绝佳之地。', '104.869012', '28.831234', '四川省宜宾市长宁县蜀南竹海苦竹寺', 7, 1),
(9, 1, 1, '七星海', '七星海是由七个天然湖泊相连而成，湖面如镜，倒映青竹与白云。乘竹筏荡漾其间，仿佛进入仙境。', '104.870123', '28.832345', '四川省宜宾市长宁县蜀南竹海七星海', 8, 1),
(10, 1, 1, '仙寓洞', '仙寓洞为天然溶洞，洞内钟乳石奇形怪状，色彩斑斓，传说古代仙人曾于此修炼。洞外竹林环抱，凉风习习。', '104.871234', '28.833456', '四川省宜宾市长宁县蜀南竹海仙寓洞', 9, 1),
(11, 1, 1, '琉璃廊桥', '琉璃廊桥横跨竹林溪流之上，以竹木结构建成，桥顶覆以琉璃瓦，在阳光下闪烁柔光，是游客驻足拍照的热门景点。', '104.872345', '28.834567', '四川省宜宾市长宁县蜀南竹海琉璃廊桥', 10, 1),
(12, 1, 1, '云水湖', '云水湖是蜀南竹海最大的人工湖，湖畔翠竹倒影，水鸟成群。傍晚时分，霞光与竹影交织，美不胜收。', '104.873456', '28.835678', '四川省宜宾市长宁县蜀南竹海云水湖', 11, 1),
(13, 1, 1, '仙女湖', '仙女湖因湖心岛形似"仙女飘带"而得名。湖边竹影婆娑，微风拂面，是情侣和家庭游客的浪漫去处。', '104.874567', '28.836789', '四川省宜宾市长宁县蜀南竹海仙女湖', 12, 1),
(14, 1, 1, '竹影古道', '竹影古道是一条穿行于密集竹林的小径，林间光影斑驳，竹香扑鼻，沿途设有古色小亭，供游客歇息。', '104.875678', '28.837890', '四川省宜宾市长宁县蜀南竹海竹影古道', 13, 1),
(15, 1, 1, '石笋峰', '石笋峰因山顶岩石形似天然石笋而得名，攀登至峰顶，可俯瞰蜀南竹海的层层竹浪，视野开阔。', '104.876789', '28.838901', '四川省宜宾市长宁县蜀南竹海石笋峰', 14, 1),
(16, 1, 1, '墨竹园', '墨竹园内种植珍稀墨竹，竹杆漆黑如墨，叶色深绿。园区内设有科普展示，介绍竹文化与竹艺。', '104.877890', '28.840012', '四川省宜宾市长宁县蜀南竹海墨竹园', 15, 1),
(17, 1, 1, '飞龙峡', '飞龙峡以峡谷溪流著称，水声如龙吟，峡谷间布满翠竹，夏季溪水清凉，是避暑胜地。', '104.878901', '28.841123', '四川省宜宾市长宁县蜀南竹海飞龙峡', 16, 1),
(18, 1, 1, '青龙湖', '青龙湖水质清澈，湖面映照青竹和远山，湖中有青龙岛传说。湖畔设有游船码头，可乘船环湖一周。', '104.880012', '28.842234', '四川省宜宾市长宁县蜀南竹海青龙湖', 17, 1),
(19, 1, 1, '凌云台', '凌云台是蜀南竹海的观景高台之一，从台上可眺望远山与竹海相连的壮丽景象，云雾缭绕时尤为仙境般美丽。', '104.881123', '28.843345', '四川省宜宾市长宁县蜀南竹海凌云台', 18, 1),
(20, 1, 1, '虹桥景区', '虹桥景区以古木桥梁和溪流闻名，桥下溪水清澈，桥面彩色玻璃铺设，在阳光下折射彩虹光影，风景如画。', '104.882234', '28.844456', '四川省宜宾市长宁县蜀南竹海虹桥景区', 19, 1);

-- 蜀南竹海景区门票（只关联到景区，不关联景点）
INSERT INTO `scenic_ticket` (`scenic_spot_id`, `ticket_type_id`, `price`, `sold_count`, `status`) VALUES
(1, 1, 100.00, 0, 1),  -- 成人票
(1, 2, 60.00, 0, 1),   -- 学生票
(1, 3, 50.00, 0, 1),   -- 儿童票
(1, 4, 60.00, 0, 1),   -- 老年票
(1, 5, 240.00, 0, 1),  -- 家庭套票
(1, 6, 60.00, 0, 1);   -- 本地人票

-- ========================================
-- 说明
-- ========================================
-- 1. 所有表都继承了BaseTableData的字段：status, create_time, update_time
-- 2. 主键统一使用BIGINT(20)自增
-- 3. 字符串字段根据业务需求设置合适长度
-- 4. 金额字段使用DECIMAL(10,2)
-- 5. 日期时间字段使用DATETIME，日期字段使用DATE
-- 6. 状态字段使用TINYINT(1)
-- 7. 为常用查询字段添加了索引
-- 8. 使用utf8mb4字符集支持emoji表情
-- 9. 所有字段都添加了详细注释
-- 10. 包含了AI知识库的示例数据
-- 11. 景区ID=1（蜀南竹海），所有景点parent_id=1
-- 12. 共计19个景点数据，包含了AI知识库文档中提到的所有景点

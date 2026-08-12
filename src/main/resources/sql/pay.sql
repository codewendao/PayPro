SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_order
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `custom` bit(1) NULL DEFAULT NULL,
  `device` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `mobile` bit(1) NULL DEFAULT NULL,
  `money` decimal(19, 2) NULL DEFAULT NULL,
  `nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `pay_num` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `pay_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `state` int(11) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `product_id` bigint(10) NULL DEFAULT NULL,
  `pay_qr_num` int(10) NULL DEFAULT NULL,
  `order_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'PRODUCT' COMMENT '订单来源,PRODUCT来自产品表，OTHER其他',
  `notify_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通知地址',
  `expire_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;





-- ==========================================
-- 减额匹配模式 - 数据库迁移 (v2.0)
-- ==========================================
ALTER TABLE `t_order`
    ADD COLUMN `match_mode` VARCHAR(20) NOT NULL DEFAULT 'REMARK'
        COMMENT '匹配模式: REMARK=备注匹配, DECREMENT=减额匹配'
        AFTER `pay_qr_num`,
    ADD COLUMN `actual_amount` DECIMAL(19,2) NULL DEFAULT NULL
        COMMENT '实际支付金额(减额模式下与money不同)'
        AFTER `match_mode`,
    ADD COLUMN `decrement_index` INT NULL DEFAULT NULL
        COMMENT '减额槽位索引(0-based, REMARK模式为NULL)'
        AFTER `actual_amount`;

-- 减额槽位查询索引
CREATE INDEX `idx_actual_amount_state`
    ON `t_order` (`actual_amount`, `state`);

-- 备注匹配查询索引(性能优化)
CREATE INDEX `idx_pay_num_create_time`
    ON `t_order` (`pay_num`, `create_time`);

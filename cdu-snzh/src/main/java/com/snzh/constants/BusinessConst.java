package com.snzh.constants;

/**
 * @author haibara
 * @description 业务常量类
 * @since 2025/10/11
 */
public class BusinessConst {

    /**
     * 用户类型常量
     */
    public static class UserType {
        /** 管理员 */
        public static final String ADMIN = "ADMIN";
        /** 普通用户 */
        public static final String USER = "USER";
    }

    /**
     * 批量操作类型常量
     */
    public static class BatchOperation {
        /** 取消 */
        public static final String CANCEL = "cancel";
        /** 退款 */
        public static final String REFUND = "refund";
        /** 完成 */
        public static final String COMPLETE = "complete";
    }

    /**
     * 快捷筛选类型常量
     */
    public static class QuickFilter {
        /** 待支付 */
        public static final String PENDING = "pending";
        /** 已支付 */
        public static final String PAID = "paid";
        /** 已取消 */
        public static final String CANCELLED = "cancelled";
        /** 已退款 */
        public static final String REFUNDED = "refunded";
        /** 已完成 */
        public static final String COMPLETED = "completed";
        /** 异常订单 */
        public static final String ABNORMAL = "abnormal";
        /** 今日订单 */
        public static final String TODAY = "today";
    }

    /**
     * 限制数量常量
     */
    public static class Limit {
        /** 默认批量处理限制 */
        public static final long DEFAULT_BATCH_LIMIT = 1000L;
        /** 异常订单查询限制 */
        public static final long ABNORMAL_ORDER_LIMIT = 100L;
    }

    /**
     * 超级管理员角色类型
     */
    public static class RoleType {
        /** 超级管理员 */
        public static final Integer SUPER_ADMIN = 0;
        /** 普通管理员 */
        public static final Integer NORMAL_ADMIN = 1;
    }
}


package com.snzh.annotation;

import java.lang.annotation.*;

/**
 * @author haibara
 * @description 需要管理员权限注解
 * @since 2025/10/11
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
    
    /**
     * 是否需要超级管理员权限
     * true-只有超级管理员可访问，false-所有管理员都可访问
     * 默认false
     */
    boolean superAdminOnly() default false;
    
    /**
     * 角色类型（预留扩展）
     * 0-超级管理员，1-普通管理员
     * 默认空数组表示所有管理员都可访问
     */
    int[] roles() default {};
}


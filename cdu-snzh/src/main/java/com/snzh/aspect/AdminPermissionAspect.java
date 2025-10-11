package com.snzh.aspect;

import com.snzh.annotation.RequireAdmin;
import com.snzh.constants.ErrorConst;
import com.snzh.exceptions.UnauthorizedException;
import com.snzh.threadlocal.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author haibara
 * @description 管理员权限验证切面
 * @since 2025/10/11
 */
@Slf4j
@Aspect
@Component
public class AdminPermissionAspect {

    /**
     * 切点：标注了@RequireAdmin的方法或类
     */
    @Pointcut("@within(com.snzh.annotation.RequireAdmin) || @annotation(com.snzh.annotation.RequireAdmin)")
    public void requireAdminPointcut() {
    }

    /**
     * 前置通知：验证管理员权限
     */
    @Before("requireAdminPointcut()")
    public void checkAdminPermission(JoinPoint joinPoint) {
        // 从ThreadLocal获取用户类型
        String userType = UserContext.get("userType");
        
        // 验证是否为管理员
        if (!"ADMIN".equals(userType)) {
            log.warn("非管理员用户尝试访问管理端接口：{}", joinPoint.getSignature());
            throw new UnauthorizedException(ErrorConst.REQUIRE_ADMIN_PERMISSION);
        }

        // 预留：角色验证扩展
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireAdmin requireAdmin = method.getAnnotation(RequireAdmin.class);
        
        // 如果方法上没有注解，尝试获取类上的注解
        if (requireAdmin == null) {
            requireAdmin = joinPoint.getTarget().getClass().getAnnotation(RequireAdmin.class);
        }
        
        // 当前版本：所有管理员都有权限，角色验证预留扩展
        // 未来可在此处添加细化的角色权限验证
        if (requireAdmin != null && requireAdmin.roles().length > 0) {
            // Integer roleType = UserContext.get("roleType");
            // 校验roleType是否在允许的roles数组中
            log.debug("角色权限验证（预留扩展）：{}", (Object) requireAdmin.roles());
        }
    }
}


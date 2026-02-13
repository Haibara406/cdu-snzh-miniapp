package com.snzh.interceptor;

import com.snzh.constants.BusinessConst;
import com.snzh.constants.ErrorConst;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.StatusEnum;
import com.snzh.mapper.AppUserMapper;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import com.snzh.threadlocal.UserContext;
import com.snzh.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * @author haibara
 * @description jwt拦截器
 * @since 2025/9/20 17:06
 */

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisCache redisCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            writeUnauthorized(response, ErrorConst.NEED_TOKEN);
            return false;
        }

        token = token.substring(7);
        String userId;
        String status;
        String userType;
        String username;
        String roleType;

        try {
            userId = jwtUtil.getUserId(token);
            status = jwtUtil.getStatus(token);
            userType = jwtUtil.getUserType(token);
            username = jwtUtil.getUsername(token);
            roleType = jwtUtil.getRoleType(token);
            
            if(userId == null || status == null || userType == null){
                writeUnauthorized(response, ErrorConst.INVALID_TOKEN);
                return false;
            }
            if(StatusEnum.STOP.getCode().equals(Integer.valueOf(status))){
                writeUnauthorized(response, ErrorConst.USER_NOT_EXIST_OR_BANNED);
                return false;
            }

            // 验证用户是否已登出（检查Redis中是否还有Refresh Token）
            RedisKeyManage redisKey = BusinessConst.UserType.ADMIN.equals(userType) ? RedisKeyManage.ADMIN_LOGIN : RedisKeyManage.USER_LOGIN;
            String refreshToken = redisCache.get(RedisKeyBuild.createKey(redisKey, userId), String.class);
            if(refreshToken == null){
                writeUnauthorized(response, ErrorConst.USER_NOT_EXIST_OR_BANNED);
                return false;
            }

            // 存入 ThreadLocal
            UserContext.set("userId", userId);
            UserContext.set("userType", userType);
            UserContext.set("status", status);
            if (username != null) {
                UserContext.set("username", username);
            }
            if (roleType != null) {
                UserContext.set("roleType", roleType);
            }
            return true;

        } catch (ExpiredJwtException e) {
            // Access Token 过期，返回 401，让客户端调用刷新端点
            writeUnauthorized(response, ErrorConst.TOKEN_EXPIRED);
            return false;

        } catch (JwtException ex) {
            writeUnauthorized(response, ErrorConst.INVALID_TOKEN);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    // 工具方法：统一返回 401
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(message);
    }
}

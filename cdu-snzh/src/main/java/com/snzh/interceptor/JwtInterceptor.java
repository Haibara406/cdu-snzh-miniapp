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
        String status = "1";
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
                return false;
            }

            // 根据用户类型选择不同的Redis Key
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
            // Access Token 过期，尝试使用 Redis 中的 Refresh Token 续签
            userId = e.getClaims().getSubject();
            userType = (String) e.getClaims().get("userType");
            username = (String) e.getClaims().get("username");
            roleType = (String) e.getClaims().get("roleType");
            
            // 根据用户类型选择不同的Redis Key
            RedisKeyManage redisKey = BusinessConst.UserType.ADMIN.equals(userType) ? RedisKeyManage.ADMIN_LOGIN : RedisKeyManage.USER_LOGIN;
            String refreshToken = redisCache.get(RedisKeyBuild.createKey(redisKey, userId), String.class);
            if (refreshToken == null) {
                writeUnauthorized(response, ErrorConst.TOKEN_EXPIRED);
                return false;
            }

            try {
                // 验证RefreshToken有效性
                jwtUtil.parseToken(refreshToken);
                // 根据用户类型生成新的 Access Token
                String newAccessToken;
                if (BusinessConst.UserType.ADMIN.equals(userType)) {
                    newAccessToken = jwtUtil.generateAdminAccessToken(userId, username, status, roleType);
                } else {
                    newAccessToken = jwtUtil.generateAccessToken(userId, status);
                }
                response.setHeader("New-Access-Token", newAccessToken);

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

            } catch (Exception ex) {
                writeUnauthorized(response, ErrorConst.INVALID_TOKEN);
                return false;
            }

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

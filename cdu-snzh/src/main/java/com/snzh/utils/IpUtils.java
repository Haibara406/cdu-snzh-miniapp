package com.snzh.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author haibara
 * @description IP地址工具类
 * @since 2025/10/11
 */
@Slf4j
public class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端真实IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = null;
        try {
            // 从各种代理头中获取IP
            ip = request.getHeader("X-Forwarded-For");
            if (isInvalidIp(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (isInvalidIp(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (isInvalidIp(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (isInvalidIp(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (isInvalidIp(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (isInvalidIp(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.error("获取IP地址异常", e);
        }

        // 处理多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }

        // 处理localhost
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        return ip == null ? UNKNOWN : ip;
    }

    /**
     * 判断IP是否无效
     */
    private static boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }
}


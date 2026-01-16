package org.joker.comfypilot.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * IP地址工具类
 */
@Slf4j
public class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端真实IP地址
     *
     * 优先级：
     * 1. X-Forwarded-For (代理服务器传递的原始IP)
     * 2. X-Real-IP (Nginx等代理服务器设置的真实IP)
     * 3. Proxy-Client-IP (Apache代理)
     * 4. WL-Proxy-Client-IP (WebLogic代理)
     * 5. HTTP_CLIENT_IP
     * 6. HTTP_X_FORWARDED_FOR
     * 7. RemoteAddr (直接连接的IP)
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return LOCALHOST_IPV4;
        }

        String ip = null;

        // 1. X-Forwarded-For
        ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For可能包含多个IP，取第一个
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }

        // 2. X-Real-IP
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }

        // 3. Proxy-Client-IP
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }

        // 4. WL-Proxy-Client-IP
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }

        // 5. HTTP_CLIENT_IP
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }

        // 6. HTTP_X_FORWARDED_FOR
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip.trim();
        }

        // 7. RemoteAddr
        ip = request.getRemoteAddr();
        if (ip != null) {
            // 处理IPv6本地地址
            if (LOCALHOST_IPV6.equals(ip)) {
                return LOCALHOST_IPV4;
            }
            return ip.trim();
        }

        return LOCALHOST_IPV4;
    }

    /**
     * 验证IP是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null
                && !ip.isEmpty()
                && !UNKNOWN.equalsIgnoreCase(ip);
    }
}

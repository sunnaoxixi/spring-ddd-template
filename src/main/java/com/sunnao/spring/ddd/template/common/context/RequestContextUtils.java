package com.sunnao.spring.ddd.template.common.context;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 当前 HTTP 请求上下文工具
 * <p>
 * 包装 RequestContextHolder，提供客户端 IP / User-Agent 等请求元信息的统一获取；
 * 非 Web 线程（如异步线程池）调用时返回 null，调用方需容忍空值。
 */
public final class RequestContextUtils {

    /**
     * User-Agent 最大长度（与 sys_login_log.user_agent 列宽对齐）
     */
    private static final int USER_AGENT_MAX_LENGTH = 512;

    /**
     * 是否信任 X-Forwarded-For 头（由 SecurityConfigure 依据 app.security.trust-x-forwarded-for 注入，默认 false）。
     * 该头可被客户端伪造，仅在前置可信反向代理会覆盖/剥离外部传入值时才应开启，
     * 否则登录/操作日志中的 IP 可被任意伪造
     */
    private static volatile boolean trustXForwardedFor = false;

    private RequestContextUtils() {
    }

    /**
     * 设置是否信任 X-Forwarded-For 头（仅供配置类启动时调用）
     */
    public static void setTrustXForwardedFor(boolean trust) {
        trustXForwardedFor = trust;
    }

    /**
     * 获取当前线程绑定的 HttpServletRequest
     *
     * @return 当前请求，非 Web 线程返回 null
     */
    public static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    /**
     * 解析客户端IP：开启信任代理头时优先取 X-Forwarded-For 首个地址（经可信代理场景），否则取直连地址
     *
     * @return 客户端IP，无请求上下文返回 null
     */
    public static String getClientIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        if (trustXForwardedFor) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (StrUtil.isNotBlank(forwarded) && !"unknown".equalsIgnoreCase(forwarded)) {
                int commaIndex = forwarded.indexOf(',');
                return commaIndex > 0 ? forwarded.substring(0, commaIndex).trim() : forwarded.trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取客户端 User-Agent（超长截断）
     *
     * @return User-Agent，无请求上下文返回 null
     */
    public static String getUserAgent() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String userAgent = request.getHeader("User-Agent");
        if (StrUtil.isBlank(userAgent)) {
            return null;
        }
        return userAgent.length() > USER_AGENT_MAX_LENGTH
                ? userAgent.substring(0, USER_AGENT_MAX_LENGTH) : userAgent;
    }
}

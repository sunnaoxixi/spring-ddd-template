package com.sunnao.spring.ddd.template.common.context;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 当前用户上下文
 * <p>
 * 包装 Sa-Token 的登录态读取，供应用层获取操作人、审计监听器自动填充审计字段使用。
 * 非 Web 线程（如异步任务）或未登录场景下返回 null，调用方无需感知底层异常。
 */
public final class CurrentUserContext {

    private CurrentUserContext() {
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录或无请求上下文时返回 null
     */
    public static Long getUserId() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        } catch (Exception e) {
            // 非 Web 上下文（异步线程、定时任务等）无法获取登录态，视为无操作人
            return null;
        }
    }
}

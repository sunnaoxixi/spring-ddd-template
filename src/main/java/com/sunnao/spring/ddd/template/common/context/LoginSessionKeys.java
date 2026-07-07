package com.sunnao.spring.ddd.template.common.context;

/**
 * 登录会话（Sa-Token Token-Session）附加信息键
 * <p>
 * 登录成功时由 auth 应用服务写入，在线用户模块读取用于展示会话明细。
 */
public final class LoginSessionKeys {

    /**
     * 登录邮箱
     */
    public static final String EMAIL = "email";

    /**
     * 用户昵称
     */
    public static final String NICKNAME = "nickname";

    /**
     * 登录IP
     */
    public static final String IP = "ip";

    /**
     * 登录 User-Agent
     */
    public static final String USER_AGENT = "userAgent";

    /**
     * 登录时间（ISO-8601 字符串）
     */
    public static final String LOGIN_TIME = "loginTime";

}

package com.sunnao.spring.ddd.template.common.config;

import com.sunnao.spring.ddd.template.common.context.RequestContextUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 安全相关配置
 * <p>
 * 启动时将 app.security.trust-x-forwarded-for 注入 RequestContextUtils：
 * X-Forwarded-For 头可被客户端伪造，默认不信任（取直连地址），
 * 仅在前置可信反向代理（Nginx 等）会覆盖/剥离外部传入值时开启。
 */
@Configuration
public class SecurityConfigure {

    /**
     * 是否信任 X-Forwarded-For 头解析客户端IP
     */
    @Value("${app.security.trust-x-forwarded-for:false}")
    private boolean trustXForwardedFor;

    @PostConstruct
    public void init() {
        RequestContextUtils.setTrustXForwardedFor(trustXForwardedFor);
    }
}

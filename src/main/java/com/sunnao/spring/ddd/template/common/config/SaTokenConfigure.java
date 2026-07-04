package com.sunnao.spring.ddd.template.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 路由拦截配置
 * <p>
 * 除登录接口外，/api/** 全部要求登录态；
 * 注解鉴权（@SaCheckRole 等）由 SaInterceptor 一并启用。
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle ->
                        SaRouter.match("/api/**")
                                .notMatch("/api/auth/login")
                                .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/**");
    }
}

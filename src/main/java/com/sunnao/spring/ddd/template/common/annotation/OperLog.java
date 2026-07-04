package com.sunnao.spring.ddd.template.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 写接口方法上，由 OperLogAspect 环绕采集
 * （traceId、操作人、URI、参数摘要、结果码、耗时、IP）并异步落库；
 * 采集与落库失败均不影响业务主流程。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /**
     * 业务模块（如 user/role/dict/file/auth）
     */
    String module();

    /**
     * 操作动作（如 创建用户）
     */
    String action();
}

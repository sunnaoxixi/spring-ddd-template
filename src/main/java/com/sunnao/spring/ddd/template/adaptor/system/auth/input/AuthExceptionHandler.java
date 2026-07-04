package com.sunnao.spring.ddd.template.adaptor.system.auth.input;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 认证鉴权异常处理器
 * <p>
 * Sa-Token 路由拦截器与注解鉴权在进入 Controller 之前抛出异常，
 * 各层手动 catch 转 ResultDO 的模式覆盖不到，需由全局 advice 统一转换。
 */
@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

    /**
     * 未登录
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ResultDO<Void>> handleNotLogin(NotLoginException e) {
        log.warn("未登录访问被拦截, type: {}", e.getType());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResultDO.buildFailResult("NOT_LOGIN", "未登录或登录已过期"));
    }

    /**
     * 角色不满足
     */
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<ResultDO<Void>> handleNotRole(NotRoleException e) {
        log.warn("角色鉴权未通过, role: {}", e.getRole());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultDO.buildFailResult("NO_PERMISSION", "无权限访问"));
    }

    /**
     * 权限不满足
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ResultDO<Void>> handleNotPermission(NotPermissionException e) {
        log.warn("权限鉴权未通过, permission: {}", e.getPermission());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultDO.buildFailResult("NO_PERMISSION", "无权限访问"));
    }
}

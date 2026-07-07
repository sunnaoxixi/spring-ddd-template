package com.sunnao.spring.ddd.template.adaptor.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器（最后防线）
 * <p>
 * 各层仍按约定手动 catch 并转换为 ResultDO，正常业务流程不会走到这里；
 * 本处理器只兜住进入 Controller 之前（如 Sa-Token 鉴权、参数反序列化）
 * 以及漏网的未捕获异常，统一转换为 ResultDO，不向客户端外泄堆栈。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 未登录
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ResultDO<Void>> handleNotLogin(NotLoginException e) {
        log.warn("未登录访问被拦截, type: {}", e.getType());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResultDO.buildFailResult(ErrorCodeEnum.NOT_LOGIN));
    }

    /**
     * 角色不满足
     */
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<ResultDO<Void>> handleNotRole(NotRoleException e) {
        log.warn("角色鉴权未通过, role: {}", e.getRole());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultDO.buildFailResult(ErrorCodeEnum.NO_PERMISSION));
    }

    /**
     * 权限不满足
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ResultDO<Void>> handleNotPermission(NotPermissionException e) {
        log.warn("权限鉴权未通过, permission: {}", e.getPermission());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultDO.buildFailResult(ErrorCodeEnum.NO_PERMISSION));
    }

    /**
     * 请求体不可读（JSON 格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResultDO<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultDO.buildFailResult(ErrorCodeEnum.BAD_REQUEST, "请求体格式不正确"));
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResultDO<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("请求参数类型不匹配, name: {}, value: {}", e.getName(), e.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultDO.buildFailResult(ErrorCodeEnum.BAD_REQUEST, "请求参数类型不正确"));
    }

    /**
     * 资源不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResultDO<Void>> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("请求资源不存在, path: {}", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResultDO.buildFailResult(ErrorCodeEnum.NOT_FOUND));
    }

    /**
     * 兜底：未预期的系统异常（只打日志，不外泄堆栈）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultDO<Void>> handleException(Exception e) {
        log.error("未预期的系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR, "系统异常，请稍后重试"));
    }
}

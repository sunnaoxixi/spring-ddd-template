package com.sunnao.spring.ddd.template.adaptor.common;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.annotation.OperLog;
import com.sunnao.spring.ddd.template.common.context.CurrentUserContext;
import com.sunnao.spring.ddd.template.common.context.RequestContextUtils;
import com.sunnao.spring.ddd.template.common.event.DomainEventPublisher;
import com.sunnao.spring.ddd.template.common.filter.TraceIdFilter;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.log.event.OperLogEvent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.StringJoiner;

/**
 * 操作日志切面
 * <p>
 * 环绕 @OperLog 标注的 Controller 方法，采集 traceId、操作人、URI、参数摘要、
 * 结果码、耗时、IP 后发布 OperLogEvent，由 application 层监听器异步落库；
 * 采集/发布失败只记录日志，不影响业务主流程。
 * <p>
 * 参数摘要取自入参 toString（RequestDTO 已通过 @ToString(exclude) 屏蔽密码等敏感字段），
 * MultipartFile 等非 DTO 入参跳过，超长截断。
 */
@Slf4j
@Aspect
@Component
public class OperLogAspect {

    /**
     * 参数摘要最大长度（与 sys_oper_log.params 列宽对齐）
     */
    private static final int PARAMS_MAX_LENGTH = 2000;

    /**
     * 未捕获异常的结果码（正常业务失败为 ResultDO 错误码）
     */
    private static final String EXCEPTION_CODE = "EXCEPTION";

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        String resultCode = "SUCCESS";
        try {
            Object result = joinPoint.proceed();
            if (result instanceof ResultDO<?> resultDO && !resultDO.isSuccess()) {
                resultCode = StrUtil.blankToDefault(resultDO.getCode(), "FAIL");
            }
            return result;
        } catch (Throwable e) {
            resultCode = EXCEPTION_CODE;
            throw e;
        } finally {
            // 采集与发布失败不影响业务主流程
            try {
                publishEvent(joinPoint, operLog, resultCode, System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                log.error("发布操作日志事件失败, module: {}, action: {}",
                        operLog.module(), operLog.action(), e);
            }
        }
    }

    /**
     * 采集请求信息并发布操作日志事件
     * <p>
     * 操作人在方法执行后获取：login 场景执行前未登录、执行后已有会话，可正确记录。
     */
    private void publishEvent(ProceedingJoinPoint joinPoint, OperLog operLog,
                              String resultCode, long costMs) {
        String uri = null;
        HttpServletRequest request = RequestContextUtils.currentRequest();
        if (request != null) {
            uri = request.getRequestURI();
        }
        String ip = RequestContextUtils.getClientIp();

        domainEventPublisher.publish(new OperLogEvent(
                MDC.get(TraceIdFilter.TRACE_ID),
                CurrentUserContext.getUserId(),
                operLog.module(),
                operLog.action(),
                uri,
                summarizeParams(joinPoint.getArgs()),
                resultCode,
                costMs,
                ip));
    }

    /**
     * 参数摘要：DTO 走 toString（敏感字段已被 @ToString 排除），文件/流类入参跳过，超长截断
     */
    private String summarizeParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg instanceof MultipartFile file) {
                joiner.add("MultipartFile(name=" + file.getOriginalFilename()
                        + ", size=" + file.getSize() + ")");
                continue;
            }
            if (arg instanceof byte[] || arg instanceof HttpServletRequest) {
                continue;
            }
            joiner.add(String.valueOf(arg));
        }
        String params = joiner.toString();
        if (params.isEmpty()) {
            return null;
        }
        return params.length() > PARAMS_MAX_LENGTH ? params.substring(0, PARAMS_MAX_LENGTH) : params;
    }

}

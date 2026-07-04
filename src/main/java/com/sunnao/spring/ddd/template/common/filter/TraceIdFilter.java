package com.sunnao.spring.ddd.template.common.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * traceId 过滤器
 * <p>
 * 每个请求生成（或透传上游 X-Trace-Id 请求头的）traceId 写入 MDC，
 * 日志 pattern 通过 %X{traceId} 输出，响应头回写 X-Trace-Id 便于排查；
 * 请求结束时记录 method、uri、status、耗时。
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    /** traceId 的 MDC key，与 logback-spring.xml 中的 %X{traceId} 对应 */
    public static final String TRACE_ID = "traceId";

    /** traceId 请求/响应头名称 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (StrUtil.isBlank(traceId)) {
            traceId = IdUtil.fastSimpleUUID();
        }
        MDC.put(TRACE_ID, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - startTime;
            log.info("{} {} status: {}, cost: {}ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), cost);
            MDC.remove(TRACE_ID);
        }
    }
}

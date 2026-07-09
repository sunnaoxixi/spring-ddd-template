---
kind: logging_system
name: 基于 Logback 的链路追踪与操作日志体系
category: logging_system
scope:
    - '**'
source_files:
    - src/main/resources/logback-spring.xml
    - src/main/java/com/sunnao/spring/ddd/template/common/filter/TraceIdFilter.java
    - src/main/java/com/sunnao/spring/ddd/template/adaptor/common/OperLogAspect.java
    - src/main/java/com/sunnao/spring/ddd/template/adaptor/system/log/input/LogController.java
---

本仓库采用 Spring Boot 内置的 SLF4J + Logback 作为统一日志框架，结合 MDC 实现全链路 traceId 透传，并通过 AOP 切面将业务操作日志异步落库，形成控制台/文件输出与结构化持久化的双通道日志体系。

1. 日志框架与输出配置
- 使用 logback-spring.xml 定义根级别 INFO 输出，同时挂载 CONSOLE（彩色）与 FILE（按天+大小滚动、保留30天、总量不超过3GB）两个 Appender。
- 通过 springProperty 注入 spring.application.name 与 logging.file.path，默认应用名 spring-ddd-template，默认目录 logs/。
- 统一 LOG_PATTERN 包含 %X{traceId}，配合 TraceIdFilter 写入 MDC，使所有日志自动携带请求级链路标识。

2. 链路追踪（TraceId）
- TraceIdFilter 在请求进入时生成或透传 X-Trace-Id 请求头，写入 MDC key traceId，并在响应头回写同一值；请求结束时记录 method、uri、status、耗时。
- AsyncConfig 确保异步线程池继承父线程 MDC，保证异步场景下 traceId 不丢失。

3. 业务操作日志（OperLog）
- 通过 @OperLog 注解 + OperLogAspect 环绕拦截 Controller 方法，采集 traceId、操作人、URI、参数摘要、结果码、耗时、IP，发布 OperLogEvent。
- application 层监听器（OperLogListener / LoginLogListener）消费事件并异步落库到 sys_oper_log / sys_login_log 表，查询由 LogController 暴露分页接口。
- 参数摘要取自入参 toString（RequestDTO 已通过 @ToString(exclude) 屏蔽密码等敏感字段），MultipartFile/byte[]/HttpServletRequest 跳过，超长截断至 2000 字符。

4. 开发者约定
- 所有业务日志使用 Lombok @Slf4j 提供的 logger，避免直接 new Logger。
- 关键流程使用 info/warn/error 分级：正常流转用 info，可恢复异常用 warn，不可恢复异常用 error 并附带堆栈。
- 涉及用户输入的参数必须脱敏后再记录，禁止打印密码、token、完整请求体。
- 需要跨服务/跨线程关联日志时，确保调用链传递 X-Trace-Id 请求头，或在异步任务中手动 MDC.put(TRACE_ID, ...)。
- 新增持久化日志类型应遵循现有模式：domain.event -> application.listener -> infrastructure.repository，对外仅暴露只读查询接口。
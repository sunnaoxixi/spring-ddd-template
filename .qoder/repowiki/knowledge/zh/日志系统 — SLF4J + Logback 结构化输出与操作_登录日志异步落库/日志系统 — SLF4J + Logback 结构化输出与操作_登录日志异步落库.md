---
kind: logging_system
name: 日志系统 — SLF4J + Logback 结构化输出与操作/登录日志异步落库
slug: logging_system
category: logging_system
scope:
    - '**'
---

## 1. 使用的框架与工具
- 日志门面：SLF4J（通过 Lombok @Slf4j 注入）
- 实现：Logback，由 src/main/resources/logback-spring.xml 集中配置
- 链路追踪：自定义 TraceIdFilter 将 traceId 写入 MDC，pattern 中通过 %X{traceId} 输出
- 业务日志：基于 Spring AOP + 自定义注解 @OperLog 的切面采集，并通过 Spring 事件异步持久化到数据库

## 2. 核心文件与包
- 日志配置：src/main/resources/logback-spring.xml
- 链路追踪：common/filter/TraceIdFilter.java
- 操作日志注解与切面：common/annotation/OperLog.java、adaptor/common/OperLogAspect.java
- 异步监听器：application/system/log/listener/LoginLogListener.java、application/system/log/listener/OperLogListener.java
- 全局异常处理中的日志使用：adaptor/common/GlobalExceptionHandler.java
- 应用层示例日志：application/auth/scenario/AuthAppServiceImpl.java

## 3. 架构与约定
### 3.1 输出格式与级别
- 统一 pattern：%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-}] %logger{50} - %msg%n
- 控制台彩色 pattern：在 pattern 基础上对线程名、级别、traceId 做高亮
- Root level 为 INFO；业务异常/鉴权失败用 warn，未预期异常用 error，正常请求耗时用 info
- 输出目标：ConsoleAppender + RollingFileAppender（按天+大小滚动，保留 30 天，总量不超过 3GB）

### 3.2 TraceId 透传
- TraceIdFilter 作为最高优先级过滤器生成或透传 X-Trace-Id，放入 MDC key traceId，并在响应头回写该值
- 异步线程池由 AsyncConfig 负责 MDC 透传，确保异步消费者也能带上 traceId

### 3.3 操作日志（OperLog）
- 在写接口 Controller 方法上标注 @OperLog(module, action)，由 OperLogAspect 环绕采集：traceId、操作人、URI、参数摘要、结果码、耗时、IP
- 参数摘要是入参 toString() 拼接，跳过 MultipartFile、byte[]、HttpServletRequest，超长截断至 2000 字符
- 采集完成后发布 OperLogEvent，由 OperLogListener 异步消费并持久化，失败仅记录错误日志，不影响主流程
- 提供 HTTP 查询接口 LogController 分页查看两类日志

### 3.4 登录日志（LoginLog）
- 登录成功时 AuthAppServiceImpl 发布 LoginLogEvent，由 LoginLogListener 异步落库
- 与操作日志相同的“失败不阻塞主流程”策略

## 4. 开发者应遵循的规则
- 使用 Lombok @Slf4j 获取 logger，不要自行声明 Logger 实例
- 日志级别：业务校验失败/鉴权拒绝用 warn，不可恢复的系统异常用 error，常规信息用 info，避免滥用 debug/trace
- 涉及敏感字段（密码、token、完整请求体等）不得直接打印，需脱敏或使用 @ToString(exclude) 配合 OperLog 的参数摘要机制
- 需要记录操作审计的写接口必须加 @OperLog(module = "子域", action = "动作描述")，module/action 用于后续统计与检索
- 不要在业务领域层直接依赖 SLF4J 输出过多细节，关键路径可在 application/adaptor 层补充结构化日志
- 如需跨线程传递上下文，请确保通过 AsyncConfig 配置的线程池执行，以保证 MDC 透传
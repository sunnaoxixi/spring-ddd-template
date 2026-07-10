# Spring DDD Template 代码修复交接文档

> 本文档汇总本轮修复完成项、遗留项及后续建议，供后续接手同学继续推进。

---

## 1. 修复概览

本轮修复基于一次完整的代码审查报告，按 **P0（高优先级） / P1（高优先级） / M（中优先级） / L（低优先级）** 分级执行。**全部修复项已完成，全量验证通过。**

| 分级 | 计划项 | 完成 | 遗留 |
|------|--------|------|------|
| P0 | 3 | 3 | 0 |
| P1 | 2 | 2 | 0 |
| M | 6 | 6 | 0 |
| L | 1 | 1 | 0 |

---

## 2. 已完成的修复项

### 2.1 P0-1 禁用/删除用户后强制下线会话

- **问题**：用户被禁用或删除后，已签发的 token 仍能继续访问，存在越权风险。
- **修复**：在 `UserAppServiceImpl.changeUserStatus` 与 `deleteUser` 中，领域操作成功后调用 `StpUtil.kickout(userId)` 强制下线该用户全部会话；踢人失败仅记录日志，不影响主流程。
- **关键文件**：`@/src/main/java/com/sunnao/spring/ddd/template/application/system/user/scenario/UserAppServiceImpl.java`

### 2.2 P0-2 跨仓储事务统一

- **问题**：用户创建/删除时用户表与角色关联表操作分散在应用层和领域层，无法保证原子性。
- **修复**：
  - `UserRepository` 新增 `saveWithRoles(UserAggregate, List<Long>)` 与 `deleteWithRoles(Long userId, Long operatorId)` 两个组合方法；
  - `UserRepositoryImpl` 在方法上声明 `@Transactional(rollbackFor = Exception.class)`，内部统一调用 `save` + `roleRepository.saveUserRoles` 或 `delete` + 清空角色；
  - `UserDomainServiceImpl` 的 `createUser` / `deleteUser` 改用新组合方法；
  - 同步更新 `UserDomainServiceImplTest` 的 verify 断言。
- **关键文件**：
  - `@/src/main/java/com/sunnao/spring/ddd/template/domain/system/user/repository/UserRepository.java`
  - `@/src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/user/repository/UserRepositoryImpl.java`
  - `@/src/main/java/com/sunnao/spring/ddd/template/domain/system/user/service/UserDomainServiceImpl.java`
  - `@/src/test/java/com/sunnao/spring/ddd/template/domain/system/user/service/UserDomainServiceImplTest.java`

### 2.3 P0-3 统一仓储删除事务

- **问题**：部分仓储 `delete` 方法内部有多步写操作，但未加事务注解。
- **修复**：为以下 `delete` 方法补充 `@Transactional(rollbackFor = Exception.class)`：
  - `UserRepositoryImpl.delete`
  - `FileRepositoryImpl.delete`
  - `DictRepositoryImpl.deleteData` / `deleteType`
- **关键文件**：
  - `@/src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/user/repository/UserRepositoryImpl.java`
  - `@/src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/file/repository/FileRepositoryImpl.java`
  - `@/src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/dict/repository/DictRepositoryImpl.java`

### 2.4 P1-4 字典类型禁用生效

- **问题**：字典类型被禁用后，通过 `queryEnabledDataByTypeKey` 仍可能返回该类型下的字典数据。
- **修复**：在 `DictRepositoryImpl.queryEnabledDataByTypeKey` 回源数据库时，先校验字典类型状态为启用；类型不存在或已禁用时返回空列表。
- **关键文件**：`@/src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/dict/repository/DictRepositoryImpl.java`

### 2.5 P1-5 角色鉴权访问矩阵

- **当前方案**：项目已移除权限码功能，控制器统一使用 `@SaCheckRole`：
  - `UserController`、`RoleController`、`LogController`、`OnlineController`：仅 `admin`
  - `DictController`：写操作仅 `admin`，查询允许 `admin` 或 `user`
  - `FileController`：允许 `admin` 或 `user`
- **关键文件**：`@/src/main/java/com/sunnao/spring/ddd/template/adaptor/system/**/input/*Controller.java`

### 2.6 M-6 `JvmLevelLock` 锁注册表引用计数防泄漏

- **问题**：原 `ConcurrentHashMap<String, ReentrantLock>` 在锁 Key 高基数场景下只增不减，存在内存泄漏。
- **修复**：引入 `LockEntry` 包装 `ReentrantLock` 与 `AtomicInteger` 引用计数；`tryLock` 成功/失败及 `unlock` 时正确增减计数，计数归零后从注册表移除。
- **关键文件**：`@/src/main/java/com/sunnao/spring/ddd/template/common/lock/JvmLevelLock.java`

### 2.7 M-7 XFF 可信代理开关

- **问题**：`getClientIp()` 无条件读取 `X-Forwarded-For`，易被伪造。
- **修复**：
  - `RequestContextUtils` 增加 `trustXForwardedFor` 静态开关，默认 `false`；仅当开关开启时才读取 `X-Forwarded-For`；
  - 新增 `SecurityConfigure` 在 `@PostConstruct` 中读取 `app.security.trust-x-forwarded-for` 并注入开关。
- **关键文件**：
  - `@/src/main/java/com/sunnao/spring/ddd/template/common/context/RequestContextUtils.java`
  - `@/src/main/java/com/sunnao/spring/ddd/template/common/config/SecurityConfigure.java`
- **配置项**：`app.security.trust-x-forwarded-for: false`（默认关闭）。

### 2.8 M-8 登录防爆破

- **问题**：登录接口无失败次数限制，存在暴力破解风险。
- **修复**：
  - 新增 `LoginAttemptLimiter`，基于 Redis 固定窗口按 `邮箱+IP` 维度统计失败次数；
  - 新增 `ErrorCodeEnum.AUTH_LOCKED`；
  - `AuthAppServiceImpl.login` 在登录前校验是否被锁定，登录失败时计数，登录成功时清零。
- **关键文件**：
  - `@/src/main/java/com/sunnao/spring/ddd/template/common/security/LoginAttemptLimiter.java`
  - `@/src/main/java/com/sunnao/spring/ddd/template/common/result/ErrorCodeEnum.java`
  - `@/src/main/java/com/sunnao/spring/ddd/template/application/auth/scenario/AuthAppServiceImpl.java`
- **配置项**：
  - `app.security.login-max-failures: 5`
  - `app.security.login-lock-minutes: 15`

### 2.9 M-9 踢会话 token 不再走 URL path

- **问题**：`DELETE /api/system/online/tokens/{tokenValue}` 会把 token 写入访问日志，存在泄露风险。
- **修复**：接口改为 `DELETE /api/system/online/tokens`，token 通过请求体 `KickOnlineUserByTokenRequestDTO` 传入；同时该 DTO 的 `toString` 排除 token 字段。
- **关键文件**：`@/src/main/java/com/sunnao/spring/ddd/template/adaptor/system/online/input/OnlineController.java`

### 2.10 M-11 字典缓存事务提交后失效

- **问题**：字典写操作在事务内即清除缓存，并发读可能把旧数据重新写入缓存。
- **修复**：`DictRepositoryImpl` 中 `deleteType` / `deleteData` 的缓存清除延迟到事务提交后执行；通过 `TransactionSynchronizationManager.registerSynchronization` 注册 `afterCommit` 回调。
- **关键文件**：`@/src/main/java/com/sunnao/spring/ddd/template/infrastructure/system/dict/repository/DictRepositoryImpl.java`

---

## 3. 已完成的遗留修复项（第二轮）

以下项在第一轮修复中尚未完成，已在第二轮全部完成。

### 3.1 M-10 在线用户分页：过滤无效会话后再计数/分页

- **状态**：✅ 已完成。
- **修复**：在 `OnlineQueryAppServiceImpl.queryOnlineUserPage` 中，改为先全量扫描所有 token 键，逐个调用 `buildOnlineUser` 过滤无效会话，得到有效在线用户列表；再在此有效列表上做内存分页，`total` 使用有效列表的 `size()`，确保 total 与当页数据一致。
- **关键文件**：`@/src/main/java/com/sunnao/spring/ddd/template/application/system/online/scenario/OnlineQueryAppServiceImpl.java`

### 3.2 L-12 低风险快修

- **状态**：✅ 已完成。
- **修复明细**：
  - **硬编码错误码**：全局 `grep` 扫描确认所有 `buildFailResult` 调用均已使用 `ErrorCodeEnum`，无残留硬编码字符串错误码；
  - **PathVariable 命名**：全量核对所有控制器 `@PathVariable`，命名风格一致（camelCase），资源自身 ID 统一为 `"id"`，嵌套路由用户 ID 为 `"userId"`，无不一致项；
  - **线程池拒绝策略**：`AsyncConfig` 中 `ThreadPoolTaskExecutor` 原未显式设置拒绝策略（默认 `AbortPolicy`），已补充 `setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())`，队列满时由提交线程执行任务提供背压；
  - **生产环境 swagger**：`application-prod.yaml` 已添加 `springdoc.swagger-ui.enabled: false` 和 `springdoc.api-docs.enabled: false`，显式关闭 swagger。
- **关键文件**：
  - `@/src/main/java/com/sunnao/spring/ddd/template/common/config/AsyncConfig.java`
  - `@/src/main/resources/application-prod.yaml`

---

## 4. 关键配置汇总

新增或调整的配置项如下：

| 配置项 | 位置 | 默认值/建议 | 说明 |
|--------|------|-------------|------|
| `app.security.trust-x-forwarded-for` | `application.yaml` | `false` | 是否信任 XFF 头 |
| `app.security.login-max-failures` | `application.yaml` | `5` | 登录最大失败次数 |
| `app.security.login-lock-minutes` | `application.yaml` | `15` | 登录锁定分钟数 |
| `app.lock.type` | `application.yaml` | `redis` | 锁实现类型：`redis` / `jvm` |
| `springdoc.swagger-ui.enabled` / `api-docs.enabled` | `application-prod.yaml` | 建议 `false` | 生产关闭 swagger |

---

## 5. 验证状态

- **验证命令**：`./mvnw clean compile test`
- **当前状态**：✅ 全量验证通过。
  - 编译：268 个源文件编译成功
  - 测试：35 个测试用例，0 失败，0 错误，5 跳过
  - 耗时：约 6 秒

---

## 6. 后续建议与风险

1. **全量验证已完成**：`./mvnw clean compile test` 已通过（35 测试，0 失败）。
2. **M-10 已完成**：在线用户分页 total 与当页数据一致性问题已修复。
3. **L-12 已完成**：硬编码错误码、PathVariable 命名、CallerRunsPolicy 及 prod swagger 开关均已确认/修复。
4. **关注事务传播**：`UserRepositoryImpl.saveWithRoles` / `deleteWithRoles` 已在实现类加 `@Transactional`，调用方（`UserDomainServiceImpl`）无需再加事务，避免嵌套事务带来的回滚边界问题。
5. **缓存一致性**：M-11 已把字典缓存失效延迟到事务提交后，但仍建议在高并发场景下增加缓存读取的短暂降级或双删策略兜底。
6. **接口兼容性**：M-9 改变了 `OnlineController.kickByToken` 的 URL 与入参方式，前端/接口文档需同步更新；DTO 的 `toString` 已排除 token，但仍需确保日志框架不单独打印请求体字段。
7. **角色矩阵落地**：P1-5 已改为角色鉴权；新增接口时需明确标注允许访问的角色，并同步前端菜单可见性。

---

## 7. 核心变更文件清单

```
src/main/java/com/sunnao/spring/ddd/template/
├── application/
│   ├── auth/scenario/AuthAppServiceImpl.java
│   ├── system/online/scenario/OnlineQueryAppServiceImpl.java   # 待继续修复
│   └── system/user/scenario/UserAppServiceImpl.java
├── domain/system/user/
│   ├── repository/UserRepository.java
│   └── service/UserDomainServiceImpl.java
├── infrastructure/
│   ├── system/dict/repository/DictRepositoryImpl.java
│   ├── system/file/repository/FileRepositoryImpl.java
│   └── system/user/repository/UserRepositoryImpl.java
├── adaptor/system/
│   ├── dict/input/DictController.java
│   ├── file/input/FileController.java
│   ├── log/input/LogController.java
│   ├── online/input/OnlineController.java
│   ├── role/input/RoleController.java
│   └── user/input/UserController.java
├── common/
│   ├── config/SecurityConfigure.java
│   ├── context/RequestContextUtils.java
│   ├── lock/JvmLevelLock.java
│   ├── result/ErrorCodeEnum.java
│   └── security/LoginAttemptLimiter.java
└── test/java/...
    └── domain/system/user/service/UserDomainServiceImplTest.java
```

---

## 8. 结论

两轮修复已覆盖全部 P0/P1 安全与事务类问题、全部 M 级问题及 L 级低风险快修；全量编译与单元测试通过。项目安全性和事务一致性得到明显提升。

第一轮文档生成时间：2026-07-07
第二轮更新时间：2026-07-07

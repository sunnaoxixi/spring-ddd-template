# spring-ddd-template

基于六边形架构（Hexagonal Architecture）的 Spring Boot DDD 项目脚手架，内置用户、认证、RBAC、字典、操作日志、文件上传六个开箱即用的系统模块。

## 技术栈

| 组件 | 版本 / 说明 |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.x |
| MyBatis-Flex | ORM（`mybatis-flex-spring-boot4-starter`） |
| PostgreSQL | 17（数据库） |
| Redis | 7（会话 / 分布式锁 / 字典缓存） |
| Sa-Token | 认证鉴权（token 存 Redis，注解鉴权 `@SaCheckRole` / `@SaCheckPermission`） |
| Flyway | 数据库迁移（`db/migration/V1~V5`） |
| springdoc-openapi | API 文档（`/swagger-ui.html`） |
| Lombok / Hutool | 工具库 |

## 架构说明（六层）

遵循 [docs/rule/ddd/DDD.md](docs/rule/ddd/DDD.md) 六边形架构规范，调用顺序自外向内：

```plain
adaptor(input) → application → domain → repository 接口（infrastructure 实现）
                             → adaptor(output)（application 定义接口，依赖倒置）
```

| 层 | 包路径 | 职责 |
| --- | --- | --- |
| adaptor | `adaptor/{业务}/input`、`adaptor/{业务}/output` | Controller 接收请求；Output Adaptor 实现应用层定义的外部服务接口（如 `LocalFileStorage`） |
| application | `application/{业务}/scenario`、`assembler` | 场景编排、DTO ↔ 领域对象转换，不写业务规则 |
| client | `client/{业务}/req`、`res`、`model`、`enums` | 对外接口定义与自包含 DTO（禁止依赖 model 层） |
| domain | `domain/{业务}/model`、`service`、`repository` | 聚合根/实体承载业务逻辑，领域服务编排"锁 → 聚合根 → 持久化"，仓储只定义接口 |
| infrastructure | `infrastructure/{业务}/repository`、`mysql`、`converter` | 仓储实现、PO 与聚合根互转、缓存读写 |
| model | `model/{业务}` | 内部共享枚举/模型（client 层禁止依赖） |

关键编码约定（与现有代码保持一致）：

- **ResultDO 全链路不抛异常**：各层方法统一返回 `ResultDO`，内部 catch 后转错误码。
- **RequestDTO 自校验**：入参 DTO 覆写 `check()`，AppService 不写校验逻辑。
- **手写 Assembler / Converter**：application 层 Assembler 负责 DTO 转换，infrastructure 层 Converter 负责 PO 转换。
- **写模式标准流程**：领域服务先 `buildLock().tryLock()`，再加载/构建聚合根、调用聚合根业务方法、`repository.save()`，finally 释放锁。
- **审计字段自动填充**：PO 继承 `BasePO`，由 `MybatisFlexConfigure` 全局监听器填充 `createAt/updateAt/createBy/updateBy`（操作人取自 `CurrentUserContext`）。

## 快速开始

```bash
# 1. 启动本地依赖（PostgreSQL 17 + Redis 7）
docker compose up -d

# 2. 启动应用（默认 dev 环境，Flyway 自动建表并写入种子数据）
.\mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run            # Linux / macOS
```

- API 文档：<http://localhost:8080/swagger-ui.html>
- 种子管理员：`admin@example.com` / `admin123456`（首次登录后请修改）
- 登录方式：`POST /api/auth/login` 获取 token，后续请求携带请求头 `satoken: {tokenValue}`

### 多环境配置

| 环境 | 文件 | 说明 |
| --- | --- | --- |
| dev（默认） | `application-dev.yaml` | localhost 数据库/Redis，配合 docker-compose |
| prod | `application-prod.yaml` | 连接信息全部走环境变量（`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST` 等），`SPRING_PROFILES_ACTIVE=prod` 激活 |
| test | `src/test/resources/application-test.yaml` | 集成测试用，走 `TEST_PG_URL`、`TEST_REDIS_HOST` 等环境变量 |

## 已有模块

| 模块 | 路由前缀 | 说明 |
| --- | --- | --- |
| 认证 auth | `/api/auth` | 登录 / 登出 / 当前用户（Sa-Token 会话存 Redis） |
| 用户 user | `/api/system/users` | 用户 CRUD、启用/禁用（仅 admin），角色经 RBAC 关联 |
| 角色 role（RBAC） | `/api/system/roles` | 角色 CRUD、分配权限、给用户授角色（仅 admin）；`StpInterfaceImpl` 从 `sys_role` / `sys_permission` 及关联表读取角色与权限点 |
| 字典 dict | `/api/system/dicts` | 类型/数据 CRUD（admin）；按 typeKey 查启用数据走 Redis 缓存（登录可用），写操作自动失效缓存 |
| 操作日志 log | `/api/system/logs` | `@OperLog(module, action)` 注解 + 切面采集，异步落库；分页查询（仅 admin） |
| 文件 file | `/api/system/files` | multipart 上传 / 下载 / 分页查询（登录可用），删除仅 admin；`FileStorage` 抽象 + 本地磁盘实现，OSS 可扩展 |

数据库迁移脚本：`src/main/resources/db/migration/`（V1 用户、V2 RBAC、V3 操作日志、V4 字典、V5 文件）。

## 横切能力

- **全局异常处理**：`adaptor/common/GlobalExceptionHandler`（Sa-Token 401/403、400/404、500 兜底）。
- **traceId 链路**：`TraceIdFilter` 生成/透传 `X-Trace-Id`，logback pattern 输出，`AsyncConfig` 的 TaskDecorator 透传到异步线程。
- **领域事件**：`DomainEventPublisher`（common 接口）→ `SpringDomainEventPublisher`（infrastructure 实现），`@Async` 监听器消费（示例：`UserCreatedListener`、`OperLogListener`）。
- **分布式锁**：`LevelLock` 接口 + `RedisLevelLock`（SET NX PX + Lua 释放，默认）/ `JvmLevelLock`（单机），配置 `app.lock.type: redis|jvm` 切换。
- **操作日志**：写接口标 `@OperLog(module, action)` 即可自动记录（traceId、操作人、参数摘要、结果码、耗时、IP）。

## 测试

```bash
.\mvnw.cmd test
```

- **单元测试**：`UserAggregateTest`（聚合根业务规则）、`UserDomainServiceImplTest`（Mockito mock 仓储，验证写模式流程）——无需外部依赖。
- **集成测试**：`AuthLoginIntegrationTest`、`UserCrudIntegrationTest`、`SpringDddTemplateApplicationTests`——需要真实 PostgreSQL / Redis，通过环境变量注入连接信息，**缺失时自动跳过**：

```bash
# PowerShell 示例
$env:TEST_PG_URL = "jdbc:postgresql://your-host:5432/spring_ddd_template_test"
$env:TEST_PG_USERNAME = "postgres"
$env:TEST_PG_PASSWORD = "***"
$env:TEST_REDIS_HOST = "your-redis-host"
.\mvnw.cmd test
```

## 如何新增业务模块

以新增 `order` 模块为例（包路径按 `{层}/system/order/` 或自定义业务域组织）：

1. **迁移脚本**：`db/migration/V{n}__init_order.sql` 建表（含审计字段与 `deleted` 逻辑删除列）。
2. **model 层**（可选）：共享枚举，如 `model/system/order/OrderStatusEnum`。
3. **domain 层**：
   - `model/aggregate/OrderAggregate` + `model/entity/OrderEntity`（业务方法写在聚合根/实体上）；
   - `model/param/*Param`（领域入参）与 `OrderQuery`（查询条件）;
   - `repository/OrderRepository`（继承 `AggregateRepository`，声明 `buildLock`）；
   - `service/OrderDomainService(Impl)`（写模式："锁 → 聚合根 → 持久化"，返回 `ResultDO`）。
4. **infrastructure 层**：`mysql/po/OrderPO`（继承 `BasePO`）、`mysql/mapper/OrderMapper`、`converter/OrderConverter`、`repository/OrderRepositoryImpl`。
5. **client 层**：`OrderAppService` / `OrderQueryAppService` 接口 + `req`（覆写 `check()`）/ `res` / `model` DTO（自包含，不依赖 model 层）。
6. **application 层**：`scenario/OrderAppServiceImpl`（校验 → Assembler 转 Param → 领域服务 → 组装响应）、`assembler/OrderAssembler`。
7. **adaptor 层**：`input/OrderController`（写接口标 `@OperLog`，按需 `@SaCheckRole` / `@SaCheckPermission`）；调用第三方服务时在 application 层定义接口、`output/` 提供实现。
8. **权限点**（可选）：在 RBAC 迁移或新迁移脚本中插入 `sys_permission` 种子并关联角色。
9. **测试**：领域层单测（Mockito）+ 集成测试（`@EnabledIfEnvironmentVariable` 条件跳过）。

## 项目结构

```plain
src/main/java/com/sunnao/spring/ddd/template/
├── adaptor/          # 防腐层：input Controller / output 外部服务实现 / common 全局异常与切面
├── application/      # 应用层：scenario 场景编排 / assembler 转换 / listener 事件消费
├── client/           # 对外接口定义层：AppService 接口 + req/res/model DTO
├── domain/           # 领域层：聚合根、实体、领域服务、仓储接口、领域事件
├── infrastructure/   # 基础设施层：仓储实现、PO、Mapper、Converter
├── model/            # 内部共享模型（枚举等）
└── common/           # 框架基建：ResultDO、锁、事件、上下文、配置、注解、过滤器
```

# spring-ddd-template

基于六边形架构（Hexagonal Architecture）的 Spring Boot DDD 项目脚手架，内置认证、用户、RBAC、字典、日志、文件上传、在线用户等开箱即用的系统模块。

## 技术栈

| 组件                | 版本 / 说明                                                        |
|-------------------|----------------------------------------------------------------|
| Java              | 25                                                             |
| Spring Boot       | 4.1.0                                                          |
| MyBatis-Flex      | 1.11.8（`mybatis-flex-spring-boot4-starter`）                    |
| PostgreSQL        | 17（数据库，docker-compose 默认镜像）                                      |
| Redis             | 7（会话 / 分布式锁 / 字典缓存 / 登录失败限制）                                  |
| Sa-Token          | 1.45.0，认证鉴权（token 存 Redis，使用 `@SaCheckRole` 按角色鉴权）              |
| Flyway            | 数据库迁移（`db/migration/V1~V8`）                                    |
| springdoc-openapi | 3.0.3，API 文档（`/swagger-ui.html`）                             |
| AWS SDK S3        | 2.31.63，S3 兼容对象存储                                            |
| Lombok / Hutool / MapStruct | 工具库 / 对象转换                                            |

## 架构说明（六层）

遵循 [docs/rule/ddd/DDD.md](docs/rule/ddd/DDD.md) 六边形架构规范，调用顺序自外向内：

```plain
adaptor(input) → application → domain → repository 接口（infrastructure 实现）
                             → adaptor(output)（application 定义接口，依赖倒置）
```

| 层              | 包路径                                                  | 职责                                                                  |
|----------------|------------------------------------------------------|---------------------------------------------------------------------|
| adaptor        | `adaptor/{业务}/input`、`adaptor/{业务}/output`           | Controller 接收请求；Output Adaptor 实现应用层定义的外部服务接口（如 `S3FileStorage`）    |
| application    | `application/{业务}/scenario`、`assembler`              | 场景编排、DTO ↔ 领域对象转换，不写业务规则                                            |
| client         | `client/{业务}/req`、`res`、`model`、`enums`              | 对外接口定义与自包含 DTO（禁止依赖 model 层）                                        |
| domain         | `domain/{业务}/model`、`service`、`repository`           | 聚合根/实体承载业务逻辑，领域服务编排"锁 → 聚合根 → 持久化"，仓储只定义接口                          |
| infrastructure | `infrastructure/{业务}/repository`、`mysql`、`converter` | 仓储实现、PO 与聚合根互转、缓存读写                                                 |
| model          | `model/{业务}`                                         | 内部共享枚举/模型（client 层禁止依赖）                                             |

关键编码约定（与现有代码保持一致）：

- **ResultDO 全链路不抛异常**：各层方法统一返回 `ResultDO`，内部 catch 后转错误码。
- **RequestDTO 自校验**：入参 DTO 覆写 `check()`，AppService 不写校验逻辑。
- **手写 Assembler / Converter**：application 层 Assembler 负责 DTO 转换，infrastructure 层 Converter 负责 PO 转换。
- **写模式标准流程**：领域服务先 `buildLock().tryLock()`，再加载/构建聚合根、调用聚合根业务方法、`repository.save()`，finally
  释放锁。
- **审计字段自动填充**：PO 继承 `BasePO`，由 `MybatisFlexConfigure` 全局监听器填充 `createAt/updateAt/createBy/updateBy`
  （操作人取自 `CurrentUserContext`）。

## 快速开始

### 一键改包（基于模板创建新项目）

```bash
# 交互模式
./rename-project.sh

# 或直接传参：<new-groupId> <new-artifactId> [new-package]
./rename-project.sh com.acme order-center
# 包名默认 = groupId + artifactId 横线转点（com.acme.order.center），也可第三个参数显式指定
```

脚本会自动替换 `pom.xml` 坐标、Java 包名与源码目录、启动类名、`spring.application.name`、数据库名（`application-*.yaml` /
`docker-compose.yaml`）及 README 中的引用（Windows 可在 Git Bash 中运行）。

```bash
# 1. 复制本地环境变量模板（macOS / Linux）
cp .env.example .env

# 2. 编辑 .env，填写 S3_ENDPOINT、S3_ACCESS_KEY、S3_SECRET_KEY、S3_BUCKET 等必填配置

# 3. 启动本地依赖（PostgreSQL 17 + Redis 7）
docker compose up -d

# 4. 启动应用（默认 dev profile，Flyway 自动建表并写入种子数据）
./mvnw spring-boot:run            # Linux / macOS
```

Windows 可先执行 `copy .env.example .env`，再用 `.\mvnw.cmd spring-boot:run` 启动。

- API 文档：<http://localhost:8080/swagger-ui.html>
- 种子管理员：`admin@example.com` / `admin123456`（首次登录后请修改）
- 登录方式：`POST /api/auth/login` 获取 token，后续请求携带请求头 `sa-token: {tokenValue}`

### 多环境配置

| 环境      | 文件                                         | 说明                                                                                                |
|---------|--------------------------------------------|---------------------------------------------------------------------------------------------------|
| dev（默认） | `application.yaml` + `.env`                | `spring.profiles.active` 默认 `dev`；`.env.example` 提供本地 PostgreSQL/Redis 默认值，S3 连接信息需自行填写 |
| prod    | `application.yaml` + `application-prod.yaml` | 连接信息走环境变量（`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST` 等）；`application-prod.yaml` 关闭 OpenAPI/Swagger |
| test    | `src/test/resources/application-test.yaml` | 集成测试用，走 `TEST_PG_URL`、`TEST_REDIS_HOST` 等环境变量；缺失时相关集成测试自动跳过 |

## 已有模块

| 模块            | 路由前缀                | 说明                                                                                           |
|---------------|---------------------|----------------------------------------------------------------------------------------------|
| 认证 auth       | `/api/auth`         | 登录 / 注册 / 登出 / 当前用户；Sa-Token 会话存 Redis，登录成功写入 token-session 附加信息，登录失败有 Redis 限流 |
| 用户 user       | `/api/system/users` | 用户 CRUD、启用/禁用、逻辑删除；仅 `admin` 角色可访问，角色经用户-角色关联表维护 |
| 角色 role       | `/api/system/roles` | 角色 CRUD、给用户授角色；仅 `admin` 角色可访问，`StpInterfaceImpl` 从角色表读取用户角色 |
| 字典 dict       | `/api/system/dicts` | 类型/数据 CRUD、按 typeKey 查询启用数据；写操作仅 `admin`，查询允许 `admin` 或 `user`，查询走 Redis 缓存 |
| 系统日志 log     | `/api/system/logs`  | 操作日志和登录日志分页查询；仅 `admin` 角色可访问，操作日志和登录日志均异步落库 |
| 文件 file       | `/api/system/files` | multipart 上传、下载、删除、分页查询；允许 `admin` 或 `user` 角色访问，仅支持配置的 S3 兼容对象存储 |
| 在线用户 online  | `/api/system/online` | 在线会话分页查询、按 token 踢下线、按用户踢全部会话；按 `admin` 角色鉴权 |

数据库迁移脚本：`src/main/resources/db/migration/`（V1 用户、V2 角色关系、V3 操作日志、V4 字典、V5 文件、V6 登录日志、V7 移除权限码表、V8 移除文件存储类型字段）。

### 文件存储

文件上传、下载和删除统一通过 `S3FileStorage` 完成。该实现是 application 层 `FileStorage` 接口的唯一 Output Adaptor，
使用 AWS SDK v2 通用 S3 协议客户端，兼容阿里云 OSS、腾讯云 COS、MinIO、七牛云 Kodo 等对象存储。

S3 连接配置走环境变量（`app.file.s3.*`，密钥不落盘）。`endpoint`、`region`、访问密钥和 `bucket` 均为必填项，
任一配置缺失时应用启动失败：

| 环境变量                  | 说明                            | 阿里云 OSS 示例                              | 腾讯云 COS 示例                                | MinIO 示例               |
|-----------------------|-------------------------------|----------------------------------------|------------------------------------------|------------------------|
| `S3_ENDPOINT`         | 服务端点                          | `https://oss-cn-hangzhou.aliyuncs.com` | `https://cos.ap-shanghai.myqcloud.com`   | `http://127.0.0.1:9000` |
| `S3_REGION`           | 区域（无区域概念的服务商保持默认 `us-east-1`） | `oss-cn-hangzhou`                      | `ap-shanghai`                             | `us-east-1`            |
| `S3_ACCESS_KEY`       | 密钥 ID                         | AccessKeyId                            | SecretId                                  | Access Key             |
| `S3_SECRET_KEY`       | 密钥 Secret                     | AccessKeySecret                        | SecretKey                                 | Secret Key             |
| `S3_BUCKET`           | 存储桶（需预先创建）                    | —                                      | —                                         | —                      |
| `S3_PATH_STYLE_ACCESS` | 路径风格访问                        | `false`（虚拟主机风格）                        | `false`（虚拟主机风格）                          | `true`                 |

文件元数据仅保存 S3 对象 key，不再保存或切换存储类型。

已有环境升级前，需先将历史本地文件按 `sys_file.path` 的原值作为对象 key 上传到配置的 S3 存储桶，再部署应用并执行 V8 迁移。

## 横切能力

- **全局异常处理**：`adaptor/common/GlobalExceptionHandler`（Sa-Token 401/403、400/404、500 兜底）。
- **traceId 链路**：`TraceIdFilter` 生成/透传 `X-Trace-Id`，logback pattern 输出，`AsyncConfig` 的 TaskDecorator 透传到异步线程。
- **领域事件**：`DomainEventPublisher`（common 接口）→ `SpringDomainEventPublisher`（infrastructure 实现），`@Async` 监听器消费（示例：
  `UserCreatedListener`、`OperLogListener`）。
- **分布式锁**：`LevelLock` 接口 + `RedisLevelLock`（SET NX PX + Lua 释放，默认）/ `JvmLevelLock`（单机），配置
  `app.lock.type: redis|jvm` 切换。
- **操作日志**：写接口标 `@OperLog(module, action)` 即可自动记录（traceId、操作人、参数摘要、结果码、耗时、IP）。

## 测试

```bash
./mvnw test
```

- **单元测试**：`UserAggregateTest`、`FileAggregateTest`（聚合根业务规则）、`UserDomainServiceImplTest`（Mockito mock 仓储，验证写模式流程）、`S3FileStorageTest`（mock S3 client，验证 S3 存储适配器）——无需外部依赖。
- **集成测试**：`AuthLoginIntegrationTest`、`AuthRegisterIntegrationTest`、`UserCrudIntegrationTest`、`SpringDddTemplateApplicationTests`——需要真实
  PostgreSQL / Redis，通过环境变量注入连接信息，**缺失时自动跳过**：

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
4. **infrastructure 层**：`mysql/po/OrderPO`（继承 `BasePO`）、`mysql/mapper/OrderMapper`、`converter/OrderConverter`、
   `repository/OrderRepositoryImpl`。
5. **client 层**：`OrderAppService` / `OrderQueryAppService` 接口 + `req`（覆写 `check()`）/ `res` / `model` DTO（自包含，不依赖
   model 层）。
6. **application 层**：`scenario/OrderAppServiceImpl`（校验 → Assembler 转 Param → 领域服务 → 组装响应）、
   `assembler/OrderAssembler`。
7. **adaptor 层**：`input/OrderController`（写接口标 `@OperLog`，使用 `@SaCheckRole` 声明允许访问的角色）；调用第三方服务时在
   application 层定义接口、`output/` 提供实现。
8. **测试**：领域层单测（Mockito）+ 集成测试（`@EnabledIfEnvironmentVariable` 条件跳过）。

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

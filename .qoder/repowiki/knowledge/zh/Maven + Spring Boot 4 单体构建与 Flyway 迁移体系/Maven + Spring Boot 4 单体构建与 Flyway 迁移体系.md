---
kind: build_system
name: Maven + Spring Boot 4 单体构建与 Flyway 迁移体系
slug: build_system
category: build_system
scope:
    - '**'
---

## 1. 构建系统与工具链

- **构建工具**：Maven（通过 Maven Wrapper 3.9.16 锁定版本，`.mvn/wrapper/maven-wrapper.properties`），项目为单模块 Spring Boot 应用。
- **Java 版本**：25，父 POM 继承 `spring-boot-starter-parent:4.1.0`，统一依赖管理与插件行为。
- **打包产物**：由 `spring-boot-maven-plugin` 生成可执行 jar，Lombok 在打包时排除。

## 2. 核心构建配置

- **注解处理器**：`maven-compiler-plugin` 显式注册 Lombok、MapStruct、MyBatis-Flex 的 processor，保证编译期代码生成稳定。
- **测试依赖**：`spring-boot-starter-webmvc-test`、`spring-boot-starter-data-redis-test` 提供集成测试基础。
- **数据库驱动**：PostgreSQL JDBC 以 `runtime` scope 引入，仅运行期可见。

## 3. 依赖管理策略

- 所有第三方库版本集中在 `<properties>` 中声明（MapStruct 1.6.3、MyBatis-Flex 1.11.8、Sa-Token 1.45.0、SpringDoc 3.0.3、AWS SDK 2.31.63）。
- AWS S3 SDK 排除 Netty NIO 客户端，仅保留 Apache HttpClient 同步实现，避免运行时冲突。
- Sa-Token 使用 Spring Boot 4 专用 starter (`sa-token-spring-boot4-starter`)，并配合 RedisTemplate 存储 token。

## 4. 数据库迁移（Flyway）

- 启用 `spring-boot-starter-flyway` + `flyway-database-postgresql`，脚本位于 `src/main/resources/db/migration/`，按 `V1__init_sys_user.sql` ~ `V6__init_sys_login_log.sql` 顺序执行。
- 配置 `baseline-on-migrate=true`，兼容已存在表结构的项目，新库从 V1 开始迁移。
- 本地开发通过 `docker-compose.yaml` 启动 PostgreSQL 17 + Redis 7，应用启动时自动完成建表。

## 5. 环境配置与 Profile

- 主配置 `application.yaml` 支持 `.env` 文件导入（`optional:file:.env[.properties]`），所有敏感参数通过环境变量注入。
- Profile 激活由 `SPRING_PROFILES_ACTIVE` 控制，默认 `dev`；另有 `application-prod.yaml` 和测试用 `application-test.yaml`。
- 关键外部依赖地址均支持占位符：DB_HOST/PORT/NAME、REDIS_*、S3_* 等。

## 6. 本地开发与容器化

- **一键启动依赖服务**：`docker compose up -d` 拉起 PostgreSQL 与 Redis，数据持久化到命名卷。
- **健康检查**：PostgreSQL 使用 `pg_isready`，Redis 使用 `redis-cli ping`，确保服务就绪后再启动应用。
- **无 Dockerfile**：当前仓库未包含容器镜像构建配置，部署需自行补充 Dockerfile 或使用云原生平台。

## 7. 开发者规范

- 新增依赖必须在 `<properties>` 中声明版本号，禁止在 `<dependencies>` 中硬编码版本。
- 数据库变更必须新增 `Vn__xxx.sql` 迁移脚本，保持幂等性，遵循现有命名约定。
- 敏感配置一律走环境变量或 `.env`，禁止写死在 YAML 中。
- 使用 Maven Wrapper 执行构建，避免团队间 Maven 版本差异导致的行为不一致。
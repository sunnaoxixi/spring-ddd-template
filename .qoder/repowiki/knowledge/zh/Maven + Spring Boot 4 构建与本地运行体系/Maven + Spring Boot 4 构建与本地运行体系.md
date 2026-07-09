---
kind: build_system
name: Maven + Spring Boot 4 构建与本地运行体系
category: build_system
scope:
    - '**'
source_files:
    - pom.xml
    - .mvn/wrapper/maven-wrapper.properties
    - src/main/resources/application.yaml
    - src/main/resources/application-prod.yaml
    - docker-compose.yaml
---

## 构建系统概览
本项目采用**单模块 Maven 工程**，以 `spring-boot-starter-parent:4.1.0` 作为父 POM，统一依赖版本管理；通过 Maven Wrapper（3.9.16）保证团队构建环境一致。无 Makefile、Dockerfile、CI 流水线或发布脚本，属于轻量级脚手架。

## 核心构建文件与职责
- `pom.xml`：声明 Java 25、Spring Boot 4.1.0、MyBatis-Flex 1.11.8、Sa-Token 1.45.0、MapStruct 1.6.3、AWS SDK v2 S3、Flyway、springdoc-openapi 等依赖；配置 `maven-compiler-plugin` 的注解处理器链（Lombok → MapStruct → MyBatis-Flex），并通过 `spring-boot-maven-plugin` 打包可执行 JAR。
- `.mvn/wrapper/maven-wrapper.properties`：锁定 Maven 分发为 3.9.16，distributionType=only-script，避免强制安装全局 Maven。
- `src/main/resources/application.yaml`：通过 `spring.config.import: optional:file:.env[.properties]` 加载根目录 `.env` 环境变量，再按 `SPRING_PROFILES_ACTIVE` 激活 profile；默认启用 Flyway 迁移（`classpath:db/migration`，`baseline-on-migrate=true` 兼容已有库）。
- `src/main/resources/application-prod.yaml`：生产环境关闭 swagger-ui 与 openapi 文档。
- `docker-compose.yaml`：仅编排开发期 PostgreSQL 17 与 Redis 7，提供 healthcheck 与持久卷，应用启动时由 Flyway 自动建表。
- `src/main/resources/db/migration/V*.sql`：Flyway 版本化脚本，按 V1~V6 顺序初始化用户、RBAC、操作日志、字典、文件、登录日志表。

## 构建与运行约定
| 阶段 | 命令/方式 | 说明 |
|---|---|---|
| 编译+测试+打包 | `./mvnw clean package -DskipTests` | 生成 `target/spring-ddd-template-0.0.1-SNAPSHOT.jar` |
| 本地运行 | `java -jar target/*.jar` 或 `./mvnw spring-boot:run` | 读取 `.env` 与 `application-{profile}.yaml` |
| 依赖服务 | `docker compose up -d` | 拉起 Postgres + Redis，端口 5432/6379 |
| 数据库迁移 | 应用启动时自动执行 Flyway | `V1__init_sys_user.sql` … `V6__init_sys_login_log.sql` |

## 关键设计决策
1. **Spring Boot 4 适配**：显式引入 `spring-boot-starter-jdbc`（Boot 4 不再默认装配 JDBC）、使用 `mybatis-flex-spring-boot4-starter` 与 `sa-token-spring-boot4-starter`，并排除 AWS SDK 的 Netty 异步传输层以仅保留同步客户端。
2. **注解处理器顺序**：Lombok 先于 MapStruct，MapStruct 先于 MyBatis-Flex processor，确保生成的代码能被后续处理器消费。
3. **配置外置**：所有敏感信息（DB、Redis、S3 密钥）通过 `.env` 注入，不入库；`app.file.storage-type`、`app.lock.type` 等运行时开关通过 profile 切换。
4. **无 CI/制品仓库**：当前脚手架未集成 GitHub Actions、Jenkins、SonarQube 或 Docker 镜像构建，发布流程尚未定义。

## 开发者应遵循的规则
- 新增依赖一律在 `pom.xml` 的 `<properties>` 中声明版本号，禁止硬编码。
- 修改数据库结构必须新增 `Vn__xxx.sql` 迁移脚本，保持幂等与回滚友好。
- 环境变量名与 `application.yaml` 中的 `${VAR:default}` 保持一致，并在 `.env.example` 中补充示例。
- 本地调试优先使用 `docker compose up -d` 拉起依赖，不要直接连外部数据库。
- 如需容器化部署，应在项目根目录新增 `Dockerfile` 并配合多阶段构建，同时更新 `docker-compose.yaml` 的 service 定义。
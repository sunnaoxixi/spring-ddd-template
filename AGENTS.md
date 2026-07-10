# 仓库指南

## 项目结构与模块组织

本项目是基于 Spring Boot 4 和 Java 25 的单模块 Maven 工程。生产代码位于 `src/main/java/com/sunnao/spring/ddd/template/`，按六边形架构与 DDD 分为 `adaptor`、`application`、`client`、`domain`、`infrastructure`、`model` 和 `common` 层。新增业务能力时，应在对应层建立相同的业务包，例如 `domain/system/order` 和 `adaptor/system/order`。

应用配置位于 `src/main/resources/`。Flyway 脚本存放在 `src/main/resources/db/migration/`，命名遵循 `V{n}__description.sql`，例如 `V7__init_order.sql`。测试代码位于 `src/test/java/`，并与生产代码包结构保持一致；集成测试统一放在 `.../integration` 下。架构和部署文档位于 `docs/`。不要编辑自动生成的 `target/` 内容。

## 开发规范（必须遵守）

所有开发、重构和代码评审都必须遵守 [`docs/rule/`](docs/rule/) 中的规范。开始编码前，先阅读 [`docs/rule/ddd/DDD.md`](docs/rule/ddd/DDD.md) 了解整体架构、依赖方向和开发模式，再通过 [`docs/rule/ddd/README.md`](docs/rule/ddd/README.md) 找到改动所涉及层的详细规范。

跨层改动应逐项核对对应的 Domain、Application、Adaptor、Infrastructure、Client 和 Model 层文档，确保职责划分、包位置、命名、依赖关系和数据转换方式符合要求。提交评审时，如实现与规范存在差异，必须在合并请求中说明原因，不得无说明地绕过规范。

## 构建、测试与开发命令

- `cp .env.example .env`：准备本地配置；禁止提交真实凭据。
- `docker compose up -d`：启动 PostgreSQL 17 和 Redis 7。
- `./mvnw spring-boot:run`：使用默认 dev 配置运行应用。
- `./mvnw test`：运行单元测试，以及环境条件满足时的集成测试。
- `./mvnw clean package`：清理并重新编译、测试，生成可执行 JAR。

Windows 环境请使用对应的 `mvnw.cmd` 命令。

## 编码风格与命名约定

Java 代码使用四空格缩进；包名使用小写，类型使用 PascalCase，成员使用 camelCase。项目未配置格式化或 lint 插件，因此应遵循相邻代码的风格并保持 import 有序。沿用现有类名后缀：`Controller`、`AppServiceImpl`、`DomainServiceImpl`、`Aggregate`、`RepositoryImpl`、`Mapper`、`PO`、`RequestDTO` 和 `ResponseDTO`。

请求校验应放在 DTO 的 `check()` 方法中，业务规则放在聚合根或领域服务中，流程编排放在应用服务中，数据库相关实现放在 `infrastructure` 层。保持项目现有的 `ResultDO` 错误处理流程，以及 Assembler/Converter 转换模式。

## 测试规范

测试使用 JUnit 5 和 Mockito。测试类命名为 `*Test.java`，测试方法应清晰描述行为，例如 `createShouldRejectBlankEmail`。聚合根和领域服务优先编写隔离的单元测试。`@SpringBootTest` 集成测试依赖 `TEST_PG_URL` 和 `TEST_REDIS_HOST`，缺少这些变量时会跳过。项目未设置数值化覆盖率门槛，但应覆盖变更涉及的成功、校验、加锁及持久化失败路径。

## 提交与合并请求规范

历史提交同时使用简洁的祈使句主题和 Conventional Commits 前缀，例如 `feat(auth): ...`、`fix(config): ...` 和 `docs: ...`。推荐采用 `type(scope): 祈使句摘要`，每个提交只处理一个明确主题，并注明数据库迁移或配置变更。

合并请求应包含简短的问题与解决方案说明、相关 Issue、测试命令及结果。涉及数据库、Docker 或环境配置时，还需说明部署和回滚方式。仅在 UI 或文档渲染发生变化时提供截图。

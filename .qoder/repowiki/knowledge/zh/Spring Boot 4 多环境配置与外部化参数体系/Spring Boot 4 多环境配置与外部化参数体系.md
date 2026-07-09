---
kind: configuration_system
name: Spring Boot 4 多环境配置与外部化参数体系
category: configuration_system
scope:
    - '**'
source_files:
    - src/main/resources/application.yaml
    - src/main/resources/application-prod.yaml
    - src/test/resources/application-test.yaml
    - .env.example
    - src/main/java/com/sunnao/spring/ddd/template/common/config/SaTokenConfigure.java
    - src/main/java/com/sunnao/spring/ddd/template/common/config/MybatisFlexConfigure.java
    - src/main/java/com/sunnao/spring/ddd/template/common/config/OpenApiConfig.java
    - src/main/java/com/sunnao/spring/ddd/template/common/config/SecurityConfigure.java
    - src/main/java/com/sunnao/spring/ddd/template/common/config/AsyncConfig.java
---

## 1. 采用的配置系统与方法
- 基于 Spring Boot 4 的 `application.yaml` + Profile 机制，通过 `spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}` 实现开发/测试/生产等多环境切换。
- 使用 `spring.config.import=optional:file:.env[.properties]` 从项目根目录加载 `.env` 文件（兼容 KEY=VALUE 与 properties），作为本地环境变量源；所有敏感或易变参数统一走 `${VAR:默认值}` 占位符注入。
- 通过 `@Configuration` + `WebMvcConfigurer` / `MyBatisFlexCustomizer` 等 Java Config 类完成框架级装配（Sa-Token、MyBatis-Flex、OpenAPI、安全拦截器等），业务配置仍集中在 YAML 中，避免硬编码。
- 数据库迁移由 Flyway 驱动，`spring.flyway.enabled=true` + `locations=classpath:db/migration`，并开启 `baseline-on-migrate` 兼容已有库。

## 2. 关键文件与包
- 配置文件
  - `src/main/resources/application.yaml`：全局默认配置，包含 datasource、redis、servlet multipart、flyway、mybatis-flex、sa-token、springdoc、app.* 自定义属性。
  - `src/main/resources/application-prod.yaml`：生产环境覆盖开关（关闭 swagger-ui 与 api-docs）。
  - `src/test/resources/application-test.yaml`：集成测试专用配置，全部依赖 `TEST_PG_*` / `TEST_REDIS_*` 环境变量。
  - `.env.example`：本地开发环境变量模板，定义 DB_*、REDIS_*、S3_* 等键。
- Java 配置类（`common/config` 包）
  - `SaTokenConfigure.java`：注册 Sa-Token 拦截器，除 `/api/auth/**` 外要求登录态，放行 OpenAPI 路径。
  - `MybatisFlexConfigure.java`：为继承 `BasePO` 的实体注册插入/更新审计监听器，自动填充 createAt/updateAt/createBy/updateBy。
  - `AsyncConfig.java`、`OpenApiConfig.java`、`SecurityConfigure.java`：异步线程池、OpenAPI 文档与安全相关配置入口。
- 启动类
  - `SpringDddTemplateApplication.java`：应用入口，配合 `@SpringBootApplication` 扫描 `common.config` 下的配置类。

## 3. 架构与约定
- 分层与职责
  - **YAML 层**：仅承载“可外部化的参数”，不写业务逻辑。按环境拆分 profile，生产环境通过 `application-prod.yaml` 关闭调试能力。
  - **.env 层**：存放本地开发所需的环境变量，被 `spring.config.import` 以 optional 方式引入，缺失时不影响启动。
  - **Java Config 层**：只负责把 YAML 中的值装配到对应组件（如 Sa-Token、MyBatis-Flex、OpenAPI），并通过 `@Configuration` 暴露 Bean。
- 命名与环境隔离
  - 开发/测试/生产共用同一份 key 空间，通过 `SPRING_PROFILES_ACTIVE` 切换；测试环境使用独立 `application-test.yaml` 并强制依赖 `TEST_*` 前缀的环境变量，缺失时集成测试自动跳过。
- 安全与机密管理
  - 所有密码、密钥（DB_PASSWORD、REDIS_PASSWORD、S3_ACCESS_KEY/S3_SECRET_KEY）一律通过环境变量注入，不在仓库中落盘；`.env.example` 仅提供键名与示例值。
- 扩展点
  - `app.lock.type`、`app.file.storage-type` 等 `app.*` 前缀的配置项用于在运行时切换锁与文件存储实现，后续新增存储后端只需提供新实现并按此键选择。

## 4. 开发者应遵循的规则
1. **新增配置项**：优先放入 `application.yaml` 的对应分组（如 `app.*`、`sa-token.*`、`springdoc.*`），并提供合理的默认值与注释说明。
2. **敏感信息**：一律通过环境变量注入，不要写入任何 YAML 或代码；如需本地调试，在根目录创建 `.env` 文件，参考 `.env.example` 的键名。
3. **Profile 使用**：不同环境的差异通过 `application-{profile}.yaml` 覆盖，禁止在代码中判断 profile 分支；测试环境使用 `application-test.yaml` 并依赖 `TEST_*` 环境变量。
4. **Java Config 边界**：`common/config` 下只放框架装配逻辑，不在此处读取业务配置；业务配置通过 `@Value` / `@ConfigurationProperties` 注入到领域或服务层。
5. **Flyway 迁移**：新增表结构必须编写 Vx__xxx.sql 脚本放入 `resources/db/migration`，保持版本递增且幂等。
6. **审计字段**：持久化对象继承 `BasePO`，由 MyBatis-Flex 监听器自动填充审计字段，不要在业务代码中手动赋值（除非显式覆盖）。
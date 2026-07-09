---
kind: configuration_system
name: Spring Boot 配置体系与环境变量注入
slug: configuration_system
category: configuration_system
scope:
    - '**'
---

## 1. 使用的系统与工具
- Spring Boot 原生 application.yaml + optional:file:.env[.properties] 导入机制，实现本地开发环境变量文件与 YAML 配置的融合。
- 通过 spring.config.import 引入项目根目录的 .env（不存在时跳过），配合 ${VAR:default} 占位符完成运行时参数注入。
- 使用 @Value("${...}") 在 Java 类中直接读取应用自定义配置项。
- 多环境通过 spring.profiles.active 切换：默认 dev，生产环境使用 application-prod.yaml 关闭 Swagger/OpenAPI。
- 测试环境独立配置文件 src/test/resources/application-test.yaml，所有连接信息走环境变量，缺失关键变量时集成测试自动跳过。

## 2. 核心配置文件与位置
- src/main/resources/application.yaml：全局基础配置，包含数据库、Redis、Flyway、Sa-Token、OpenAPI、应用自定义配置等。
- src/main/resources/application-prod.yaml：生产环境覆盖配置（禁用 swagger-ui 与 api-docs）。
- src/test/resources/application-test.yaml：测试环境专用配置，全部依赖环境变量。
- .env.example：提供本地开发所需的环境变量模板（DB/Redis/S3 相关键）。
- src/main/java/com/sunnao/spring/ddd/template/common/config/：Java 侧配置类，将部分行为从 YAML 迁移到代码级 Bean 定义。

## 3. 架构与设计约定
- 分层加载顺序：.env → application.yaml → application-{profile}.yaml，后者覆盖前者同名属性。
- 敏感信息不落地：数据库密码、Redis 密码、S3 密钥等一律通过环境变量注入，禁止硬编码或写入版本库。
- 默认值策略：对可选参数使用 ${KEY:default} 形式提供安全默认值（如 Redis port=6379、SSL=false、lock.type=redis 等）；对必填项不提供默认值，启动失败即提示缺失。
- 应用自定义配置命名空间：以 app.* 为前缀组织业务开关与能力选择，例如：
  - app.lock.type：分级锁实现（redis/jvm）
  - app.file.storage-type：文件存储后端（local/s3）
  - app.security.*：登录尝试限制、X-Forwarded-For 信任开关等
- 配置与代码解耦：基础设施细节（MyBatis-Flex 审计监听器、Sa-Token 路由拦截、OpenAPI 安全方案）集中在 common/config 包下，通过 @Configuration 装配，避免散落在各模块。

## 4. 开发者应遵循的规则
- 新增外部依赖配置时，优先在 application.yaml 中以 ${ENV_VAR:default} 形式声明，并在 .env.example 中补充对应键。
- 若配置项属于“运行时开关”且影响 Bean 创建（如锁类型、存储类型），使用 @Value("${app.xxx:default}") 注入到具体组件，保持 YAML 简洁。
- 生产环境差异仅通过 profile 覆盖，不要在主配置中写死环境相关逻辑。
- 测试配置必须完全依赖环境变量，不得引用本地路径或硬编码凭据。
- 任何新增的 app.* 配置项需在 application.yaml 中集中说明用途与取值范围，便于团队协作。
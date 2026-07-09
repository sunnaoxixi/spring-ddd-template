---
kind: dependency_management
name: Maven 依赖与版本集中管理
slug: dependency_management
category: dependency_management
scope:
    - '**'
---

本项目采用 Maven 单模块（聚合根）方式管理所有第三方依赖，通过 spring-boot-starter-parent 作为父 POM 统一继承 Spring Boot 4.1.0 的依赖 BOM，并在顶层 properties 中集中声明各组件的版本号，子模块或业务代码直接引用这些属性，避免版本号散落。

构建工具与包装器：使用 Maven Wrapper（.mvn/wrapper/maven-wrapper.properties），固定 Maven 3.9.16 分发地址为 Maven Central，保证团队与 CI 环境构建一致性。

核心依赖与版本策略：
- Java 25、Spring Boot 4.1.0、MyBatis-Flex 1.11.8、Sa-Token 1.45.0、springdoc-openapi 3.0.3、AWS SDK v2 S3 2.31.63、MapStruct 1.6.3、Hutool 5.8.46 等均在 properties 中统一定义。
- Lombok 标记为 optional=true，并通过 maven-compiler-plugin 的 annotationProcessorPaths 显式注册 Lombok、MapStruct、MyBatis-Flex 注解处理器，测试编译阶段仅启用 Lombok。
- Flyway 迁移脚本位于 src/main/resources/db/migration/，由 spring-boot-starter-flyway + flyway-database-postgresql 驱动执行。

仓库与私有源：未定义任何 repositories / pluginRepositories / mirror / distributionManagement，默认使用 Maven Central；也未发现 .m2/settings.xml 或环境变量指向私有仓库，表明当前不依赖内部 Nexus/Artifactory。

打包排除：spring-boot-maven-plugin 在 excludes 中排除了 Lombok，避免将 Lombok 打入最终 jar。

无锁文件与 vendoring：项目未生成 dependency.lock 或锁定文件，也不存在 vendor 目录，依赖解析完全交由 Maven 本地缓存（~/.m2/repository）。

开发者约定：新增依赖时优先在顶层 properties 声明版本，再在 dependencies 中引入；需要排除传递冲突时使用 exclusions（如 AWS SDK 排除 Netty NIO 客户端）；注解处理器统一通过 maven-compiler-plugin 的 annotationProcessorPaths 配置。
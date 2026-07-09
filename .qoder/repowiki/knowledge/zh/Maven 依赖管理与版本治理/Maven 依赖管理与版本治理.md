---
kind: dependency_management
name: Maven 依赖管理与版本治理
category: dependency_management
scope:
    - '**'
source_files:
    - pom.xml
    - .mvn/wrapper/maven-wrapper.properties
---

本项目采用 Maven 作为唯一的依赖管理工具，基于 Spring Boot 4.1.0 parent POM 进行统一编排，未使用多模块聚合工程，所有第三方库在根 pom.xml 中集中声明。

**版本集中化与属性管理**
- 通过 properties 段集中定义关键依赖版本：mybatis-flex.version=1.11.8、sa-token.version=1.45.0、springdoc.version=3.0.3、aws-sdk.version=2.31.63、org.mapstruct.version=1.6.3，子依赖直接引用 ${xxx.version}，避免散落的硬编码版本号。
- 其余依赖（如 postgresql、commons-pool2、aspectjweaver、spring-boot-starter-flyway、flyway-database-postgresql）由 Spring Boot parent 的 dependencyManagement 统一管理版本，无需显式指定。

**构建期注解处理器**
- maven-compiler-plugin 的 annotationProcessorPaths 显式注册 Lombok、MapStruct processor、MyBatis-Flex processor，确保编译期代码生成正确执行；测试编译阶段仅启用 Lombok。
- spring-boot-maven-plugin 将 Lombok 从最终 jar 中排除，避免打包冗余。

**私有仓库与镜像**
- 项目内未发现 .mvn/maven.config、settings.xml、~/.m2/settings.xml 或任何 <mirror>/<repository>/<server> 配置，也未见 MAVEN_OPTS、Nexus/Sonatype 相关环境变量，表明依赖解析完全依赖默认中央仓库（Maven Wrapper 指向 repo.maven.apache.org）。
- 若需接入企业私有仓库，应在用户级 ~/.m2/settings.xml 或通过 CI 环境注入 settings 文件。

**锁定策略**
- 未引入 mvnw 之外的 lockfile（如 dependency-check、versions-maven-plugin、bom），也无 vendor/ 目录，属于随用随拉模式。升级依赖时建议配合 mvn versions:display-dependency-updates 人工审查后再更新 properties 中的版本号。

**开发者约定**
- 新增依赖优先放入 properties 定义版本，再在 dependencies 引用，保持单点维护。
- 对 Spring Boot parent 已管理的依赖不要重复声明版本，避免冲突。
- 需要排除传递依赖时（如 AWS SDK 排除 Netty NIO 客户端），在对应 dependency 下使用 exclusions 显式声明并附注释说明原因。
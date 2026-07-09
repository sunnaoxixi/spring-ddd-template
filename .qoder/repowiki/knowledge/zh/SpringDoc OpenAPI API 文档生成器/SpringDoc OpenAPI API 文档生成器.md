---
kind: external_dependency
name: SpringDoc OpenAPI API 文档生成器
slug: springdoc-openapi-api-文档生成器
category: external_dependency
scope:
    - '**'
---

### SpringDoc OpenAPI API 文档生成器
- **角色定位**：自动生成 RESTful API 文档的工具，替代传统手写文档，提供交互式 Swagger UI 界面
- **访问路径**：开发环境可通过 `/swagger-ui.html` 访问可视化文档，`/v3/api-docs` 获取 OpenAPI JSON 规范
- **环境隔离**：生产环境（prod profile）默认关闭 Swagger UI 以避免安全风险，开发环境（dev profile）开启
- **Profile 控制**：通过 `SPRING_PROFILES_ACTIVE` 环境变量控制激活的配置文件，默认 prod 安全配置
- **验证参考**：确认具体注解使用和配置项与 SpringDoc 官方文档保持一致
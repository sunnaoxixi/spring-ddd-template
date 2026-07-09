---
kind: external_dependency
name: Sa-Token 认证鉴权框架
slug: sa-token-认证鉴权框架
category: external_dependency
scope:
    - '**'
---

### Sa-Token 认证鉴权框架
- **角色定位**：项目采用的轻量级 Java 权限认证框架，替代 Spring Security，提供 Token 签发、校验、会话管理功能
- **集成方式**：通过 `cn.dev33:sa-token-spring-boot4-starter` 依赖引入，配置类 `SaTokenConfigure` 定义 token 名称（`sa-token`）、有效期（30天）、多端并发等策略
- **存储后端**：通过 `sa-token-redis-template` 将 token 数据持久化到 Redis，支持分布式部署
- **鉴权注解**：使用 `@SaCheckRole` / `@SaCheckPermission` 进行接口级权限控制，权限点格式为 `{模块}:{read|write}`
- **请求头约定**：前端需在请求头携带 `sa-token: {tokenValue}`，不使用 Cookie 模式
- **特殊行为**：注册接口 `/api/auth/register` 需显式放行，无需登录态即可访问；当前用户信息接口 `/api/auth/me` 仅返回角色列表，不返回具体权限点集合
- **验证参考**：确认具体注解用法和配置项与官方文档保持一致
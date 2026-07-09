---
kind: external_dependency
name: Supabase 云端 PostgreSQL 服务
slug: supabase-云端-postgresql-服务
category: external_dependency
scope:
    - '**'
---

### Supabase 云端 PostgreSQL 服务
- **角色定位**：作为项目的云端数据库服务提供者，提供托管的 PostgreSQL 17 实例
- **连接特征**：使用 Supabase 提供的专用域名（如 `db.mgudilvkfewjmnvhqchs.supabase.co`），端口固定 5432，默认数据库名 `postgres`
- **安全约束**：生产环境通常强制启用 TLS 加密连接，需要在 Redis 配置中设置 `REDIS_SSL=true`
- **网络限制**：云端数据库通常有 IP 白名单限制，应用服务器 IP 需加入白名单才能访问
- **迁移脚本**：项目使用 Flyway 进行数据库版本管理，迁移脚本位于 `src/main/resources/db/migration/` 目录
- **环境变量**：连接信息通过 `.env` 文件中的 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD` 配置
- **验证参考**：Supabase 具体连接参数和安全策略需参照其官方文档
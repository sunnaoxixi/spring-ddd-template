# Docker Compose 生产部署指南

本文档用于将 `spring-ddd-template` 部署到云服务器，并用 Docker Compose 统一管理 Nginx、Spring Boot 应用、PostgreSQL 和 Redis。

## 架构

```text
Internet
  |
  v
Nginx :80
  |
  v
Spring Boot app :8080
  |                |
  v                v
PostgreSQL :5432  Redis :6379
```

生产 Compose 只对公网暴露 Nginx 的 `80` 端口。应用、PostgreSQL、Redis 只在 Compose 内部网络访问。

## 文件说明

| 文件 | 说明 |
| --- | --- |
| `Dockerfile` | 构建 Spring Boot 应用镜像，使用 Java 25。 |
| `.dockerignore` | 排除本地构建产物、日志、IDE 文件和真实环境变量。 |
| `.env.prod.example` | 生产环境变量模板，不包含真实密钥。 |
| `docker-compose.prod.yaml` | 生产 Compose 栈：Nginx、应用、PostgreSQL、Redis。 |
| `deploy/nginx/default.conf` | Nginx 反向代理配置。 |

如果需要使用 GitHub Actions 自动部署，参考 [GitHub Actions CI/CD 部署教程](./github-actions-cicd.md)。

## 服务器准备

安装 Docker 和 Docker Compose 插件，并开放云服务器安全组：

| 端口 | 用途 | 是否建议公网开放 |
| --- | --- | --- |
| `22` | SSH | 按需开放，建议限制来源 IP。 |
| `80` | Nginx HTTP | 开放。 |
| `443` | HTTPS | 配置 HTTPS 后开放。 |
| `8080` | Spring Boot | 不开放。 |
| `5432` | PostgreSQL | 不开放。 |
| `6379` | Redis | 不开放。 |

## 首次部署

在服务器拉取代码：

```bash
git clone <your-repo-url> spring-ddd-template
cd spring-ddd-template
```

创建生产环境变量文件：

```bash
cp .env.prod.example .env.prod
vim .env.prod
```

至少修改以下值：

```env
DB_PASSWORD=replace-with-a-strong-postgres-password
REDIS_PASSWORD=replace-with-a-strong-redis-password
```

启动整套服务：

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml up -d --build
```

查看状态：

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml ps
```

查看应用日志：

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml logs -f app
```

应用首次启动时会通过 Flyway 自动执行 `src/main/resources/db/migration/` 下的数据库迁移。

## 验证部署

通过 Nginx 访问登录接口：

```bash
curl -X POST http://<server-ip>/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"admin123456"}'
```

返回 token 表示 Nginx、应用、PostgreSQL、Redis 基本链路可用。首次登录后请立即修改默认管理员密码。

## 日常运维

更新应用：

```bash
git pull
docker compose --env-file .env.prod -f docker-compose.prod.yaml up -d --build
```

重启应用：

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml restart app
```

暂停整套服务：

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml stop
```

停止服务但保留数据卷：

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml down
```

删除服务和数据卷会清空数据库、Redis 和本地文件，请谨慎执行：

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml down -v
```

## 数据备份与恢复

备份 PostgreSQL：

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml exec -T postgres \
  sh -c 'pg_dump -U "$DB_USERNAME" "$DB_NAME"' > backup.sql
```

恢复 PostgreSQL：

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml exec -T postgres \
  sh -c 'psql -U "$DB_USERNAME" "$DB_NAME"' < backup.sql
```

备份应用本地上传文件：

```bash
docker run --rm \
  -v spring-ddd-template-app-files:/data \
  -v "$PWD":/backup \
  alpine tar czf /backup/app-files.tar.gz -C /data .
```

## HTTPS

当前 Compose 默认只配置 HTTP `80`，适合先跑通部署。生产 HTTPS 有两种常见做法：

1. 使用云厂商负载均衡、CDN 或网关托管证书，回源到服务器 `80`。
2. 在本机扩展 Nginx 配置，挂载证书目录并开放 `443`。

如果采用本机 Nginx 证书方式，需要增加 `443:443` 端口映射，并在 `deploy/nginx/default.conf` 中增加 `listen 443 ssl;`、证书路径和 HTTP 到 HTTPS 跳转规则。

## 安全注意事项

- 不要提交 `.env.prod`，仓库只保留 `.env.prod.example`。
- 数据库和 Redis 不要映射公网端口。
- `DB_PASSWORD` 和 `REDIS_PASSWORD` 必须使用强密码。
- 首次部署后立即修改默认管理员密码。
- 定期备份 PostgreSQL 数据卷和 `app-files` 文件卷。
- 如果应用需要识别真实客户端 IP，确认 Nginx 是唯一可信入口后，再评估是否开启 `app.security.trust-x-forwarded-for`。

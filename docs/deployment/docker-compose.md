# Docker Compose 生产部署指南

本文档用于将 `spring-ddd-template` 后端部署到云服务器，并用 Docker Compose 统一管理 Spring Boot 应用、PostgreSQL 和 Redis。

## 架构

```text
Internet
  |
  v
Frontend / Gateway Nginx :80/443
  |
  | optional proxy /api/ to backend
  v
Spring Boot app :8080
  |                |
  v                v
PostgreSQL :5432  Redis :6379
```

生产 Compose 只暴露后端应用的 `8080` 端口。前端页面或统一网关继续占用 `80/443`，可按需把 `/api/` 反向代理到 `http://127.0.0.1:8080`。PostgreSQL 和 Redis 只在 Compose 内部网络访问，不映射到宿主机端口。

## 文件说明

| 文件 | 说明 |
| --- | --- |
| `Dockerfile` | 构建 Spring Boot 应用镜像，使用 Java 25。 |
| `.dockerignore` | 排除本地构建产物、日志、IDE 文件和真实环境变量。 |
| `.env.prod.example` | 生产环境变量模板，不包含真实密钥。 |
| `docker-compose.prod.yaml` | 后端生产 Compose 栈：应用、PostgreSQL、Redis。 |

如果需要使用 GitHub Actions 自动部署，参考 [GitHub Actions CI/CD 部署教程](./github-actions-cicd.md)。

## 服务器准备

安装 Docker 和 Docker Compose 插件，并开放云服务器安全组：

| 端口 | 用途 | 是否建议公网开放 |
| --- | --- | --- |
| `22` | SSH | 按需开放，建议限制来源 IP。 |
| `80` | 前端或统一网关 HTTP | 由前端服务决定。 |
| `443` | 前端或统一网关 HTTPS | 由前端服务决定。 |
| `8080` | Spring Boot 后端 API | 如需公网直连后端则开放；若只允许前端 Nginx 反代，可在防火墙限制来源。 |
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

直连后端 `8080` 访问登录接口：

```bash
curl -X POST http://<server-ip>:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"admin123456"}'
```

返回 token 表示应用、PostgreSQL、Redis 基本链路可用。首次登录后请立即修改默认管理员密码。

如果前端 Nginx 需要同域转发 API，可以在前端 Nginx 配置中加入类似片段：

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

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

## 前端网关和 HTTPS

后端 Compose 不管理 `80/443`。生产 HTTPS 应由前端项目、统一 Nginx、云负载均衡或 CDN 承担。

常见方式：

1. 前端 Nginx 监听 `80/443`，页面请求 `/api/` 时反代到 `127.0.0.1:8080`。
2. 云厂商负载均衡、CDN 或网关托管证书，再按路径或域名转发到前端和后端。
3. 后端单独使用 `api.example.com:443` 时，应由外层网关转发到后端 `8080`，而不是让后端 Compose 直接管理证书。

## 安全注意事项

- 不要提交 `.env.prod`，仓库只保留 `.env.prod.example`。
- 数据库和 Redis 不要映射公网端口。
- 如果 `8080` 不需要公网直连，建议在云服务器安全组或系统防火墙中限制来源。
- `DB_PASSWORD` 和 `REDIS_PASSWORD` 必须使用强密码。
- 首次部署后立即修改默认管理员密码。
- 定期备份 PostgreSQL 数据卷和 `app-files` 文件卷。
- 如果应用需要识别真实客户端 IP，确认前端 Nginx 或网关是唯一可信入口后，再评估是否开启 `app.security.trust-x-forwarded-for`。

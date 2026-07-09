# GitHub Actions CI/CD 部署教程

本文档基于 [Docker Compose 生产部署指南](./docker-compose.md)，说明如何用 GitHub Actions 自动部署到云服务器。

## 推荐流程

推荐使用 GitHub Actions 负责 CI 校验和远程触发部署，云服务器继续使用本仓库已有的 `docker-compose.prod.yaml` 管理后端 Spring Boot、PostgreSQL 和 Redis。

```text
push main
  |
  v
GitHub Actions
  |
  | 1. mvn test
  | 2. SSH 登录服务器
  v
Cloud Server
  |
  | git pull --ff-only
  | docker compose up -d --build
  v
Backend App :8080 + PostgreSQL + Redis
```

这种方式的优点是部署逻辑和手动部署保持一致，不需要先接入 Docker Hub、GHCR 或私有镜像仓库。

## 方案对比

| 方案 | 说明 | 适用场景 |
| --- | --- | --- |
| SSH 到服务器执行 Compose | GitHub Actions 登录服务器，拉取最新代码并重新构建容器。 | 单台云服务器、小团队、低运维复杂度，推荐先用。 |
| 构建镜像并推送到镜像仓库 | Actions 构建应用镜像，推送到 GHCR/Docker Hub，服务器只拉镜像并重启。 | 多台服务器、需要版本化镜像、需要回滚镜像 tag。 |
| GitHub self-hosted runner | 在服务器安装 GitHub Runner，由服务器本机执行部署任务。 | 内网部署、不能开放 SSH、已有 Runner 管理体系。 |

本文完整展开第一种方案。

## 前置条件

服务器上已经完成一次手动部署，并能执行以下命令成功启动：

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yaml up -d --build
```

服务器部署目录示例：

```text
/opt/spring-ddd-template
```

该目录中必须存在：

```text
.env.prod
docker-compose.prod.yaml
Dockerfile
```

`.env.prod` 只保存在服务器上，不提交到 GitHub。

## 创建部署用户

建议使用独立的 `deploy` 用户，不直接使用 `root`：

```bash
sudo adduser deploy
sudo usermod -aG docker deploy
sudo mkdir -p /opt/spring-ddd-template
sudo chown -R deploy:deploy /opt/spring-ddd-template
```

重新登录 `deploy` 用户后确认 Docker 可用：

```bash
docker ps
```

如果仓库是私有仓库，需要确保服务器上的 `deploy` 用户有权限执行 `git pull`。常见做法是在 GitHub 仓库配置 Deploy key，或在服务器上配置只读访问令牌。

## 配置 SSH 登录密钥

在本地生成一把专用于 GitHub Actions 部署的 SSH key：

```bash
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ./github-actions-deploy
```

把公钥写入服务器：

```bash
ssh-copy-id -i ./github-actions-deploy.pub deploy@<server-ip>
```

验证登录：

```bash
ssh -i ./github-actions-deploy deploy@<server-ip> 'cd /opt/spring-ddd-template && docker compose --env-file .env.prod -f docker-compose.prod.yaml ps'
```

验证通过后，把私钥内容保存到 GitHub Secrets。

## 配置 GitHub Secrets

进入 GitHub 仓库：

```text
Settings -> Secrets and variables -> Actions -> New repository secret
```

添加以下 Secrets：

| Secret | 示例 | 说明 |
| --- | --- | --- |
| `SERVER_HOST` | `1.2.3.4` | 云服务器公网 IP 或域名。 |
| `SERVER_PORT` | `22` | SSH 端口。 |
| `SERVER_USER` | `deploy` | SSH 登录用户。 |
| `SERVER_SSH_KEY` | 私钥全文 | `github-actions-deploy` 私钥内容。 |
| `DEPLOY_PATH` | `/opt/spring-ddd-template` | 服务器上的项目目录。 |

不要把 `.env.prod`、数据库密码、Redis 密码写进 workflow 文件。生产环境变量应只留在服务器。

## 创建 Workflow

在仓库中新建文件：

```text
.github/workflows/deploy.yml
```

内容如下：

```yaml
name: CI/CD Deploy

on:
  push:
    branches:
      - main
  workflow_dispatch:

concurrency:
  group: production-deploy
  cancel-in-progress: false

jobs:
  ci:
    name: Maven Test
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '25'
          cache: maven

      - name: Run tests
        run: ./mvnw -B test

  deploy:
    name: Deploy to server
    runs-on: ubuntu-latest
    needs: ci
    environment: production

    steps:
      - name: Configure SSH
        shell: bash
        env:
          SERVER_HOST: ${{ secrets.SERVER_HOST }}
          SERVER_PORT: ${{ secrets.SERVER_PORT }}
          SERVER_SSH_KEY: ${{ secrets.SERVER_SSH_KEY }}
        run: |
          mkdir -p ~/.ssh
          install -m 600 /dev/null ~/.ssh/deploy_key
          printf '%s\n' "$SERVER_SSH_KEY" > ~/.ssh/deploy_key
          ssh-keyscan -p "$SERVER_PORT" "$SERVER_HOST" >> ~/.ssh/known_hosts

      - name: Deploy with Docker Compose
        shell: bash
        env:
          SERVER_HOST: ${{ secrets.SERVER_HOST }}
          SERVER_PORT: ${{ secrets.SERVER_PORT }}
          SERVER_USER: ${{ secrets.SERVER_USER }}
          DEPLOY_PATH: ${{ secrets.DEPLOY_PATH }}
        run: |
          ssh -i ~/.ssh/deploy_key -p "$SERVER_PORT" "$SERVER_USER@$SERVER_HOST" \
            "cd '$DEPLOY_PATH' && \
             git pull --ff-only && \
             docker compose --env-file .env.prod -f docker-compose.prod.yaml up -d --build --remove-orphans && \
             docker image prune -f"
```

提交到 `main` 后，GitHub Actions 会先运行 Maven 测试，测试通过后再部署。

## 手动触发部署

workflow 中配置了 `workflow_dispatch`，可以在 GitHub 页面手动触发：

```text
Actions -> CI/CD Deploy -> Run workflow
```

这适合首次验证、紧急重试或服务器配置调整后的手动发布。

## 增加生产环境审批

上面的 workflow 使用了：

```yaml
environment: production
```

可以在 GitHub 仓库中配置生产环境审批：

```text
Settings -> Environments -> New environment -> production
```

然后添加 required reviewers。这样 `main` 分支 CI 通过后，部署步骤会等待人工批准。

## 常见问题

### GitHub Actions 能 SSH 登录，但 git pull 失败

服务器上的 `deploy` 用户没有仓库读取权限。进入服务器后手动执行：

```bash
cd /opt/spring-ddd-template
git pull --ff-only
```

如果这里失败，先修复服务器侧 GitHub 访问权限。

### docker: permission denied

`deploy` 用户不在 `docker` 用户组，或加入用户组后没有重新登录。执行：

```bash
sudo usermod -aG docker deploy
```

然后重新登录 `deploy` 用户。

### git pull 提示本地有修改

生产服务器目录应该保持干净。检查修改内容：

```bash
cd /opt/spring-ddd-template
git status --short
```

如果修改的是 `.env.prod`，说明该文件不应被 Git 跟踪；确认仓库只提交 `.env.prod.example`，不要提交 `.env.prod`。

### Maven 测试需要数据库或 Redis

当前项目的部分集成测试依赖 PostgreSQL 和 Redis，并通过环境变量缺失时跳过。若需要在 CI 中完整运行集成测试，可以在 workflow 的 `ci` job 中增加 PostgreSQL 和 Redis services，再配置测试环境变量。

## 回滚思路

当前 SSH Compose 方案是从服务器工作目录拉取 `main` 最新代码并现场构建。如果需要回滚：

```bash
cd /opt/spring-ddd-template
git log --oneline -n 10
git checkout <commit-sha>
docker compose --env-file .env.prod -f docker-compose.prod.yaml up -d --build
```

回滚后如果要恢复到 `main`：

```bash
git checkout main
git pull --ff-only
docker compose --env-file .env.prod -f docker-compose.prod.yaml up -d --build
```

如果项目后续需要更强的回滚能力，建议演进到“构建镜像并推送到镜像仓库”的方案，用镜像 tag 管理版本。

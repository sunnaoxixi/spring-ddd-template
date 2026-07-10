# Spring DDD Template 前端开发对接指南

> 本文档以当前仓库源码为准，面向独立前端项目说明可用页面、接口契约、登录态、角色鉴权和已知能力边界。
>
> 本次梳理日期：2026-07-10。当前仓库是 Spring Boot 4.1.0、Java 25 的后端单模块 Maven 工程，**不包含前端工程或前端技术栈约束**。
>
> 主要源码依据：
>
> - `src/main/java/com/sunnao/spring/ddd/template/adaptor/**/input/*Controller.java`
> - `src/main/java/com/sunnao/spring/ddd/template/client/**/req|res/*.java`
> - `src/main/java/com/sunnao/spring/ddd/template/common/config/SaTokenConfigure.java`
> - `src/main/java/com/sunnao/spring/ddd/template/infrastructure/auth/StpInterfaceImpl.java`
> - `src/main/java/com/sunnao/spring/ddd/template/common/result/ResultDO.java`
> - `src/main/java/com/sunnao/spring/ddd/template/common/result/ErrorCodeEnum.java`
> - `src/main/resources/application.yaml`、`application-prod.yaml` 和 `db/migration/*.sql`

---

## 1. 当前后端能力快照

当前共有 7 个 Controller、34 个 HTTP 接口：

| 模块 | 接口数 | 当前能力 | 接口角色 |
|---|---:|---|---|
| 认证 | 4 | 登录、注册、登出、当前用户 | `/api/auth/**` 当前全部被路由拦截器放行，接口内部自行处理登录态 |
| 用户 | 6 | 用户 CRUD、状态变更 | 仅 `admin` |
| 角色 | 6 | 角色 CRUD、全量覆盖用户角色 | 仅 `admin` |
| 字典 | 9 | 字典类型和字典数据维护、业务字典查询 | 查询：`admin` 或 `user`；写入：仅 `admin` |
| 文件 | 4 | S3 兼容对象存储的上传、下载、查询、删除 | `admin` 或 `user` |
| 日志 | 2 | 操作日志、登录日志分页查询 | 仅 `admin` |
| 在线用户 | 3 | 在线会话分页、按会话或用户强制下线 | 仅 `admin` |

当前后端没有以下接口：

- 首页统计、仪表盘数据接口；
- 普通用户修改本人资料接口；
- 修改密码、忘记密码或管理员重置密码接口；
- 文件公开 URL、预签名 URL、图片预览接口；
- 权限码查询、角色权限分配或按钮权限接口；
- 在线用户筛选接口；
- refresh-token 接口。

前端不得把尚未实现的能力设计成可提交表单；如保留入口，应明确做成只读、禁用或“后端待支持”状态。

---

## 2. 本地联调环境

### 2.1 启动后端

Windows 示例：

```powershell
Copy-Item .env.example .env
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Linux / macOS 示例：

```bash
cp .env.example .env
docker compose up -d
./mvnw spring-boot:run
```

说明：

- `docker compose up -d` 只启动 PostgreSQL 17 和 Redis 7；
- 文件模块使用 S3 兼容对象存储，`docker compose` 不会启动 MinIO 等对象存储；`.env` 中的 `S3_ENDPOINT`、`S3_REGION`、`S3_ACCESS_KEY`、`S3_SECRET_KEY`、`S3_BUCKET` 必须按实际环境填写，关键配置留空会导致应用在 S3 初始化阶段启动失败；
- Flyway 会在应用启动时自动建表并写入初始化数据；
- 后端未配置 `server.servlet.context-path` 和自定义端口，本地默认地址为 `http://localhost:8080`。

### 2.2 联调地址

| 内容 | 地址 |
|---|---|
| 后端 Origin | `http://localhost:8080` |
| API 前缀 | `/api` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

后端当前**没有 CORS 配置**。前端开发服务器若不是 `localhost:8080` 同源，应使用开发代理把 `/api`、`/v3/api-docs` 等路径转发到后端，或由统一网关处理跨域；不要依赖浏览器直接跨域访问。

`application-prod.yaml` 的注释写着关闭 Swagger，但当前实际配置值仍是 `enabled: true`。因此当前代码配置下文档仍可能开放，部署环境是否暴露应以最终运行配置和网关策略为准，前端运行时不能依赖 Swagger 必然可访问。

### 2.3 初始化账号

全新数据库的种子管理员为：

- 邮箱：`admin@example.com`
- 初始密码：`admin123456`

该账号仅用于初始化和本地联调。迁移脚本提示首次登录后修改密码，但当前后端尚未提供修改密码接口，这是现阶段明确的能力缺口。

---

## 3. 通用 HTTP 契约

### 3.1 Token 与请求头

登录或注册成功后，`data` 返回：

```json
{
  "tokenName": "sa-token",
  "tokenValue": "实际 token 值",
  "userId": 1,
  "nickname": "系统管理员",
  "roles": ["admin"]
}
```

后续请求应使用响应中的 `tokenName` 作为请求头名称、`tokenValue` 作为请求头值：

```http
sa-token: 实际 token 值
```

当前配置：

- 只从请求头读取 token，不读取 Cookie；
- token 有效期为 30 天；
- 允许同一账号多端同时登录；
- 没有 refresh-token 和静默续期接口；
- 登录或注册会签发新 token；
- token 不应放入 URL、查询参数、埋点、错误日志或普通业务持久化中。

### 3.2 统一 JSON 响应

除文件下载接口的二进制成功响应，以及文件下载 Controller 内产生的 404/500 空响应外，其余接口统一返回 `ResultDO<T>`：

```json
{
  "success": true,
  "code": null,
  "msg": null,
  "data": {}
}
```

业务失败示例：

```json
{
  "success": false,
  "code": "PARAM_ERROR",
  "msg": "每页条数必须在1~100之间",
  "data": null
}
```

前端必须遵守以下判断顺序：

1. 先处理 HTTP 状态；
2. 对 JSON 接口解析响应体；
3. 即使 HTTP 状态为 `200`，仍必须检查 `success`；
4. `success=false` 时以 `code` 做流程判断，以 `msg` 做用户提示；
5. `success=true` 时，`code` 和 `msg` 通常为 `null`，无返回数据的写操作其 `data` 也为 `null`。

不要仅用“HTTP 2xx”判断业务成功。

### 3.3 HTTP 状态与业务错误

| HTTP 状态 | 当前产生场景 | 响应体 |
|---:|---|---|
| `200` | 正常成功；DTO 校验失败；领域规则失败；数据库/存储等被应用层转换后的业务失败 | 通常为统一 `ResultDO`，必须继续判断 `success` |
| `400` | JSON 无法反序列化、query/path 参数类型错误 | `ResultDO`，`code=BAD_REQUEST` |
| `401` | 除 `/api/auth/**` 外的接口未登录、token 失效或被踢下线 | `ResultDO`，`code=NOT_LOGIN` |
| `403` | 已登录但角色不满足 Controller 的 `@SaCheckRole` | `ResultDO`，`code=NO_PERMISSION` |
| `404` | 不存在的 HTTP 路径 | `ResultDO`，`code=NOT_FOUND` |
| `500` | 未捕获异常 | `ResultDO`，`code=SYSTEM_ERROR` |

认证接口有两个特殊行为：

- `GET /api/auth/me` 未登录时不会触发 401，而是返回 HTTP 200、`success=false`、`code=NOT_LOGIN`；
- `POST /api/auth/logout` 未登录时按幂等成功处理，返回 HTTP 200、`success=true`。

文件下载的控制器内失败不使用统一 JSON，详见第 10 章。

### 3.4 链路追踪头

每次响应都会带：

```http
X-Trace-Id: 后端默认生成的 32 位标识，或前端/上游传入值
```

前端也可以在请求中主动传入 `X-Trace-Id`，后端会透传使用。发生问题时建议把响应中的 `X-Trace-Id` 展示在错误详情或复制诊断信息中，便于和操作日志、登录日志及服务端日志关联。

### 3.5 分页、状态、时间与排序

分页接口统一使用：

| 参数 | 默认值 | 约束 |
|---|---:|---|
| `pageNum` | `1` | 必须 `>= 1` |
| `pageSize` | `10` | 必须在 `1~100` 之间 |

分页响应的列表字段因模块而异：

| 模块 | `data.total` | 列表字段 |
|---|---|---|
| 用户 | 有 | `data.users` |
| 角色 | 有 | `data.roles` |
| 字典类型 | 有 | `data.types` |
| 文件 | 有 | `data.files` |
| 操作日志 | 有 | `data.logs` |
| 登录日志 | 有 | `data.logs` |
| 在线用户 | 有 | `data.onlineUsers` |

统一状态值：

- `1`：启用；
- `0`：禁用。

时间字段使用 Java `LocalDateTime`，以不带时区偏移的 ISO-8601 字符串传输，例如：

```text
2026-07-10T12:30:00
```

日志查询的 `startTime`、`endTime` 也使用该格式，开始和结束边界均包含在查询范围内，且 `startTime` 不能晚于 `endTime`。前端不要擅自追加 `Z` 或按 UTC 转换，除非部署环境另有统一时区约定。

当前排序规则：

- 用户、角色、字典类型、文件、操作日志、登录日志：按 ID 倒序；
- 字典数据：按 `sort` 升序，再按 ID 升序；
- 在线会话：后端没有显式排序保证，前端不能依赖固定顺序。

### 3.6 推荐的统一请求处理

通用 JSON 请求至少需要区分三类失败：

```text
HTTP 401             -> 清理当前登录态并跳转登录页
HTTP 403             -> 展示无权限页或无权限提示
HTTP 200 + success=false -> 按 code 处理业务失败，展示 msg
```

`GET /api/auth/me` 是登录恢复流程的例外：它可能以 HTTP 200 返回 `NOT_LOGIN`，因此应用初始化时不能只看 HTTP 状态。

---

## 4. 登录态与角色鉴权

### 4.1 `/api/auth/**` 的实际放行范围

当前 `SaTokenConfigure` 放行整个 `/api/auth/**`，并非只放行登录、注册：

| 接口 | 未登录调用结果 |
|---|---|
| `POST /api/auth/login` | 正常执行登录 |
| `POST /api/auth/register` | 正常执行注册并自动登录 |
| `POST /api/auth/logout` | 幂等成功 |
| `GET /api/auth/me` | HTTP 200，业务失败 `NOT_LOGIN` |

除上述路径外，所有 `/api/**` 都先要求登录，再执行 Controller 上的角色校验。

### 4.2 角色访问矩阵

| 模块/能力 | `admin` | `user` | 仅持有其他自定义角色 |
|---|:---:|:---:|:---:|
| 用户管理 | 可访问 | 不可访问 | 当前不可访问 |
| 角色管理 | 可访问 | 不可访问 | 当前不可访问 |
| 字典类型/数据查询 | 可访问 | 可访问 | 当前不可访问 |
| 字典写操作 | 可访问 | 不可访问 | 当前不可访问 |
| 文件查询、上传、下载、删除 | 可访问 | 可访问 | 当前不可访问 |
| 操作日志、登录日志 | 可访问 | 不可访问 | 当前不可访问 |
| 在线用户 | 可访问 | 不可访问 | 当前不可访问 |

当前系统只使用角色鉴权：

- `StpInterfaceImpl.getPermissionList()` 固定返回空列表；
- 权限表和角色权限关联表已由 Flyway V7 删除；
- 没有权限码接口，也没有按钮级权限码；
- 自定义角色虽然可以创建、启用并分配给用户，但没有任何 Controller 声明这些角色，因此不会自动获得接口访问能力；
- 前端隐藏菜单和按钮只是体验控制，后端角色校验仍是最终安全边界。

### 4.3 有效角色规则

用户列表/详情、登录接口和 `/api/auth/me` 返回的是**启用状态角色的 `roleKey` 列表**，不是角色 ID：

```json
["admin", "user"]
```

注意：

- 已禁用角色不会出现在有效角色列表中，也不会通过后续角色鉴权；
- `admin` 和 `user` 都是内置角色，二者均不能删除；
- `admin` 角色不允许禁用；
- `user` 角色当前允许禁用。禁用后，所有仅依赖 `user` 角色的用户都将失去字典和文件接口访问能力，前端应提供高风险二次确认；
- 用户角色被清空后，已有登录会话可以继续存在，但后续访问角色接口会得到 403；
- 角色分配界面需要把用户返回的 `roleKey` 与角色列表的 `roleKey` 映射为角色 ID。已分配但已禁用的角色不会出现在用户的有效角色列表中，当前接口无法完整回显这部分关联；
- 注册响应是当前例外：创建流程直接回显所分配的角色键。如果 `user` 已被禁用，注册仍会建立该角色关联，注册响应可能仍暂时包含 `user`，但随后 `/api/auth/me` 和 Controller 鉴权会按启用状态过滤。前端应以 `/api/auth/me` 作为有效角色的权威来源。

---

## 5. 前端路由与菜单建议

仓库没有现成前端，以下路径仅是对当前接口能力的建议映射：

```text
/login                         登录
/register                      注册
/                              登录后首页（当前无后端统计接口）
/profile                       当前账户信息；普通用户只读
/system/user                   用户管理 [admin]
/system/role                   角色管理 [admin]
/system/dict                   字典类型 [admin | user]
/system/dict/data?typeKey=...  字典数据二级页 [admin | user]
/system/file                   文件管理 [admin | user]
/system/log/operation          操作日志 [admin]
/system/log/login              登录日志 [admin]
/system/online                 在线用户 [admin]
/403                           无权限
/404                           前端路由不存在
```

菜单和路由守卫建议：

1. 应用启动时读取本地登录 token；
2. 有 token 时调用 `GET /api/auth/me` 恢复用户；
3. HTTP 401 或业务错误 `NOT_LOGIN` 时清理 token；
4. 使用 `roles` 控制菜单、路由和操作按钮；
5. 自定义角色当前不映射任何系统菜单；
6. 普通用户的 `/profile` 只能回显 `/api/auth/me`，不能提交修改；
7. 首页只能展示前端静态欢迎信息或已有接口可推导的数据，不应虚构统计 API。

---

## 6. 认证模块

### 6.1 接口清单

| Method | 路径 | 请求 | 成功时 `data` | 访问要求 |
|---|---|---|---|---|
| POST | `/api/auth/login` | JSON：`email`、`password` | `tokenName`、`tokenValue`、`userId`、`nickname`、`roles` | 公开 |
| POST | `/api/auth/register` | JSON：`email`、`nickname`、`password`、`confirmPassword` | 与登录响应相同 | 公开 |
| POST | `/api/auth/logout` | 无 | `null` | 当前路由公开；未登录也成功 |
| GET | `/api/auth/me` | 无 | 当前用户信息 | 当前路由公开；未登录返回业务 `NOT_LOGIN` |

### 6.2 登录

请求：

```json
{
  "email": "admin@example.com",
  "password": "admin123456"
}
```

校验规则：

- `email` 必填并通过后端邮箱格式校验；
- `password` 不能为空；
- 邮箱或密码错误统一返回 `AUTH_FAIL`，不会区分账号不存在还是密码错误；
- 同一“邮箱 + 客户端 IP”在固定窗口内累计 5 次凭证错误后，15 分钟窗口结束前返回 `AUTH_LOCKED`；
- 账号禁用返回 `USER_DISABLED`，禁用状态不计入密码失败次数；
- 登录成功会清空该“邮箱 + IP”的失败计数。

成功响应的 `data`：

```json
{
  "tokenName": "sa-token",
  "tokenValue": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "userId": 1,
  "nickname": "系统管理员",
  "roles": ["admin"]
}
```

### 6.3 注册

请求：

```json
{
  "email": "user@example.com",
  "nickname": "普通用户",
  "password": "123456",
  "confirmPassword": "123456"
}
```

校验规则：

- 邮箱、昵称必填；
- 密码长度至少 6 位；
- `password` 与 `confirmPassword` 必须一致；
- 邮箱冲突返回 `EMAIL_DUPLICATE`；
- 注册用户默认分配 `user` 角色；该关联不校验角色启用状态，若 `user` 已被禁用，注册仍可成功，但后续不会获得有效的 `user` 接口权限；
- 注册成功后自动登录并直接返回 token，无需再次调用登录接口。

### 6.4 当前用户

成功时 `data` 示例：

```json
{
  "userId": 1,
  "email": "admin@example.com",
  "nickname": "系统管理员",
  "avatar": null,
  "roles": ["admin"],
  "status": 1
}
```

该接口适合用于刷新页面后的登录态恢复。它不返回 token，也不提供资料修改能力。

### 6.5 登出

调用成功后前端应清理本地 token 和用户状态。即使接口因网络异常未返回，用户主动选择退出时也应允许前端清理本地登录态。

---

## 7. 用户管理模块

全部接口仅允许 `admin`。

### 7.1 接口清单

| Method | 路径 | 请求 | 成功时 `data` |
|---|---|---|---|
| GET | `/api/system/users/page` | query：`pageNum`、`pageSize`、`email?`、`nickname?`、`status?` | `{ total, users }` |
| GET | `/api/system/users/{id}` | path：用户 ID | `{ user }` |
| POST | `/api/system/users` | JSON：`email`、`nickname`、`password`、`avatar?`、`roleIds?` | `{ userId }` |
| PUT | `/api/system/users/{id}` | JSON：`nickname?`、`avatar?` | `{ userId }` |
| PUT | `/api/system/users/{id}/status` | JSON：`status` | `{ userId, status }` |
| DELETE | `/api/system/users/{id}` | path：用户 ID | `{ userId }` |

### 7.2 用户数据结构

列表和详情中的 `UserDTO`：

```json
{
  "id": 1,
  "email": "admin@example.com",
  "nickname": "系统管理员",
  "status": 1,
  "roles": ["admin"],
  "avatar": null,
  "createAt": "2026-07-10T10:00:00",
  "updateAt": "2026-07-10T10:00:00"
}
```

其中 `roles` 是启用角色的 `roleKey`，不是角色 ID。

### 7.3 分页查询

查询匹配规则：

- `email`：精确匹配；
- `nickname`：模糊匹配；
- `status`：精确匹配 `0` 或 `1`；
- 默认按用户 ID 倒序。

示例：

```http
GET /api/system/users/page?pageNum=1&pageSize=10&email=admin@example.com&status=1
```

### 7.4 创建用户

请求示例：

```json
{
  "email": "new-user@example.com",
  "nickname": "新用户",
  "password": "123456",
  "avatar": "https://example.com/avatar.png",
  "roleIds": [2]
}
```

规则：

- 邮箱格式必须合法，昵称必填，密码至少 6 位；
- `avatar` 是普通字符串 URL，后端不校验 URL 可访问性；
- `roleIds` 不能包含 `null`；
- `roleIds` 省略、传 `null` 或传空数组 `[]` 时，都会默认分配 `user` 角色；
- 默认角色关联不校验启用状态；若 `user` 已被禁用，新用户仍可创建，但不会获得有效的 `user` 接口权限；
- 显式传入角色 ID 时，重复 ID 会被去重，任何无效 ID 都会导致创建失败；禁用角色也可以建立关联，但不会产生有效授权；
- 新用户默认状态为启用。

### 7.5 修改用户资料

请求示例：

```json
{
  "nickname": "新昵称",
  "avatar": "https://example.com/new-avatar.png"
}
```

规则：

- 只能修改昵称和头像；
- 邮箱、密码、角色、状态不能通过此接口修改；
- `nickname` 和 `avatar` 至少一个为非空字符串；
- 空字符串不会清空已有昵称或头像，当前接口没有“移除头像”语义；
- 该接口只允许 `admin`，普通用户不能用它修改本人资料。

### 7.6 启用、禁用与删除

状态请求：

```json
{
  "status": 0
}
```

规则：

- 状态只能是 `0` 或 `1`；
- 重复设置为当前状态会返回 `STATUS_INVALID`；
- 禁用用户成功后，后端会尝试踢下线该用户的全部会话；
- 删除是逻辑删除，同时清理用户角色关联，并尝试踢下线该用户全部会话；
- 后端当前没有保护“种子管理员用户”或“当前操作者本人”的特殊规则，管理员可以禁用或删除自己。前端应对当前用户的自禁用、自删除提供更强确认，并在成功后立即清理本地登录态。

### 7.7 用户角色分配入口

角色分配使用角色模块接口：

```http
PUT /api/system/roles/users/{userId}
```

请求和注意事项见第 8.6 节。

---

## 8. 角色管理模块

全部接口仅允许 `admin`。

### 8.1 接口清单

| Method | 路径 | 请求 | 成功时 `data` |
|---|---|---|---|
| GET | `/api/system/roles/page` | query：`pageNum`、`pageSize`、`roleKey?`、`roleName?`、`status?` | `{ total, roles }` |
| GET | `/api/system/roles/{id}` | path：角色 ID | `{ role }` |
| POST | `/api/system/roles` | JSON：`roleKey`、`roleName`、`remark?` | `{ roleId }` |
| PUT | `/api/system/roles/{id}` | JSON：`roleName?`、`status?`、`remark?` | `{ roleId }` |
| DELETE | `/api/system/roles/{id}` | path：角色 ID | `{ roleId }` |
| PUT | `/api/system/roles/users/{userId}` | JSON：`roleIds` | `{ userId }` |

### 8.2 角色数据结构

```json
{
  "id": 1,
  "roleKey": "admin",
  "roleName": "管理员",
  "status": 1,
  "remark": "系统管理员",
  "createAt": "2026-07-10T10:00:00",
  "updateAt": "2026-07-10T10:00:00"
}
```

### 8.3 分页查询

- `roleKey`：精确匹配；
- `roleName`：模糊匹配；
- `status`：精确匹配；
- 默认按角色 ID 倒序。

分配角色时如果角色总数超过 100，前端需要翻页加载，不能把 `pageSize` 设置为超过 100。

### 8.4 创建角色

请求示例：

```json
{
  "roleKey": "auditor",
  "roleName": "审计员",
  "remark": "示例自定义角色"
}
```

`roleKey` 规则：

```text
^[a-z][a-z0-9_-]{1,63}$
```

即长度 2~64，以小写字母开头，只允许小写字母、数字、下划线和中划线。创建后 `roleKey` 不可修改，新角色默认启用。

创建自定义角色并不会自动授予任何接口能力，必须由后端在 Controller 上显式声明该角色后才生效。

### 8.5 修改与删除角色

修改示例：

```json
{
  "roleName": "新角色名称",
  "status": 0,
  "remark": ""
}
```

规则：

- 可修改角色名称、状态、备注，不能修改 `roleKey`；
- 三个可修改字段不能同时为 `null`/空；
- `remark: ""` 可以清空备注；
- `admin`、`user` 均不允许删除；
- `admin` 不允许禁用；
- `user` 允许禁用，但影响范围很大，前端应做高风险确认；
- 删除自定义角色时会清理该角色的用户关联。

### 8.6 全量覆盖用户角色

请求：

```json
{
  "roleIds": [1, 2]
}
```

语义：

- 这是全量覆盖，不是增量新增；
- `roleIds: []` 表示清空该用户的全部角色；
- `roleIds: null` 或省略字段会返回 `PARAM_ERROR`；
- 数组不能包含 `null`；
- 重复 ID 会被去重；
- 用户或任一角色不存在时整体失败；
- 被清空角色的用户不会自动登出，但后续访问角色接口会得到 403；
- 分配禁用角色可以保存关联，但禁用角色不属于有效角色，不能通过鉴权，也不会出现在用户接口返回的 `roles` 中。

---

## 9. 字典管理模块

### 9.1 接口清单

| Method | 路径 | 请求 | 成功时 `data` | 角色 |
|---|---|---|---|---|
| GET | `/api/system/dicts/types/page` | query：分页、`typeKey?`、`typeName?`、`status?` | `{ total, types }` | `admin` 或 `user` |
| POST | `/api/system/dicts/types` | JSON：`typeKey`、`typeName`、`remark?` | `{ typeId }` | `admin` |
| PUT | `/api/system/dicts/types/{id}` | JSON：`typeName?`、`status?`、`remark?` | `{ typeId }` | `admin` |
| DELETE | `/api/system/dicts/types/{id}` | path：类型 ID | `{ typeId }` | `admin` |
| GET | `/api/system/dicts/data?typeKey=...` | query：`typeKey` | `{ typeKey, dataList }`，仅启用项 | `admin` 或 `user` |
| GET | `/api/system/dicts/data/all?typeKey=...` | query：`typeKey` | `{ typeKey, dataList }`，含禁用项 | `admin` 或 `user` |
| POST | `/api/system/dicts/data` | JSON：`typeKey`、`label`、`value`、`sort?`、`remark?` | `{ dataId }` | `admin` |
| PUT | `/api/system/dicts/data/{id}` | JSON：`label?`、`value?`、`sort?`、`status?`、`remark?` | `{ dataId }` | `admin` |
| DELETE | `/api/system/dicts/data/{id}` | path：数据 ID | `{ dataId }` | `admin` |

### 9.2 字典类型

类型结构：

```json
{
  "id": 1,
  "typeKey": "user_status",
  "typeName": "用户状态",
  "status": 1,
  "remark": null,
  "createAt": "2026-07-10T10:00:00",
  "updateAt": "2026-07-10T10:00:00"
}
```

查询规则：

- `typeKey`：精确匹配；
- `typeName`：模糊匹配；
- `status`：精确匹配；
- 默认按类型 ID 倒序。

创建请求：

```json
{
  "typeKey": "order_status",
  "typeName": "订单状态",
  "remark": "订单状态字典"
}
```

`typeKey` 规则：

```text
^[a-z][a-z0-9_]{1,63}$
```

即长度 2~64，不允许中划线。创建后 `typeKey` 不可修改，新类型默认启用。

更新只能修改 `typeName`、`status`、`remark`，三者不能同时为空；`remark: ""` 可清空备注。

删除字典类型是逻辑删除，并同时逻辑删除该类型下的字典数据。前端删除确认应明确提示级联影响。

### 9.3 字典数据

数据结构：

```json
{
  "id": 1,
  "typeKey": "user_status",
  "label": "启用",
  "value": "1",
  "sort": 1,
  "status": 1,
  "remark": null,
  "createAt": "2026-07-10T10:00:00",
  "updateAt": "2026-07-10T10:00:00"
}
```

业务表单下拉选项应使用：

```http
GET /api/system/dicts/data?typeKey=user_status
```

该接口只返回“类型启用且数据启用”的项目，并走 Redis 缓存。类型不存在或已禁用时当前行为是成功返回空 `dataList`，不是 `DICT_TYPE_NOT_FOUND`。

管理页应使用：

```http
GET /api/system/dicts/data/all?typeKey=user_status
```

该接口包含禁用项，不走缓存。两个接口都按 `sort`、ID 升序返回，不分页。

创建请求：

```json
{
  "typeKey": "order_status",
  "label": "待支付",
  "value": "pending",
  "sort": 1,
  "remark": null
}
```

规则：

- `typeKey`、`label`、`value` 必填；
- `sort` 省略时默认为 `0`；
- 新数据默认启用；
- 同一 `typeKey` 下 `value` 必须唯一；
- 更新时不能修改 `typeKey`；
- 更新字段不能全部为空，`remark: ""` 可清空备注。

### 9.4 页面操作边界

`user` 角色可以进入字典类型页和字典数据页查看数据，但创建、编辑、删除按钮必须隐藏或禁用；后端会对写请求返回 403。

---

## 10. 文件管理模块

全部接口允许 `admin` 或 `user`。

### 10.1 接口清单

| Method | 路径 | 请求 | 成功响应 |
|---|---|---|---|
| GET | `/api/system/files/page` | query：`pageNum`、`pageSize`、`originalName?`、`uploadBy?` | JSON：`{ total, files }` |
| POST | `/api/system/files` | `multipart/form-data`，字段名 `file` | JSON：`{ fileId, originalName, size }` |
| GET | `/api/system/files/{id}/download` | path：文件 ID | 二进制流 |
| DELETE | `/api/system/files/{id}` | path：文件 ID | JSON：`{ fileId }` |

### 10.2 文件列表数据

```json
{
  "id": 100,
  "originalName": "example.pdf",
  "size": 102400,
  "contentType": "application/pdf",
  "uploadBy": 1,
  "createAt": "2026-07-10T10:00:00"
}
```

当前 `FileDTO` **只有**以上字段：

- `size` 单位为字节；
- 不返回对象存储路径；
- 不返回公开 URL 或预签名 URL；
- 不存在 `storageType` 或“存储方式”字段；
- 当前实现固定使用 S3 兼容对象存储，存储细节对前端透明。

查询规则：

- `originalName`：模糊匹配；
- `uploadBy`：上传人用户 ID 精确匹配；
- 默认按文件 ID 倒序。

### 10.3 上传

请求必须是单文件 multipart：

```text
Content-Type: multipart/form-data
字段名: file
```

规则：

- 单文件上限 10MB；
- 文件不能为空；
- 原始文件名不能为空，也不能包含 `..`、`/`、`\`；
- 前端应在选择文件后预校验大小，但后端校验仍是最终依据；
- 上传成功仅返回文件 ID、原始文件名和字节大小，不返回下载 URL。

### 10.4 下载

成功响应：

- HTTP 200；
- body 为文件二进制内容；
- `Content-Disposition` 包含 UTF-8 文件名；
- `Content-Type` 尽量使用上传时记录的 MIME，非法或缺失时回退为 `application/octet-stream`。

前端应使用支持自定义请求头的请求方式并按 Blob/ArrayBuffer 处理，例如 Axios 的 `responseType: "blob"` 或 Fetch 的 `response.blob()`，因为下载接口也要求 `sa-token` 请求头。

下载失败语义：

- 未登录/无角色：在进入 Controller 前返回 401/403 和统一 JSON；
- 文件不存在：Controller 返回 HTTP 404，响应体为空；
- 其他读取失败：Controller 返回 HTTP 500，响应体为空；
- 文件不存在和读取失败不会返回统一 `ResultDO`，前端需要按 HTTP 状态给出通用提示。

### 10.5 删除与文件所有权

删除流程为：逻辑删除元数据，然后尽力清理 S3 对象；物理文件清理失败只记录服务端日志，不改变已返回的删除成功结果。

当前没有文件所有权校验：

- `admin` 和 `user` 都能查询全部文件；
- 二者都能下载任意文件；
- 二者都能删除任意文件；
- 不是“用户只能管理自己上传的文件”。

如果产品要求个人文件隔离，必须先补充后端授权逻辑，不能只依赖前端隐藏操作。

### 10.6 头像接入限制

用户的 `avatar` 字段是字符串 URL，而文件上传只返回 `fileId`，两者当前没有自动转换关系。并且原生 `<img src="...">` 无法附加 `sa-token` 自定义请求头。

因此不能直接宣称“上传文件后即可作为头像”。可选方案需要产品和后端明确：

- 后端新增公开/预签名图片 URL；
- 后端新增头像专用接口；
- 前端以带 token 的请求下载 Blob 后创建临时 Object URL，并约定 `avatar` 保存何种可解析标识。

---

## 11. 日志管理模块

全部接口仅允许 `admin`，且都是只读接口。日志通过异步事件落库，写操作结束后列表中可能有短暂延迟。

### 11.1 接口清单

| Method | 路径 | query 参数 | 成功时 `data` |
|---|---|---|---|
| GET | `/api/system/logs/page` | 分页、`module?`、`operatorId?`、`startTime?`、`endTime?` | `{ total, logs }` |
| GET | `/api/system/logs/login/page` | 分页、`email?`、`userId?`、`success?`、`startTime?`、`endTime?` | `{ total, logs }` |

### 11.2 操作日志

字段：

```json
{
  "id": 1,
  "traceId": "链路标识",
  "operatorId": 1,
  "module": "user",
  "action": "创建用户",
  "uri": "/api/system/users",
  "params": "参数摘要字符串",
  "resultCode": "SUCCESS",
  "costMs": 25,
  "ip": "127.0.0.1",
  "createAt": "2026-07-10T10:00:00"
}
```

查询规则：

- `module`：精确匹配；
- `operatorId`：精确匹配；
- 时间范围包含边界；
- 默认按 ID 倒序。

`resultCode` 成功时为 `SUCCESS`，业务失败时通常是具体错误码，未捕获异常时为 `EXCEPTION`。`params` 是服务端生成的摘要字符串，不是稳定 JSON；密码、文件内容和在线会话 token 已在相关 DTO/切面中排除，前端应按普通文本展示。

### 11.3 登录日志

字段：

```json
{
  "id": 1,
  "traceId": "链路标识",
  "userId": 1,
  "email": "admin@example.com",
  "success": true,
  "code": "SUCCESS",
  "msg": null,
  "ip": "127.0.0.1",
  "userAgent": "浏览器 User-Agent",
  "createAt": "2026-07-10T10:00:00"
}
```

查询规则：

- `email`：精确匹配；
- `userId`：精确匹配；
- `success`：布尔值精确匹配；
- 时间范围包含边界；
- 默认按 ID 倒序；
- 登录失败时 `userId` 通常为 `null`，`code` 和 `msg` 记录失败原因。

---

## 12. 在线用户模块

全部接口仅允许 `admin`。

### 12.1 接口清单

| Method | 路径 | 请求 | 成功时 `data` |
|---|---|---|---|
| GET | `/api/system/online/page` | query：`pageNum`、`pageSize` | `{ total, onlineUsers }` |
| DELETE | `/api/system/online/tokens` | JSON：`tokenValue` | `null` |
| DELETE | `/api/system/online/users/{userId}` | path：用户 ID | `null` |

### 12.2 在线会话数据

```json
{
  "tokenValue": "敏感的会话 token",
  "userId": 1,
  "email": "admin@example.com",
  "nickname": "系统管理员",
  "ip": "127.0.0.1",
  "userAgent": "浏览器 User-Agent",
  "loginTime": "2026-07-10T10:00:00"
}
```

一个用户多端登录时会显示为多条会话。部分历史会话或会话附加信息写入失败时，`email`、`nickname`、`ip`、`userAgent`、`loginTime` 可能为空。

当前实现会扫描有效 token 后在内存中分页：

- 只有分页参数，没有用户、邮箱或 IP 筛选；
- 返回顺序没有显式保证；
- 前端不能在当前页数据上做筛选后声称是全量搜索。

### 12.3 按单个会话踢下线

请求体必须是 JSON，token 不放在 URL：

```json
{
  "tokenValue": "待踢下线的 token"
}
```

会话不存在或已失效时返回 HTTP 200、业务错误 `NOT_FOUND`。

`tokenValue` 与登录 token 同等敏感。列表中可以把它作为行操作的临时值，但不得：

- 显示完整 token 给普通用户；
- 写入 URL、路由参数或浏览器历史；
- 输出到前端日志、埋点或错误上报；
- 长期存入本地持久化状态。

### 12.4 按用户踢下线

```http
DELETE /api/system/online/users/{userId}
```

该接口会踢下线用户的全部在线会话。用户当前不在线时返回业务错误 `NOT_FOUND`。

如果管理员踢的是自己的当前 token 或自己的全部会话，操作响应可能仍成功；前端应立即清理当前登录态并跳转登录页。

---

## 13. 查询匹配规则汇总

| 接口 | 字段 | 匹配方式 |
|---|---|---|
| 用户分页 | `email` | 精确 |
| 用户分页 | `nickname` | 模糊 |
| 角色分页 | `roleKey` | 精确 |
| 角色分页 | `roleName` | 模糊 |
| 字典类型分页 | `typeKey` | 精确 |
| 字典类型分页 | `typeName` | 模糊 |
| 字典数据列表 | `typeKey` | 精确 |
| 文件分页 | `originalName` | 模糊 |
| 文件分页 | `uploadBy` | 用户 ID 精确 |
| 操作日志分页 | `module` | 精确 |
| 操作日志分页 | `operatorId` | 用户 ID 精确 |
| 登录日志分页 | `email` | 精确 |
| 登录日志分页 | `userId`、`success` | 精确 |
| 在线用户分页 | — | 当前无筛选条件 |

所有状态筛选均为 `0`/`1` 精确匹配。前端表单文案应与实际匹配规则一致，尤其不要把用户邮箱筛选描述为模糊搜索。

---

## 14. 错误码与前端处理

业务错误通常以 HTTP 200 返回。下表是当前 `ErrorCodeEnum` 定义：

| code | 默认文案/含义 | 前端建议 |
|---|---|---|
| `FAIL` | 操作失败 | 展示 `msg` |
| `PARAM_ERROR` | 参数错误 | 优先定位到表单字段，展示具体 `msg` |
| `SYSTEM_ERROR` | 系统异常 | 提示稍后重试并保留 `X-Trace-Id` |
| `LOCK_FAIL` | 获取锁失败，请稍后重试 | 防重复提交并允许稍后重试 |
| `NOT_LOGIN` | 未登录或登录已过期 | 清理登录态并跳转登录页 |
| `NO_PERMISSION` | 无权限访问 | 展示 403；不要无限重试 |
| `BAD_REQUEST` | 请求不合法 | 检查 JSON、参数类型和时间格式 |
| `NOT_FOUND` | 请求资源不存在 | 刷新当前列表；在线踢人也使用此码 |
| `DATA_ERROR` | 数据异常 | 通用错误提示并记录 traceId |
| `STATUS_INVALID` | 状态不合法 | 刷新数据，避免重复状态变更 |
| `DB_QUERY_ERROR` | 数据库查询异常 | 通用重试提示 |
| `DB_SAVE_ERROR` | 数据库保存异常 | 通用重试提示 |
| `DB_DELETE_ERROR` | 数据库删除异常 | 通用重试提示 |
| `AUTH_FAIL` | 邮箱或密码错误 | 登录表单内提示，不泄露账号是否存在 |
| `AUTH_LOCKED` | 登录失败次数过多，请稍后重试 | 禁止立即重复提交，提示稍后再试 |
| `USER_DISABLED` | 账号已被禁用，请联系管理员 | 清理登录态并提示联系管理员 |
| `USER_NOT_FOUND` | 用户不存在 | 刷新用户列表 |
| `EMAIL_DUPLICATE` | 邮箱已被注册 | 邮箱字段级提示 |
| `ROLE_NOT_FOUND` | 角色不存在 | 刷新角色选项 |
| `ROLE_KEY_DUPLICATE` | 角色标识已存在 | `roleKey` 字段级提示 |
| `ROLE_BUILT_IN` | 内置角色不允许该操作 | 删除 `admin`/`user`，或禁用 `admin` 时出现 |
| `DICT_TYPE_NOT_FOUND` | 字典类型不存在 | 刷新字典类型列表 |
| `DICT_DATA_NOT_FOUND` | 字典数据不存在 | 刷新字典数据列表 |
| `DICT_VALUE_DUPLICATE` | 同类型下字典值已存在 | `value` 字段级提示 |
| `TYPE_KEY_DUPLICATE` | 字典类型键已存在 | `typeKey` 字段级提示 |
| `FILE_NOT_FOUND` | 文件不存在 | JSON 删除接口可按码处理；下载接口是 404 空 body |
| `FILE_EMPTY` | 文件内容不能为空 | 文件选择控件提示 |
| `FILE_TOO_LARGE` | 文件大小超出限制 | 提示 10MB 上限 |
| `FILE_READ_ERROR` | 文件读取失败 | 提示重新选择或稍后重试 |
| `FILE_STORE_ERROR` | 文件存储失败 | 提示稍后重试 |
| `FILE_DELETE_ERROR` | 文件删除失败 | 展示具体 `msg`；当前物理清理失败通常仅服务端记录 |

任何错误处理都不应只匹配默认中文文案；流程判断应使用稳定的 `code`，展示时优先使用服务端返回的 `msg`。

---

## 15. 当前能力边界与前端实现约束

### 15.1 个人中心只能只读

- `/api/auth/me` 可供任意登录用户读取本人信息；
- `PUT /api/system/users/{id}` 仅 `admin` 可调用；
- 没有普通用户修改本人资料接口；
- 没有修改密码、重置密码或忘记密码接口。

因此普通用户个人中心只能只读。管理员可以通过用户管理接口修改资料，但该能力不应被误认为面向普通用户的个人中心接口。

### 15.2 头像与文件未打通

- `avatar` 是字符串；
- 上传只返回 `fileId`，不返回 URL；
- 下载要求请求头 token；
- 没有公开/预签名 URL。

头像上传、回显和持久化需要新增约定或后端能力，不能仅靠当前字段自动完成。

### 15.3 自定义角色目前没有实际授权能力

自定义角色可维护、可分配，但当前所有受保护 Controller 只声明了 `admin`、`user`。前端不得把“创建角色”等同于“创建权限集合”，也不应设计不存在的权限树或角色权限配置页。

### 15.4 文件不是个人文件空间

文件模块没有所有权授权，`user` 角色也能查看和删除其他人的文件。若页面面向普通用户开放，应明确这是当前系统级文件列表，而不是“我的文件”。

### 15.5 角色回显只有有效角色键

用户详情不返回角色 ID，也不返回已禁用的已分配角色。角色分配弹窗只能通过角色列表的 `roleKey` 做映射，无法完整还原禁用角色关联；如要求精确管理关联，需要后端新增用户角色 ID 查询接口。

### 15.6 在线用户数据包含敏感 token

在线列表直接返回 `tokenValue`，这是当前按会话踢人的必要参数，也是敏感信息。前端必须限制其生命周期和可见性。

### 15.7 生产 API 文档配置存在注释与值不一致

当前 `application-prod.yaml` 注释称关闭 Swagger，但配置值为开启。部署前应由后端修正或通过网关限制，前端不能把生产 Swagger 当作稳定依赖。

---

## 16. 联调验收清单

### 通用

- [ ] 前端开发环境已通过代理或网关解决同源问题；
- [ ] 登录/注册使用 `tokenName` 和 `tokenValue`，请求头实际为 `sa-token`；
- [ ] 所有 JSON 接口同时判断 HTTP 状态和 `success`；
- [ ] 能处理 HTTP 401、403，以及 HTTP 200 的 `NOT_LOGIN`；
- [ ] 分页 `pageNum >= 1`，`pageSize` 未超过 100；
- [ ] 时间参数使用不带时区的 ISO `LocalDateTime`；
- [ ] 错误上报保留响应 `X-Trace-Id`，但不记录 token 和密码。

### 认证与权限

- [ ] 登录、注册成功后保存 token，并以 `/api/auth/me` 作为刷新后的用户信息和有效角色来源；
- [ ] 5 次错误后的 `AUTH_LOCKED` 有明确提示；
- [ ] `admin` 与 `user` 菜单、路由和操作按钮符合访问矩阵；
- [ ] 自定义角色不会错误显示系统管理能力；
- [ ] 普通用户个人中心没有可提交的资料或密码表单。

### 用户与角色

- [ ] 用户邮箱筛选按精确匹配实现，昵称才是模糊匹配；
- [ ] 创建用户的空 `roleIds` 与分配角色的空 `roleIds` 语义不同；
- [ ] 角色分配按全量覆盖处理；
- [ ] 内置角色删除按钮不可用，`admin` 禁用控件不可用；
- [ ] 禁用 `user` 角色有高风险确认；
- [ ] 自禁用、自删除或踢自己下线后能清理本地登录态。

### 字典与文件

- [ ] 字典管理页使用 `/data/all`，业务下拉使用 `/data`；
- [ ] `user` 只能查看字典，不能写；
- [ ] 文件列表没有“存储方式”、存储路径或公开 URL 列；
- [ ] 上传字段名是 `file`，前端校验 10MB；
- [ ] 下载按二进制处理并携带 token；
- [ ] 下载 404/500 空响应体不会按统一 JSON 强制解析；
- [ ] 页面未误导用户“只能管理本人文件”；
- [ ] 头像功能未假定上传响应会返回 URL。

### 日志与在线用户

- [ ] 日志筛选的邮箱、模块和用户 ID 使用精确匹配；
- [ ] 日志时间范围包含边界且开始时间不晚于结束时间；
- [ ] 能接受异步日志的短暂展示延迟；
- [ ] 在线列表不伪造后端不存在的全量筛选；
- [ ] 单会话踢人使用 DELETE JSON body `{ "tokenValue": "..." }`；
- [ ] 在线会话 token 不进入 URL、日志、埋点或长期存储。

---

## 附录：34 个接口覆盖核对

| 模块 | 接口 |
|---|---|
| 认证（4） | `POST /api/auth/login`、`POST /api/auth/register`、`POST /api/auth/logout`、`GET /api/auth/me` |
| 用户（6） | `GET /api/system/users/page`、`GET /api/system/users/{id}`、`POST /api/system/users`、`PUT /api/system/users/{id}`、`PUT /api/system/users/{id}/status`、`DELETE /api/system/users/{id}` |
| 角色（6） | `GET /api/system/roles/page`、`GET /api/system/roles/{id}`、`POST /api/system/roles`、`PUT /api/system/roles/{id}`、`DELETE /api/system/roles/{id}`、`PUT /api/system/roles/users/{userId}` |
| 字典（9） | `GET /api/system/dicts/types/page`、`POST /api/system/dicts/types`、`PUT /api/system/dicts/types/{id}`、`DELETE /api/system/dicts/types/{id}`、`GET /api/system/dicts/data`、`GET /api/system/dicts/data/all`、`POST /api/system/dicts/data`、`PUT /api/system/dicts/data/{id}`、`DELETE /api/system/dicts/data/{id}` |
| 文件（4） | `GET /api/system/files/page`、`POST /api/system/files`、`GET /api/system/files/{id}/download`、`DELETE /api/system/files/{id}` |
| 日志（2） | `GET /api/system/logs/page`、`GET /api/system/logs/login/page` |
| 在线用户（3） | `GET /api/system/online/page`、`DELETE /api/system/online/tokens`、`DELETE /api/system/online/users/{userId}` |
